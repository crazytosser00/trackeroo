package ru.roansa.trackeroo

import android.app.Application
import ru.roansa.trackeroo_core.logging.Logger
import timber.log.Timber

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Logger.default(applicationContext)

        Timber.plant(Timber.DebugTree())
    }
}