package ru.roansa.trackeroo_core.hookers.view

import android.app.Activity
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import ru.roansa.trackeroo_core.ext.findTarget
import ru.roansa.trackeroo_core.hookers.ViewHooker
import ru.roansa.trackeroo_core.logging.Logger
import java.lang.ref.WeakReference
import kotlin.math.abs

class TrackerooGestureListener(currentActivity: Activity) :
    GestureDetector.OnGestureListener {

    private val activityRef: WeakReference<Activity> = WeakReference(currentActivity)
    private val scrollState: ScrollState = ScrollState()

    fun onUp(event: MotionEvent) {
        val decorView = checkWindowDecorView("onUp")
        val scrollTarget = scrollState.target
        if (decorView == null || scrollTarget == null || scrollState.type == null) return

        val direction = scrollState.calculateDirection(event)
        scrollTarget.extractView()?.run {
            when (scrollState.type) {
                null -> Unit /* do nothing */
                ScrollState.Type.SCROLL -> {
                    val distX = abs(event.x - scrollState.startX)
                    val distY = abs(event.y - scrollState.startY)
                    ViewHooker.onViewScroll(
                        this,
                        direction.name,
                        scrollState.startX,
                        scrollState.startY,
                        distX,
                        distY
                    )
                }
                ScrollState.Type.SWIPE -> {
                    ViewHooker.onViewSwipe(this, direction.name)
                }
            }
        }

        scrollState.reset()
    }

    override fun onDown(event: MotionEvent): Boolean {
        event.run {
            with(scrollState) {
                reset()
                startX = this@run.x
                startY = this@run.y
            }
        }
        return false
    }

    override fun onSingleTapUp(event: MotionEvent): Boolean {
        val decorView = checkWindowDecorView("onSingleTapUp")
        if (decorView == null || event == null) return false

        val target = decorView.findTarget(event.x, event.y, ViewType.CLICKABLE) ?: return false
        target.extractView()?.run {
            ViewHooker.onViewClick(this, event.x, event.y)
        }

        return false
    }

    override fun onScroll(
        firstEvent: MotionEvent,
        currentEvent: MotionEvent,
        distX: Float,
        distY: Float
    ): Boolean {
        val decorView = checkWindowDecorView("onScroll")
        if (decorView == null || firstEvent == null) return false

        if (scrollState.type == null) {
            val target = decorView.findTarget(firstEvent.x, firstEvent.y, ViewType.SCROLLABLE)
                ?: return false

            scrollState.target = target
            scrollState.type = ScrollState.Type.SCROLL
        }

        return false
    }

    override fun onFling(p0: MotionEvent, p1: MotionEvent, p2: Float, p3: Float): Boolean {
        scrollState.type = ScrollState.Type.SWIPE
        return false
    }

    private fun checkWindowDecorView(caller: String): View? {
        val activity = activityRef.get();
        if (activity == null) {
            //TODO replace word "breadcrumb" with another that is NOT from Sentry
            Logger.e(Logger.TAG, "Activity is null in $caller. No breadcrumb captured.");
            return null;
        }

        val window = activity.window;
        if (window == null) {
            //TODO replace word "breadcrumb" with another that is NOT from Sentry
            Logger.e(Logger.TAG, "Window is null in $caller. No breadcrumb captured.");
            return null;
        }

        return window.decorView;
    }

    private fun ViewComponent.extractView(): View? {
        val view = viewRef.get()
        return if (view is View) view else null
    }


    /**
     * Unused methods
     */
    override fun onLongPress(p0: MotionEvent) {}
    override fun onShowPress(p0: MotionEvent) {}
}

/**
 * Instance of this class shouldn't re-instaniate at runtime for system resources economy
 */
internal class ScrollState(
    var target: ViewComponent? = null,
    var type: Type? = null,
    var startX: Float = 0f,
    var startY: Float = 0f
) {
    internal fun calculateDirection(endEvent: MotionEvent): Direction {
        val diffX = endEvent.x - startX
        val diffY = endEvent.y - startY
        return if (abs(diffX) > abs(diffY)) {
            if (diffX > 0f) Direction.RIGHT
            else Direction.LEFT
        } else {
            if (diffY > 0f) Direction.DOWN
            else Direction.UP
        }
    }

    internal fun reset() {
        target = null
        type = null
        startX = 0f
        startY = 0f
    }

    enum class Direction {
        RIGHT, LEFT, UP, DOWN
    }

    enum class Type {
        SCROLL, SWIPE
    }
}