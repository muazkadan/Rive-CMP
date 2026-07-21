package dev.muazkadan.rivecmp.native

import java.lang.System.Logger.Level

private val platformLogger = System.getLogger("Rive")

/**
 * JNI bridge methods that take a String directly instead of a lambda. These are used by the C++
 * helper for efficient logging from native code, and by the Kotlin side for its own logging.
 */
public object RiveLog {

    @JvmStatic
    @Suppress("UNUSED") // Used by native code
    public fun v(tag: String, msg: String): Unit = log(Level.TRACE, tag, msg)

    @JvmStatic
    @Suppress("UNUSED") // Used by native code
    public fun d(tag: String, msg: String): Unit = log(Level.DEBUG, tag, msg)

    @JvmStatic
    @Suppress("UNUSED") // Used by native code
    public fun i(tag: String, msg: String): Unit = log(Level.INFO, tag, msg)

    @JvmStatic
    @Suppress("UNUSED") // Used by native code
    public fun w(tag: String, msg: String): Unit = log(Level.WARNING, tag, msg)

    @JvmStatic
    @Suppress("UNUSED") // Used by native code
    public fun e(tag: String, msg: String): Unit = log(Level.ERROR, tag, msg)

    private fun log(level: Level, tag: String, msg: String) {
        if (platformLogger.isLoggable(level)) platformLogger.log(level, "[$tag] $msg")
    }
}
