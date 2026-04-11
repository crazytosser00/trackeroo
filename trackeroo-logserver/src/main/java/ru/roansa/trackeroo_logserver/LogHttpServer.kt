package ru.roansa.trackeroo_logserver

import android.content.Context
import android.util.Log
import fi.iki.elonen.NanoHTTPD
import ru.roansa.trackeroo_core.logging.Logger
import ru.roansa.trackeroo_core.module.ITrackerooModule
import java.io.Closeable
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Встроенный HTTP-сервер для стриминга логов по локальной сети.
 *
 * Предоставляет:
 * - GET /          — веб-интерфейс с real-time логами
 * - GET /stream    — SSE-стрим новых строк лога
 * - GET /files     — JSON-список файлов логов
 * - GET /files/{n} — скачивание файла логов
 *
 * @param context контекст приложения (applicationContext)
 * @param port порт сервера, по умолчанию 8888
 */
class LogHttpServer(
    private val context: Context,
    private val port: Int = 8888
) : ITrackerooModule {

    private val sseClients = CopyOnWriteArrayList<PipedOutputStream>()
    private var server: NanoHTTPD? = null

    private val logDir: File
        get() = Logger.logFileConfig.logDirectory

    private val logListener = object : Logger.OnNewFormattedLogStringListener {
        override fun onNewFormattedLogString(logString: String) {
            val sseData = "data: ${logString.replace("\n", "\ndata: ")}\n\n"
            val bytes = sseData.toByteArray(Charsets.UTF_8)

            val deadClients = mutableListOf<PipedOutputStream>()
            for (client in sseClients) {
                try {
                    client.write(bytes)
                    client.flush()
                } catch (e: IOException) {
                    deadClients.add(client)
                    closeSilently(client)
                }
            }
            if (deadClients.isNotEmpty()) {
                sseClients.removeAll(deadClients.toSet())
            }
        }
    }

    companion object {
        private const val TAG = "LogHttpServer"
        private const val MAX_SSE_CLIENTS = 10

        /**
         * Безопасный запуск сервера. Любая ошибка логируется, но не пробрасывается.
         *
         * @param context контекст приложения
         * @param port порт сервера
         * @return инстанс сервера или null при ошибке запуска
         */
        @Deprecated(
            "Use ITrackerooModule.start() instead",
            ReplaceWith("LogHttpServer(context, port).also { it.start() }")
        )
        fun startSafely(context: Context, port: Int = 8888): LogHttpServer? {
            return try {
                val server = LogHttpServer(context.applicationContext, port)
                server.start()
                server
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to start server: ${t.message}", t)
                null
            }
        }
    }

    /**
     * Запускает HTTP-сервер и подписывается на лог-строки.
     * Если сервер уже запущен, ничего не делает.
     */
    override fun start() {
        if (isRunning()) return
        try {
            val httpServer = createServer()
            httpServer.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false)
            server = httpServer
            Logger.addOnNewFormattedLogStringListener(logListener)
            Log.i(TAG, "Server started on port $port")
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to start server: ${t.message}", t)
        }
    }

    /**
     * Останавливает HTTP-сервер, отписывается от лог-строк и закрывает SSE-клиентов.
     */
    override fun stop() {
        try {
            Logger.removeOnNewFormattedLogStringListener(logListener)
            closeAllSseClients()
            server?.stop()
            server = null
            Log.i(TAG, "Server stopped")
        } catch (t: Throwable) {
            Log.e(TAG, "Error stopping server: ${t.message}", t)
        }
    }

    /**
     * Проверяет, запущен ли сервер.
     *
     * @return true если сервер активен
     */
    override fun isRunning(): Boolean = server?.isAlive == true

    /**
     * Безопасная остановка сервера и очистка ресурсов.
     */
    @Deprecated(
        "Use ITrackerooModule.stop() instead",
        ReplaceWith("stop()")
    )
    fun stopSafely() {
        stop()
    }

    /**
     * Возвращает URL запущенного сервера в локальной сети.
     * Использует {@link NetworkInterface} для определения IP — не требует пермишенов.
     *
     * @return URL вида "http://<ip>:<port>" или null, если сервер не запущен или IP не определён
     */
    fun getServerUrl(): String? {
        if (!isRunning()) return null
        val ip = getDeviceLocalIpAddress() ?: return null
        return "http://$ip:$port"
    }

    /**
     * Определяет локальный IPv4-адрес устройства, перебирая сетевые интерфейсы.
     *
     * @return IPv4-адрес в виде строки или null, если подходящий адрес не найден
     */
    private fun getDeviceLocalIpAddress(): String? {
        return try {
            NetworkInterface.getNetworkInterfaces()?.toList().orEmpty()
                .filter { it.isUp && !it.isLoopback }
                .flatMap { it.inetAddresses.toList() }
                .firstOrNull { it is Inet4Address && !it.isLoopbackAddress }
                ?.hostAddress
        } catch (e: Exception) {
            Log.w(TAG, "Failed to resolve local IP: ${e.message}")
            null
        }
    }

    /**
     * Создаёт экземпляр NanoHTTPD с обработкой маршрутов.
     * Вся логика маршрутизации находится внутри анонимного класса,
     * чтобы иметь доступ к protected-методам NanoHTTPD.
     *
     * @return настроенный экземпляр NanoHTTPD
     */
    private fun createServer(): NanoHTTPD {
        return object : NanoHTTPD("0.0.0.0", port) {
            override fun useGzipWhenAccepted(r: Response): Boolean =
                r.mimeType != "text/event-stream" && super.useGzipWhenAccepted(r)

            override fun serve(session: IHTTPSession): Response {
                return try {
                    val uri = session.uri ?: "/"
                    when {
                        uri == "/stream" -> serveSseStream()
                        uri == "/files" -> serveFileList()
                        uri.startsWith("/files/") -> serveLogFile(uri.removePrefix("/files/"))
                        else -> serveHtmlPage()
                    }
                } catch (t: Throwable) {
                    Log.e(TAG, "Error serving ${session.uri}: ${t.message}", t)
                    newFixedLengthResponse(
                        Response.Status.INTERNAL_ERROR,
                        MIME_PLAINTEXT,
                        "Internal server error"
                    )
                }
            }

            private fun serveSseStream(): Response {
                if (sseClients.size >= MAX_SSE_CLIENTS) {
                    return newFixedLengthResponse(
                        Response.Status.SERVICE_UNAVAILABLE,
                        MIME_PLAINTEXT,
                        "Too many SSE clients"
                    )
                }

                val pipedOut = PipedOutputStream()
                val pipedIn = PipedInputStream(pipedOut, 64 * 1024)
                sseClients.add(pipedOut)

                return newChunkedResponse(Response.Status.OK, "text/event-stream", pipedIn).apply {
                    addHeader("Cache-Control", "no-cache")
                    addHeader("Connection", "keep-alive")
                    addHeader("Access-Control-Allow-Origin", "*")
                    addHeader("X-Accel-Buffering", "no")
                }
            }

            private fun serveFileList(): Response {
                val files = logDir.listFiles()?.filter { it.isFile && it.name.endsWith(".txt") }
                    ?: emptyList()

                val json = files.joinToString(",", "[", "]") { f ->
                    """{"name":"${f.name.replace("\"", "\\\"")}","size":${f.length()},"lastModified":${f.lastModified()}}"""
                }

                return newFixedLengthResponse(Response.Status.OK, "application/json", json)
            }

            private fun serveLogFile(fileName: String): Response {
                val sanitized = fileName.replace("..", "").replace("/", "").replace("\\", "")
                if (sanitized.isEmpty()) {
                    return newFixedLengthResponse(Response.Status.BAD_REQUEST, MIME_PLAINTEXT, "Invalid file name")
                }

                val file = File(logDir, sanitized)
                if (!file.exists() || !file.isFile || !file.canonicalPath.startsWith(logDir.canonicalPath)) {
                    return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "File not found")
                }

                val fis = FileInputStream(file)
                return newChunkedResponse(Response.Status.OK, "application/octet-stream", fis).apply {
                    addHeader("Content-Disposition", "attachment; filename=\"${file.name}\"")
                }
            }

            private fun serveHtmlPage(): Response {
                return newFixedLengthResponse(Response.Status.OK, "text/html", HTML_PAGE)
            }
        }
    }

    private fun closeAllSseClients() {
        for (client in sseClients) {
            closeSilently(client)
        }
        sseClients.clear()
    }

    private fun closeSilently(closeable: Closeable) {
        try {
            closeable.close()
        } catch (_: Throwable) {}
    }

    private val HTML_PAGE = """
<!DOCTYPE html>
<html lang="ru">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Log Viewer</title>
<style>
  * { margin: 0; padding: 0; box-sizing: border-box; }
  body {
    background: #1a1a2e;
    color: #c8c8d4;
    font: 13px/1.5 'JetBrains Mono', 'Fira Code', 'Consolas', monospace;
    padding: 0;
  }
  #toolbar {
    position: sticky;
    top: 0;
    z-index: 10;
    background: #16213e;
    padding: 8px 12px;
    display: flex;
    align-items: center;
    gap: 10px;
    border-bottom: 1px solid #0f3460;
    flex-wrap: wrap;
  }
  #toolbar button {
    background: #0f3460;
    color: #e0e0e0;
    border: 1px solid #533483;
    padding: 4px 14px;
    border-radius: 4px;
    cursor: pointer;
    font-size: 12px;
  }
  #toolbar button:hover { background: #533483; }
  #toolbar button.active { background: #e94560; border-color: #e94560; }
  #status {
    font-size: 11px;
    color: #7f8c8d;
  }
  #status .connected { color: #2ecc71; }
  #status .disconnected { color: #e94560; }
  #filter {
    background: #0f3460;
    color: #e0e0e0;
    border: 1px solid #533483;
    padding: 4px 8px;
    border-radius: 4px;
    font-size: 12px;
    width: 180px;
  }
  #log {
    padding: 8px 12px;
    white-space: pre-wrap;
    word-break: break-all;
  }
  #log div { padding: 1px 0; }
  .E { color: #e94560; }
  .W { color: #f0a500; }
  .I { color: #2ecc71; }
  .D { color: #7f8c8d; }
  .V { color: #555; }
  .highlight { background: rgba(233, 69, 96, 0.2); }
  #files-panel {
    position: fixed;
    right: 0;
    top: 0;
    width: 300px;
    height: 100%;
    background: #16213e;
    border-left: 1px solid #0f3460;
    padding: 12px;
    transform: translateX(100%);
    transition: transform 0.2s;
    z-index: 20;
    overflow-y: auto;
  }
  #files-panel.open { transform: translateX(0); }
  #files-panel h3 { color: #e0e0e0; margin-bottom: 10px; font-size: 14px; }
  #files-panel a {
    display: block;
    color: #53a8b6;
    text-decoration: none;
    padding: 4px 0;
    font-size: 12px;
  }
  #files-panel a:hover { color: #e94560; }
  .file-size { color: #7f8c8d; font-size: 11px; }
</style>
</head>
<body>

<div id="toolbar">
  <button id="btn-pause">Pause</button>
  <button id="btn-clear">Clear</button>
  <button id="btn-files">Files</button>
  <button id="btn-bottom">&darr; Bottom</button>
  <input id="filter" type="text" placeholder="Filter...">
  <span id="status">connecting...</span>
  <span id="line-count" style="font-size:11px;color:#7f8c8d;"></span>
</div>

<div id="log"></div>

<div id="files-panel">
  <h3>Log files</h3>
  <div id="files-list">Loading...</div>
</div>

<script>
(function() {
  const log = document.getElementById('log');
  const status = document.getElementById('status');
  const lineCount = document.getElementById('line-count');
  const filterInput = document.getElementById('filter');
  const filesPanel = document.getElementById('files-panel');
  const filesList = document.getElementById('files-list');

  const MAX_LINES = 5000;
  let paused = false;
  let buffer = [];
  let es = null;
  let totalLines = 0;
  let autoScroll = true;
  let filterText = '';

  function classifyLine(text) {
    if (text.includes(' E ') || text.includes('/E ') || text.includes('ERROR')) return 'E';
    if (text.includes(' W ') || text.includes('/W ') || text.includes('WARN')) return 'W';
    if (text.includes(' I ') || text.includes('/I ') || text.includes('INFO')) return 'I';
    if (text.includes(' D ') || text.includes('/D ') || text.includes('DEBUG')) return 'D';
    return 'V';
  }

  function matchesFilter(text) {
    if (!filterText) return true;
    return text.toLowerCase().includes(filterText);
  }

  function appendLine(text) {
    totalLines++;
    if (!matchesFilter(text)) return;

    const div = document.createElement('div');
    div.textContent = text;
    div.className = classifyLine(text);
    log.appendChild(div);

    while (log.childNodes.length > MAX_LINES) {
      log.removeChild(log.firstChild);
    }

    lineCount.textContent = log.childNodes.length + ' lines (total: ' + totalLines + ')';

    if (autoScroll) {
      window.scrollTo(0, document.body.scrollHeight);
    }
  }

  function connect() {
    if (es) { es.close(); }
    try {
      es = new EventSource('/stream');
    } catch (err) {
      return;
    }
    es.onopen = function() {
      status.innerHTML = '<span class="connected">&#9679; connected</span>';
    };
    es.onmessage = function(e) {
      if (paused) {
        buffer.push(e.data);
        if (buffer.length > MAX_LINES) buffer.shift();
        status.innerHTML = '<span class="connected">&#9679; paused (' + buffer.length + ' buffered)</span>';
        return;
      }
      appendLine(e.data);
    };
    es.onerror = function() {
      status.innerHTML = '<span class="disconnected">&#9679; disconnected</span>';
      setTimeout(connect, 3000);
    };
  }

  document.getElementById('btn-pause').addEventListener('click', function() {
    paused = !paused;
    this.textContent = paused ? 'Resume' : 'Pause';
    this.classList.toggle('active', paused);
    if (!paused && buffer.length > 0) {
      buffer.forEach(appendLine);
      buffer = [];
    }
  });

  document.getElementById('btn-clear').addEventListener('click', function() {
    log.innerHTML = '';
    totalLines = 0;
    lineCount.textContent = '';
  });

  document.getElementById('btn-bottom').addEventListener('click', function() {
    autoScroll = true;
    window.scrollTo(0, document.body.scrollHeight);
  });

  window.addEventListener('scroll', function() {
    var atBottom = (window.innerHeight + window.scrollY) >= (document.body.scrollHeight - 50);
    autoScroll = atBottom;
  });

  filterInput.addEventListener('input', function() {
    filterText = this.value.toLowerCase();
  });

  document.getElementById('btn-files').addEventListener('click', function() {
    filesPanel.classList.toggle('open');
    if (filesPanel.classList.contains('open')) {
      loadFiles();
    }
  });

  function loadFiles() {
    filesList.innerHTML = 'Loading...';
    fetch('/files')
      .then(function(r) { return r.json(); })
      .then(function(files) {
        if (files.length === 0) {
          filesList.innerHTML = 'No log files found';
          return;
        }
        filesList.innerHTML = '';
        files.forEach(function(f) {
          var a = document.createElement('a');
          a.href = '/files/' + encodeURIComponent(f.name);
          a.target = '_blank';
          var sizeKb = (f.size / 1024).toFixed(1);
          a.innerHTML = f.name + ' <span class="file-size">(' + sizeKb + ' KB)</span>';
          filesList.appendChild(a);
        });
      })
      .catch(function() {
        filesList.innerHTML = 'Error loading file list';
      });
  }

  connect();
})();
</script>
</body>
</html>
""".trimIndent()
}
