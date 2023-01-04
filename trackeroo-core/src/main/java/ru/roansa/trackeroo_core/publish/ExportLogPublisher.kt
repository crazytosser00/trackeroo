package ru.roansa.trackeroo_core.publish

import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
import java.net.URLConnection

class ExportLogPublisher(private val context: Context) : ILogPublisher {

    override fun publish(file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            setDataAndType(uri, URLConnection.guessContentTypeFromName(file.name))
            clipData = ClipData(ClipData.newRawUri("", uri))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(Intent.EXTRA_STREAM, uri)
        }
        context.startActivity(intent)
    }

}