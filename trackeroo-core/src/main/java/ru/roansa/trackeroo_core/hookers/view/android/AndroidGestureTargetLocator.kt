package ru.roansa.trackeroo_core.hookers.view.android

import android.content.res.Resources
import android.view.View
import ru.roansa.trackeroo_core.ext.isClassAvailable
import ru.roansa.trackeroo_core.ext.isScrollable
import ru.roansa.trackeroo_core.ext.isTouchable
import ru.roansa.trackeroo_core.ext.resourceId
import ru.roansa.trackeroo_core.hookers.view.IGestureTargetLocator
import ru.roansa.trackeroo_core.hookers.view.ViewComponent
import ru.roansa.trackeroo_core.hookers.view.ViewType
import ru.roansa.trackeroo_core.hookers.view.isClickable
import ru.roansa.trackeroo_core.hookers.view.isScrollable
import java.lang.ref.WeakReference

internal class AndroidGestureTargetLocator :
    IGestureTargetLocator {

    private val isAndroidXAvailable: Boolean = isClassAvailable("androidx.core.view.ScrollingView")
    private var coordinates = IntArray(2)

    override fun findTarget(root: Any, x: Float, y: Float, type: ViewType): ViewComponent? {
        /**
         * TODO Optimization issue
         * Method isViewAcceptable checks if view is touchable or is scrollable every time for every view.
         * But, the chance that a view can be scrollable is much lower than a chance that a view can be touchable.
         * In this case the method can be optimized by changing to if-elif construction like in
         * sentry-java.sentry-android-core.AndroidViewGestureTargetLocator.locate() (THIS IS NOT A FULL PACKAGE PATH)
         */
        fun isViewAcceptable(root: View, type: ViewType, isAndroidXAvailable: Boolean): Boolean {
            return (type.isClickable() && root.isTouchable()) ||
                    (type.isScrollable() && root.isScrollable(isAndroidXAvailable))
        }
        return when {
            root !is View -> null
            touchWithinBounds(root, x, y) &&
                    isViewAcceptable(root, type, isAndroidXAvailable) -> createViewComponent(root)
            else -> null
        }
    }

    private fun createViewComponent(view: View): ViewComponent? = try {
        val resName = view.resourceId()
        val className = view.javaClass.canonicalName ?: view.javaClass.simpleName
        ViewComponent(WeakReference(view), className, resName)
    } catch (ex: Resources.NotFoundException) {
        null
    }

    /**
     * This method returns true if touch is within view boundaries
     * @param view – a view in screen's view graph
     * @param touchX – x coordinate of user's touch
     * @param touchY – y coordinate of user's touch
     */
    private fun touchWithinBounds(view: View, touchX: Float, touchY: Float): Boolean {
        view.getLocationOnScreen(coordinates)
        val (viewX, viewY) = coordinates

        /**
         * This variant can be changed to
         * (touchX > viewX) && (touchX < viewX + view.width) && (touchY > viewY) && (touchY < viewY + view.height)
         * But OR form has better performance because there is no need to execute all logical expressions
         */
        return !(touchX < viewX || touchX > viewX + view.width || touchY < viewY || touchY > viewY + view.height)
    }
}