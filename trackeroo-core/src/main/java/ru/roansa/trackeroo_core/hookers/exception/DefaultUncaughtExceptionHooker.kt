package ru.roansa.trackeroo_core.hookers.exception

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import ru.roansa.trackeroo_core.logging.Logger

open class DefaultUncaughtExceptionHooker(
    private val context: Context,
    private val actionOnException: UncaughtExceptionAction
) : ExceptionHooker, Thread.UncaughtExceptionHandler {

    private val lastExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(thread: Thread, exception: Throwable) {
        /**
         * If StartIntentAction selected then default crash dialog will not be showing
         * In other cases that dialog will be shown
         */
        Logger.e("DefaultUncaughtExceptionHooker", "App was crashing with some exception", exception)
        when (actionOnException) {
            is UncaughtExceptionAction.StartActivityAction -> {
                context.startActivity(
                    Intent().setComponent(
                        ComponentName(
                            actionOnException.appPackageName,
                            actionOnException.activityWithPackageName
                        )
                    ).addFlags(FLAG_ACTIVITY_NEW_TASK)
                )
            }

            // TODO Need to check if background work will be aborted after the app was closed
            is UncaughtExceptionAction.PublishAction -> {
                Logger.publish()
                lastExceptionHandler?.uncaughtException(thread, exception)
            }

            is UncaughtExceptionAction.CustomAction -> {
                lastExceptionHandler?.uncaughtException(thread, exception)
                actionOnException.block(thread, exception)
            }
        }
    }
}

sealed class UncaughtExceptionAction {
    /**
     * Action to start some Activity with info about crash (i.e. to send bug report from user)
     * @param appPackageName – package name of application, same as "package" attribute in manifest
     * of app module (i.e. com.my.application)
     * @param activityWithPackageName – package name with Activity name that should be opened. It
     * can be only from current project module, not from 3rd party library or outside app package
     * (i.e. com.my.application.widget.BugReportActivity)
     */
    class StartActivityAction(val appPackageName: String, val activityWithPackageName: String) :
        UncaughtExceptionAction()

    object PublishAction : UncaughtExceptionAction()

    /**
     * Action with listener to custom actions
     */
    class CustomAction(val block: (Thread, Throwable) -> Unit) : UncaughtExceptionAction()
}