package ru.roansa.trackeroo

import android.app.Application
import ru.roansa.sentry_publisher.SentryAttachmentPublisher
import ru.roansa.trackeroo_core.logging.Logger
import ru.roansa.trackeroo_core.logging.Logger.build
import ru.roansa.trackeroo_core.logging.file.LogTextFileWriter
import ru.roansa.trackeroo_core.logging.publish.ExportLogPublisher
import ru.roansa.trackeroo_core.logging.transform.DebugLevelTransformer
import ru.roansa.trackeroo_core.logging.transform.MessageTransformer
import ru.roansa.trackeroo_core.logging.transform.TimeTransformer
import timber.log.Timber

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Logger.init(applicationContext)
            .setLogWriter(LogTextFileWriter(Logger.logFileConfig, true))
            .setLogPublisher(SentryAttachmentPublisher())
            .addLogTransformer(TimeTransformer())
            .addLogTransformer(DebugLevelTransformer())
            .addLogTransformer(MessageTransformer())
            .build()

        Timber.plant(Timber.DebugTree())
    }
}