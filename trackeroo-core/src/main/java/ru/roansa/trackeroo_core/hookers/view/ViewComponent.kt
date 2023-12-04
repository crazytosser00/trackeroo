package ru.roansa.trackeroo_core.hookers.view

import java.lang.ref.WeakReference
import java.util.Objects

internal class ViewComponent(
    val viewRef: WeakReference<Any>,
    val className: String?= null,
    val resourceName: String?= null,
    val tag: String? = null
) {

    val identifier: String get() = resourceName ?: tag ?: UNKNOWN

    override fun equals(other: Any?): Boolean = when {
        other === this -> true
        other == null || other !is ViewComponent -> false
        else -> other.className == this.className
                && other.resourceName == this.resourceName
                && other.tag == this.tag
    }

    override fun hashCode(): Int = Objects.hash(className, resourceName, tag)

    companion object {
        const val UNKNOWN = "unknown"
    }
}

enum class ViewType {
    CLICKABLE, SCROLLABLE
}

fun ViewType.isClickable(): Boolean = this == ViewType.CLICKABLE
fun ViewType.isScrollable(): Boolean = this == ViewType.SCROLLABLE