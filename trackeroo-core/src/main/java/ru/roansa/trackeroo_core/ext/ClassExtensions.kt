package ru.roansa.trackeroo_core.ext

import ru.roansa.trackeroo_core.logging.Logger

/**
 * Method tries to load a class via reflection
 *
 *  @param clazz – full class name
 *  @return a Class<?> if it's available, or null
 */
fun loadClass(clazz: String): Class<*>? {
    return try {
        Class.forName(clazz)
    } catch (ex: ClassCastException) {
        Logger.e(Logger.TAG, "Class not available", ex)
        null
    }
    catch (ex: UnsatisfiedLinkError) {
        Logger.e(Logger.TAG, "Failed to load class $clazz – UnsatisfiedLinkError", ex)
        null
    }
    catch (t: Throwable) {
        Logger.e(Logger.TAG, "Failed to init", t)
        null
    }
}

/**
 * Method tries to load class via reflection and check his availability
 * @param clazz – full class name
 * @return true if class is available via reflection
 */
fun isClassAvailable(clazz: String): Boolean = loadClass(clazz) != null