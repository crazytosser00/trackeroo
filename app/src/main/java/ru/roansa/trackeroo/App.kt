package ru.roansa.trackeroo

import android.app.Application
import ru.roansa.trackeroo_core.logging.LogFileWriter
import ru.roansa.trackeroo_core.logging.Logger
import ru.roansa.trackeroo_core.logging.transform.DebugLevelTransformer
import ru.roansa.trackeroo_core.logging.transform.LogFormatter
import ru.roansa.trackeroo_core.logging.transform.MessageTransformer
import ru.roansa.trackeroo_core.logging.transform.TimeTransformer
import ru.roansa.trackeroo_core.publish.ExportLogPublisher
import timber.log.Timber

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Logger.default(applicationContext)

        Timber.plant(Timber.DebugTree())
    }
}