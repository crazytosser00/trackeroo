package ru.roansa.trackeroo

import android.app.Application
import ru.roansa.trackeroo_core.logging.Logger
import ru.roansa.trackeroo_core.logging.Logger.build
import ru.roansa.trackeroo_core.logging.file.text.LogTextFileWriter
import ru.roansa.trackeroo_core.logging.publish.ShareIntentLogPublisher
import ru.roansa.trackeroo_core.logging.transform.DebugLevelTransformer
import ru.roansa.trackeroo_core.logging.transform.MessageTransformer
import ru.roansa.trackeroo_core.logging.transform.TimeTransformer
import timber.log.Timber

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Logger.builder(applicationContext)
            .setLogWriter(LogTextFileWriter(Logger.logFileConfig, true))
            .setLogPublisher(ShareIntentLogPublisher(applicationContext))
            .addLogTransformer(TimeTransformer())
            .addLogTransformer(DebugLevelTransformer())
            .addLogTransformer(MessageTransformer())
            .build()

        Timber.plant(Timber.DebugTree())
    }
}