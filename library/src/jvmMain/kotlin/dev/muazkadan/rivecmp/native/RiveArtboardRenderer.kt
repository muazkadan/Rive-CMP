package dev.muazkadan.rivecmp.native

public open class RiveArtboardRenderer(private val controller: RiveFileController) : Renderer() {

    private val fit get() = controller.fit
    private val alignment get() = controller.alignment
    private val scaleFactor get() = controller.layoutScaleFactorActive

    init {
        RiveLog.d(TAG, "Initializing.")
    }

    override fun draw() {
        // Early out for deleted renderer or inactive controller.
        if (!hasCppObject) return
        controller.artboard.draw(cppPointer, fit, alignment, scaleFactor)
    }

    override fun advance(elapsed: Float) {
        if (!hasCppObject) return
        controller.advance(elapsed)
        // Don't stop if we're queueing more inputs.
        // Are we done playing?
        if (!controller.isAdvancing) stop()
    }

    public fun reset() {
        RiveLog.d(TAG, "Reset.")
        controller.stopAnimations()
        controller.reset()
        stop()
        controller.selectArtboard()
        start()
    }

    public companion object {
        public const val TAG: String = "RiveL/RiveArtboardRenderer"
    }
}
