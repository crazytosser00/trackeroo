package ru.roansa.trackeroo

import android.view.View

/**
 * Глобальная переменная для хранения времени последнего клика.
 * Позволяет пропускать клики произведенные в слишком короткий
 * интервал не только по одной вью, но по всем вью на экране.
 */
private var lastClickTimeStamp: Long = System.currentTimeMillis()
private const val THROTTLE_FIRST_INTERVAL_MS = 300L

fun View.setThrottledClickListener(interval: Long = THROTTLE_FIRST_INTERVAL_MS, listener: () -> Unit) {
    setOnClickListener {
        val currentClickTimeStamp = System.currentTimeMillis()
        if (currentClickTimeStamp - lastClickTimeStamp >= interval) {
            listener()
        }
        lastClickTimeStamp = currentClickTimeStamp
    }
}