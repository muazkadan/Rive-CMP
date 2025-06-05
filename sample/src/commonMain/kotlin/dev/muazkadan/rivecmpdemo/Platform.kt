package dev.muazkadan.rivecmpdemo

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform