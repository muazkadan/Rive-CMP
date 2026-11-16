package dev.muazkadan.rivecmp.native

import java.io.File
import java.io.IOException
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
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
        val library = checkNotNull(
            javaClass.classLoader.getResourceAsStream(nativeLibFileName)?.use { it.readBytes() },
        ) { "Native libraries were not found in the Jar '$nativeLibFileName'" }

        // Windows, where the copy made below could not be deleted once it is loaded
        if ("posix" !in FileSystems.getDefault().supportedFileAttributeViews()) {
            val copies = reusableCopies(System.getenv("LOCALAPPDATA"), System.getProperty("user.home"))
            System.load(reusableCopy(nativeLibFileName, library, copies).absolutePath)
            return
        }

        val tempDir = java.nio.file.Files.createTempDirectory("$baseName-runtime").toFile()
            .apply { deleteOnExit() }
        val extractionFile = tempDir.resolve(nativeLibFileName)
        extractionFile.writeBytes(library)

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
        val copy = root.resolve("rive-desktop-runtime-$digest").resolve(name)
        if (!copy.holds(library)) place(library, copy)
        return copy
    }

    /**
     * The directory the copies that outlive a run are kept in: the user's own application data,
     * [localAppData], or their home directory, [userHome], when that is not known. Unlike the
     * temp directory, which several users can share, neither can be written to by someone else,
     * who could otherwise swap the library for their own before it is loaded.
     */
    internal fun reusableCopies(localAppData: String?, userHome: String): File =
        File(localAppData ?: userHome, "rive-cmp")

    /**
     * Puts [library] at [copy]. It is written aside and moved into place in one step, so another
     * process finds either a whole library there or none, and one that is there stays there.
     *
     * A process that started at the same time may get there first and have its copy loaded,
     * after which it cannot be replaced. That copy is as good, so only a [copy] that ends up
     * without the content is a failure.
     */
    @Throws(IllegalStateException::class)
    internal fun place(library: ByteArray, copy: File) {
        var partial: Path? = null
        try {
            copy.parentFile.mkdirs()
            partial = Files.createTempFile(copy.parentFile.toPath(), copy.name, ".partial")
            Files.write(partial, library)
            Files.move(partial, copy.toPath(), StandardCopyOption.ATOMIC_MOVE)
        } catch (e: IOException) {
            if (!copy.holds(library)) {
                throw IllegalStateException("Could not write the native library to '$copy'", e)
            }
        } finally {
            partial?.let(Files::deleteIfExists)
        }
    }

    private fun File.holds(content: ByteArray): Boolean = isFile && readBytes().contentEquals(content)
}
