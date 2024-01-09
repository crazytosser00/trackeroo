package ru.roansa.trackeroo_core.hookers

import android.app.Application
import android.content.Context
import android.content.res.Resources.NotFoundException
import android.view.View
import android.view.View.NO_ID
import ru.roansa.trackeroo_core.const.TagConst
import ru.roansa.trackeroo_core.const.ViewParamsConst
import ru.roansa.trackeroo_core.hookers.view.IGestureTargetLocator
import ru.roansa.trackeroo_core.hookers.view.UserActivityTracker
import ru.roansa.trackeroo_core.hookers.view.android.AndroidGestureTargetLocator
import ru.roansa.trackeroo_core.logging.LogEntity
import ru.roansa.trackeroo_core.logging.LogLevel
import ru.roansa.trackeroo_core.logging.Logger

// With blackjack! A-a-and hookers!
internal object ViewHooker {

    const val TAG = "Trackeroo-ViewHooker"

    internal lateinit var gestureTargetLocators: List<IGestureTargetLocator>
    private var userActivityTracker: UserActivityTracker? = null

    @JvmStatic
    internal fun init(context: Context) {
        gestureTargetLocators = listOf(AndroidGestureTargetLocator())
        if (context is Application) {
            userActivityTracker = UserActivityTracker(context)
            userActivityTracker?.bound()
        } else {
            Logger.e(
                TAG,
                """
                Cannot instaniate UserActivityTracker class - given context is not an Application. 
                Make sure that Trackeroo Logger getting application context.
            """.trimIndent()
            )
        }
    }

    @JvmStatic
    internal fun onViewClick(view: View, pointX: Float, pointY: Float) {
        Logger.log(
            LogEntity(
                level = LogLevel.DEBUG,
                tag = TagConst.VIEW_CLICKED,
                message = "view id = ${getViewId(view)}, point = [x:$pointX,y:$pointY]",
                parameters = hashMapOf(
                    ViewParamsConst.POINT_X to pointX,
                    ViewParamsConst.POINT_Y to pointY
                )
            )
        )
    }

    @JvmStatic
    internal fun onViewScroll(
        view: View,
        direction: String,
        pointX: Float,
        pointY: Float,
        distanceX: Float,
        distanceY: Float
    ) {
        val message = """
            view id = ${getViewId(view)}, direction = $direction, start point = [x:$pointX,y:$pointY],
             scroll distance = [x:$distanceX,y:$distanceY]
        """.trimIndent()
        Logger.log(
            LogEntity(
                level = LogLevel.DEBUG,
                tag = TagConst.VIEW_SCROLLED,
                message = message,
                parameters = hashMapOf(
                    ViewParamsConst.POINT_X to pointX,
                    ViewParamsConst.POINT_Y to pointY,
                    ViewParamsConst.DISTANCE_X to distanceX,
                    ViewParamsConst.DISTANCE_Y to distanceY
                )
            )
        )
    }

    @JvmStatic
    internal fun onViewSwipe(view: View, direction: String) {
        Logger.d(TagConst.VIEW_SWIPED, "view id = ${getViewId(view)}, direction = $direction")
    }

    private fun getViewId(view: View): String = try {
        if (view.id == NO_ID) "undefined"
        else view.resources.getResourceName(view.id)
    } catch (e: NotFoundException) {
        "undefined"
    }

}