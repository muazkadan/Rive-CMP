package dev.muazkadan.rivecmp.native

public object Rive {

    private external fun cppInitialize()

    private const val RIVE_DESKTOP = "rive-desktop"

    private val initialized by lazy {
        RiveLog.i("Rive", "Initializing Rive runtime")
        NativeLoader.loadLibraryFromJar(RIVE_DESKTOP)
        cppInitialize()
    }

    /**
     * Initialises Rive.
     *
     * This loads the C++ libraries required to use Rive objects and then makes sure to initialize
     * the C++ environment. Safe to call repeatedly - the actual work only runs once.
     */
    public fun init() {
        initialized
    }
}
