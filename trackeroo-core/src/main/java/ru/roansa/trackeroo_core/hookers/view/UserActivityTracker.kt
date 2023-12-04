package ru.roansa.trackeroo_core.hookers.view

import android.app.Activity
import android.app.Application
import android.os.Bundle
import ru.roansa.trackeroo_core.ext.isClassAvailable
import ru.roansa.trackeroo_core.logging.Logger

class UserActivityTracker(private val application: Application) :
    Application.ActivityLifecycleCallbacks {
    private val isAndroidXAvailable: Boolean =
        isClassAvailable("androidx.core.view.GestureDetectorCompat")

    private fun startTracking(activity: Activity) {
        activity.window?.let {
            val delegate = it.callback ?: return
            val gestureListener = TrackerooGestureListener(activity)
            it.callback = TrackerooWindowCallback(delegate, gestureListener, activity)
        }
    }

    private fun stopTracking(activity: Activity) {
        activity.window?.let {
            val currentCallback = it.callback
            if (currentCallback is TrackerooWindowCallback) {
                it.callback = currentCallback.delegate
            }
        }
    }

    fun bound() {
        if (isAndroidXAvailable) {
            application.registerActivityLifecycleCallbacks(this)
        } else {
            Logger.e(
                Logger.TAG,
                "androidx.core is not available, UserActivityTracker cannot be installed"
            )
        }
    }

    fun unbound() {
        application.unregisterActivityLifecycleCallbacks(this)
    }

    override fun onActivityResumed(currentActivity: Activity) {
        startTracking(currentActivity)
    }

    override fun onActivityPaused(currentActivity: Activity) {
        stopTracking(currentActivity)
    }

    //No need in methods below
    override fun onActivityCreated(p0: Activity, p1: Bundle?) {}
    override fun onActivityStarted(p0: Activity) {}
    override fun onActivityStopped(p0: Activity) {}
    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {}
    override fun onActivityDestroyed(p0: Activity) {}
}