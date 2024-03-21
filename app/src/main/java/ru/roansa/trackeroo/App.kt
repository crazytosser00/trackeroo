package ru.roansa.trackeroo

import android.app.Application
import android.content.Context
import ru.roansa.trackeroo_core.hookers.exception.DefaultUncaughtExceptionHooker
import ru.roansa.trackeroo_core.hookers.exception.UncaughtExceptionAction
import ru.roansa.trackeroo_core.logging.Logger
import ru.roansa.trackeroo_core.logging.Logger.build
import ru.roansa.trackeroo_core.logging.file.text.LogTextFileWriter
import ru.roansa.trackeroo_core.logging.publish.ShareIntentLogPublisher
import ru.roansa.trackeroo_core.logging.transform.DebugLevelTransformer
import ru.roansa.trackeroo_core.logging.transform.MessageTransformer
import ru.roansa.trackeroo_core.logging.transform.TimeTransformer
import timber.log.Timber

class App : Application() {

    private val activityExceptionAction = UncaughtExceptionAction.StartActivityAction(
        "ru.roansa.trackeroo",
        "ru.roansa.trackeroo.OopsActivity"
    )

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)

    }

    override fun onCreate() {
        Logger.builder(applicationContext)
            .setLogWriter(LogTextFileWriter(Logger.logFileConfig, true))
            .setLogPublisher(ShareIntentLogPublisher(applicationContext))
            .addLogTransformer(TimeTransformer())
            .addLogTransformer(DebugLevelTransformer())
            .addLogTransformer(MessageTransformer())
            .setUncaughtExceptionHooker(
                DefaultUncaughtExceptionHooker(
                    applicationContext,
                    UncaughtExceptionAction.PublishAction
                )
            )
            .build()
        super.onCreate()

        Timber.plant(Timber.DebugTree())
    }
}