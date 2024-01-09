package ru.roansa.trackeroo_core.hookers.view

import android.annotation.SuppressLint
import android.content.Context
import android.view.*
import android.view.accessibility.AccessibilityEvent
import androidx.core.view.GestureDetectorCompat
import ru.roansa.trackeroo_core.logging.Logger

internal class TrackerooWindowCallback(
    val delegate: Window.Callback,
    private val gestureListener: TrackerooGestureListener,
    private val context: Context,
    private val gestureDetector: GestureDetectorCompat =
        GestureDetectorCompat(context, gestureListener)
) : Window.Callback {

    override fun dispatchTouchEvent(motionEvent: MotionEvent?): Boolean {
        if (motionEvent != null) {
            try {
                gestureDetector.onTouchEvent(motionEvent)
                val action = motionEvent.actionMasked
                if (action == MotionEvent.ACTION_UP) {
                    gestureListener.onUp(motionEvent)
                }
            } catch (t: Throwable) {
                Logger.e(Logger.TAG, "There is exception during touch event dispatching", t)
            }
        }

        return delegate.dispatchTouchEvent(motionEvent)
    }

    /**
     * Default method realizations below
     */

    override fun dispatchKeyEvent(keyEvent: KeyEvent?): Boolean {
        return delegate.dispatchKeyEvent(keyEvent)
    }

    override fun dispatchKeyShortcutEvent(keyEvent: KeyEvent?): Boolean {
        return delegate.dispatchKeyShortcutEvent(keyEvent)
    }

    override fun dispatchTrackballEvent(motionEvent: MotionEvent?): Boolean {
        return delegate.dispatchTrackballEvent(motionEvent)
    }

    override fun dispatchGenericMotionEvent(motionEvent: MotionEvent?): Boolean {
        return delegate.dispatchGenericMotionEvent(motionEvent)
    }

    override fun dispatchPopulateAccessibilityEvent(accessibilityEvent: AccessibilityEvent?): Boolean {
        return delegate.dispatchPopulateAccessibilityEvent(accessibilityEvent)
    }

    override fun onCreatePanelView(i: Int): View? {
        return delegate.onCreatePanelView(i)
    }

    override fun onCreatePanelMenu(i: Int, menu: Menu): Boolean {
        return delegate.onCreatePanelMenu(i, menu)
    }

    override fun onPreparePanel(i: Int, view: View?, menu: Menu): Boolean {
        return delegate.onPreparePanel(i, view, menu)
    }

    override fun onMenuOpened(i: Int, menu: Menu): Boolean {
        return delegate.onMenuOpened(i, menu)
    }

    override fun onMenuItemSelected(i: Int, menuItem: MenuItem): Boolean {
        return delegate.onMenuItemSelected(i, menuItem)
    }

    override fun onWindowAttributesChanged(layoutParams: WindowManager.LayoutParams?) {
        delegate.onWindowAttributesChanged(layoutParams)
    }

    override fun onContentChanged() {
        delegate.onContentChanged()
    }

    override fun onWindowFocusChanged(b: Boolean) {
        delegate.onWindowFocusChanged(b)
    }

    override fun onAttachedToWindow() {
        delegate.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        delegate.onDetachedFromWindow()
    }

    override fun onPanelClosed(i: Int, menu: Menu) {
        delegate.onPanelClosed(i, menu)
    }

    override fun onSearchRequested(): Boolean {
        return delegate.onSearchRequested()
    }

    @SuppressLint("NewApi")
    override fun onSearchRequested(searchEvent: SearchEvent?): Boolean {
        return delegate.onSearchRequested(searchEvent)
    }

    override fun onWindowStartingActionMode(callback: ActionMode.Callback?): ActionMode? {
        return delegate.onWindowStartingActionMode(callback)
    }

    @SuppressLint("NewApi")
    override fun onWindowStartingActionMode(callback: ActionMode.Callback?, i: Int): ActionMode? {
        return delegate.onWindowStartingActionMode(callback, i)
    }

    override fun onActionModeStarted(actionMode: ActionMode?) {
        delegate.onActionModeStarted(actionMode)
    }

    override fun onActionModeFinished(actionMode: ActionMode?) {
        delegate.onActionModeFinished(actionMode)
    }
}