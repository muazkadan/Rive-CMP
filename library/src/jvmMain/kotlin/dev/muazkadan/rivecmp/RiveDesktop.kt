package dev.muazkadan.rivecmp

import dev.muazkadan.rivecmp.native.Rive

/**
 * Initializes the Rive JVM/Desktop native runtime: loads the JNI bridge to rive-runtime and sets
 * up the C++ environment.
 *
 * Call this once, before the first [CustomRiveAnimation] or [RiveComposition] is used - typically
 * at application startup (e.g. the top of `fun main()`).
 *
 * Android auto-initializes via `androidx.startup`, and iOS/JS/wasmJs need no initialization at
 * all - JVM/Desktop has no equivalent automatic startup hook, so this call is required here
 * specifically. Safe to call more than once; only the first call does any work.
 */
public object RiveDesktop {
    public fun init(): Unit = Rive.init()
}
