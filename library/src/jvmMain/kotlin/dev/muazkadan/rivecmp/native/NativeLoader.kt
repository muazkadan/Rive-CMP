package dev.muazkadan.rivecmp.native

import java.io.File
import java.nio.file.FileSystems
import java.util.Locale

public object NativeLoader {

    private val suffix: String
    private val extension: String

    init {
        val os = System.getProperty("os.name", "unknown").lowercase(Locale.ENGLISH)
        val arch = System.getProperty("os.arch", "unknown").lowercase(Locale.ENGLISH)

        suffix = when {
            "aarch64" in arch -> "arm64"
            "arm" in arch -> "arm"
            "64" in arch -> "x64"
            "86" in arch || "32" in arch -> "x86"
            else -> error("CPU architecture not supported. Current CPU architecture: $arch")
        }

        extension = when {
            "mac" in os || "darwin" in os -> "dylib"
            "win" in os -> "dll"
            "nux" in os || "nix" in os || "aix" in os -> "so"
            else -> error("OS not supported. Current OS: $os")
        }
    }

    @Suppress("UnsafeDynamicallyLoadedCode")
    @Throws(IllegalStateException::class)
    public fun loadLibraryFromJar(baseName: String) {
        val nativeLibFileName = "lib$baseName$suffix.$extension"

        val tempDir = java.nio.file.Files.createTempDirectory("$baseName-runtime").toFile()
            .apply { deleteOnExit() }
        val extractionFile = tempDir.resolve(nativeLibFileName)

        checkNotNull(
                javaClass.classLoader.getResourceAsStream(nativeLibFileName)?.use { input ->
                    extractionFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                },
        ) { "Native libraries were not found in the Jar '$nativeLibFileName'" }

        try {
            System.load(extractionFile.absolutePath)
        }
        finally {
            if ("posix" in FileSystems.getDefault().supportedFileAttributeViews()) extractionFile.delete()
            else extractionFile.deleteOnExit()
        }
    }
}
