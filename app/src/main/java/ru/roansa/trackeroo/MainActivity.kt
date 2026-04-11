package ru.roansa.trackeroo

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import ru.roansa.trackeroo_core.logging.Logger
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: DebugViewModel

    private lateinit var tvEmitCount: TextView
    private lateinit var tvCollectCount: TextView
    private lateinit var tvCollectorStatus: TextView
    private lateinit var tvCurrentFile: TextView
    private lateinit var tvFileSize: TextView
    private lateinit var tvFilesCount: TextView
    private lateinit var btnRefreshStatus: Button

    private lateinit var etAutoLogInterval: TextInputEditText
    private lateinit var btnStartAutoLog: Button
    private lateinit var btnStopAutoLog: Button
    private lateinit var tvAutoLogStatus: TextView

    private lateinit var etStressTestCount: TextInputEditText
    private lateinit var btnRunStressTest: Button
    private lateinit var pbStressTest: ProgressBar
    private lateinit var tvStressTestProgress: TextView

    private lateinit var rvLogFiles: RecyclerView
    private lateinit var btnRefreshFiles: Button
    private lateinit var logFileAdapter: LogFileAdapter

    private lateinit var tvServerUrl: TextView

    private lateinit var btnStartServer: Button
    private lateinit var btnStopServer: Button

    private lateinit var btnPublish: Button
    private lateinit var btnClearLogs: Button
    private lateinit var btnThrowException: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this)[DebugViewModel::class.java]

        initViews()
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
        updateServerUrl()
    }

    private fun initViews() {
        tvEmitCount = findViewById(R.id.tvEmitCount)
        tvCollectCount = findViewById(R.id.tvCollectCount)
        tvCollectorStatus = findViewById(R.id.tvCollectorStatus)
        tvCurrentFile = findViewById(R.id.tvCurrentFile)
        tvFileSize = findViewById(R.id.tvFileSize)
        tvFilesCount = findViewById(R.id.tvFilesCount)
        btnRefreshStatus = findViewById(R.id.btnRefreshStatus)

        etAutoLogInterval = findViewById(R.id.etAutoLogInterval)
        btnStartAutoLog = findViewById(R.id.btnStartAutoLog)
        btnStopAutoLog = findViewById(R.id.btnStopAutoLog)
        tvAutoLogStatus = findViewById(R.id.tvAutoLogStatus)

        etStressTestCount = findViewById(R.id.etStressTestCount)
        btnRunStressTest = findViewById(R.id.btnRunStressTest)
        pbStressTest = findViewById(R.id.pbStressTest)
        tvStressTestProgress = findViewById(R.id.tvStressTestProgress)

        rvLogFiles = findViewById(R.id.rvLogFiles)
        btnRefreshFiles = findViewById(R.id.btnRefreshFiles)

        tvServerUrl = findViewById(R.id.tvServerUrl)

        btnStartServer = findViewById(R.id.btnStartServer)
        btnStopServer = findViewById(R.id.btnStopServer)

        btnPublish = findViewById(R.id.btnPublish)
        btnClearLogs = findViewById(R.id.btnClearLogs)
        btnThrowException = findViewById(R.id.btnThrowException)
    }

    private fun setupRecyclerView() {
        logFileAdapter = LogFileAdapter { file ->
            showFileContentDialog(file)
        }
        rvLogFiles.layoutManager = LinearLayoutManager(this)
        rvLogFiles.adapter = logFileAdapter
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.status.collect { status ->
                status?.let { updateStatusUI(it) }
            }
        }

        lifecycleScope.launch {
            viewModel.stressTestProgress.collect { progress ->
                progress?.let { updateStressTestUI(it) } ?: hideStressTestProgress()
            }
        }

        lifecycleScope.launch {
            viewModel.logFiles.collect { files ->
                logFileAdapter.updateFiles(files)
            }
        }
    }

    private fun setupClickListeners() {
        btnRefreshStatus.setOnClickListener {
            viewModel.refreshStatus()
            viewModel.refreshLogFiles()
            updateServerUrl()
        }

        btnStartAutoLog.setOnClickListener {
            val intervalText = etAutoLogInterval.text?.toString() ?: "500"
            val interval = intervalText.toLongOrNull() ?: 500L
            if (interval > 0) {
                viewModel.startAutoLogging(interval)
            }
        }

        btnStopAutoLog.setOnClickListener {
            viewModel.stopAutoLogging()
        }

        btnRunStressTest.setOnClickListener {
            val countText = etStressTestCount.text?.toString() ?: "1000"
            val count = countText.toIntOrNull() ?: 1000
            if (count > 0) {
                viewModel.runStressTest(count)
            }
        }

        btnRefreshFiles.setOnClickListener {
            viewModel.refreshLogFiles()
        }

        btnStartServer.setOnClickListener {
            App.logHttpServer.start()
            updateServerUrl()
        }

        btnStopServer.setOnClickListener {
            App.logHttpServer.stop()
            updateServerUrl()
        }

        btnPublish.setOnClickListener {
            Logger.publish()
        }

        btnClearLogs.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Clear Logs")
                .setMessage("Are you sure you want to delete all log files?")
                .setPositiveButton("Yes") { _, _ ->
                    Logger.clearLogs()
                    viewModel.refreshStatus()
                    viewModel.refreshLogFiles()
                }
                .setNegativeButton("No", null)
                .show()
        }

        btnThrowException.setOnClickListener {
            var a: Int? = null
            var x = 6
            x = x / a!!
        }
    }

    private fun updateStatusUI(status: DebugStatus) {
        tvEmitCount.text = "Emit count: ${status.emitCount}"
        tvCollectCount.text = "Collect count: ${status.collectCount}"
        tvCollectorStatus.text = "Collector: ${if (status.collectorActive) "active" else "inactive"}"
        tvCurrentFile.text = "Current file: ${status.currentFile.substringAfterLast("/")}"
        tvFileSize.text = "File size: ${formatFileSize(status.currentFileSize)}"
        tvFilesCount.text = "Files count: ${status.filesCount}"

        tvAutoLogStatus.text = "Status: ${if (status.autoLoggingActive) "Running" else "Stopped"}"
    }

    private fun updateStressTestUI(progress: StressTestProgress) {
        pbStressTest.visibility = android.view.View.VISIBLE
        tvStressTestProgress.visibility = android.view.View.VISIBLE
        pbStressTest.max = progress.total
        pbStressTest.progress = progress.current
        tvStressTestProgress.text = "${progress.current} / ${progress.total}"
        
        if (progress.completed) {
            btnRunStressTest.isEnabled = true
        } else {
            btnRunStressTest.isEnabled = false
        }
    }

    private fun hideStressTestProgress() {
        pbStressTest.visibility = android.view.View.GONE
        tvStressTestProgress.visibility = android.view.View.GONE
        btnRunStressTest.isEnabled = true
    }

    private fun showFileContentDialog(file: LogFileItem) {
        val content = viewModel.readFileContent(file.path) ?: "Unable to read file"
        
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_file_content, null)
        val tvFileName = dialogView.findViewById<TextView>(R.id.tvDialogFileName)
        val tvFileContent = dialogView.findViewById<TextView>(R.id.tvFileContent)
        val btnClose = dialogView.findViewById<Button>(R.id.btnDialogClose)

        tvFileName.text = file.name
        tvFileContent.text = content

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    /**
     * Обновляет отображение URL лог-сервера.
     * Получает адрес из {@link App.logHttpServer}.
     */
    private fun updateServerUrl() {
        val url = App.logHttpServer.getServerUrl()
        tvServerUrl.text = "Log server: ${url ?: "-"}"
    }

    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes bytes"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> "${bytes / (1024 * 1024)} MB"
        }
    }
}
