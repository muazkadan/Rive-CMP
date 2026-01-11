package dev.muazkadan.rivecmp

@JsModule("@rive-app/canvas")
@JsNonModule
external object RiveSDK {
    class Rive(options: dynamic) {
        fun play()
        fun pause()
        fun stop()
        fun stateMachineInputs(name: String): Array<dynamic>
        fun cleanup()
        fun resizeToCanvas()
        fun resizeDrawingSurfaceToCanvas()
    }
    object Fit {
        val Cover: dynamic
        val Contain: dynamic
        val Fill: dynamic
        val FitWidth: dynamic
        val FitHeight: dynamic
        val None: dynamic
        val ScaleDown: dynamic
    }
    object Alignment {
        val Center: dynamic
        val TopLeft: dynamic
        val TopCenter: dynamic
        val TopRight: dynamic
        val CenterLeft: dynamic
        val CenterRight: dynamic
        val BottomLeft: dynamic
        val BottomCenter: dynamic
        val BottomRight: dynamic
    }
    class Layout(options: dynamic = definedExternally)
}