package ru.roansa.trackeroo_core.logging.publish

import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
import java.net.URLConnection

class ShareIntentLogPublisher(private val context: Context) : ILogPublisher<Intent>() {

    override fun publish(target: Intent) {
        //TODO add exception when no activity can handle ACTION_SEND intent
        context.startActivity(target)
    }

    override fun prepare(target: File): Intent {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", target)
        val intent = Intent(Intent.ACTION_SEND).apply {
            setDataAndType(uri, URLConnection.guessContentTypeFromName(target.name))
            clipData = ClipData(ClipData.newRawUri("", uri))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(Intent.EXTRA_STREAM, uri)
        }
        return intent
    }
}