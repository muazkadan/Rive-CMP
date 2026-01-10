package dev.muazkadan.rivecmpdemo

actual fun getPlatform(): Platform {
    return object : Platform {
        override val name: String = "JS"
    }
}