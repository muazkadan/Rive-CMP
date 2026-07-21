package dev.muazkadan.rivecmpdemo

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dev.muazkadan.rivecmp.RiveDesktop

fun main() {
    RiveDesktop.init()
    application {
        Window(onCloseRequest = ::exitApplication, title = "Rive CMP Demo") {
            App()
        }
    }
}
