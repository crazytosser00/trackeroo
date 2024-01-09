package ru.roansa.trackeroo_core.logging.publish

import android.content.Context
import android.content.Intent
import java.io.File

class ShareIntentLogPublisher(private val context: Context) : ILogPublisher<Intent>() {

    override fun publish(target: Intent) {}

    override fun prepare(target: File): Intent = Intent()
}