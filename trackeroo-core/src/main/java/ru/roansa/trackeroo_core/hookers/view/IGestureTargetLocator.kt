package ru.roansa.trackeroo_core.hookers.view

internal interface IGestureTargetLocator {
    fun findTarget(root: Any, x: Float, y: Float, type: ViewType): ViewComponent?
}