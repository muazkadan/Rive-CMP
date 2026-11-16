package dev.muazkadan.rivecmp.native

import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
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

        // Windows, where the copy made below could not be deleted once it is loaded
        if ("posix" !in FileSystems.getDefault().supportedFileAttributeViews()) {
            val library = checkNotNull(
                javaClass.classLoader.getResourceAsStream(nativeLibFileName)?.use { it.readBytes() },
            ) { "Native libraries were not found in the Jar '$nativeLibFileName'" }
            val copies = File(System.getProperty("java.io.tmpdir"))
            System.load(reusableCopy(nativeLibFileName, library, copies).absolutePath)
            return
        }

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
            extractionFile.delete()
        }
    }

    /**
     * Returns a copy of [library] named [name] in a directory under [root] that is named after
     * the library's content, writing it only if that copy is not already there.
     *
     * A loaded library cannot be deleted on Windows, so a copy made for one run would stay behind
     * after every run. A copy that later runs find again and reuse stays behind once.
     */
    internal fun reusableCopy(name: String, library: ByteArray, root: File): File {
        val digest = MessageDigest.getInstance("SHA-256").digest(library)
            .joinToString("") { "%02x".format(it) }
        val directory = root.resolve("rive-desktop-runtime-$digest").apply { mkdirs() }
        val copy = directory.resolve(name)
        if (!copy.isFile || !copy.readBytes().contentEquals(library)) {
            // Written aside and moved into place, so another process never loads half a library
            val partial = Files.createTempFile(directory.toPath(), name, ".partial")
            Files.write(partial, library)
            Files.move(partial, copy.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }
        return copy
    }
}
