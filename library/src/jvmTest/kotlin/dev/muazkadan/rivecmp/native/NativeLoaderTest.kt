package dev.muazkadan.rivecmp.native

import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * Checks the copy of the native library that is kept between runs where a loaded library cannot
 * be deleted (Windows): one copy per library content, reused by later runs instead of every run
 * leaving another one behind. The copying is plain file handling, so it is tested on every platform.
 */
class NativeLoaderTest {

    private val root: File = Files.createTempDirectory("native-loader-test").toFile()
    private val library = ByteArray(4096) { it.toByte() }

    @AfterTest
    fun deleteCopies() {
        root.deleteRecursively()
    }

    @Test
    fun copiesTheLibraryUnderItsOwnName() {
        val copy = NativeLoader.reusableCopy("librive-desktopx64.dll", library, root)

        assertEquals("librive-desktopx64.dll", copy.name)
        assertContentEquals(library, copy.readBytes())
    }

    @Test
    fun reusesTheCopyAnEarlierRunLeft() {
        val first = NativeLoader.reusableCopy("librive-desktopx64.dll", library, root)
        val longAgo = 1_000_000L
        first.setLastModified(longAgo)

        val second = NativeLoader.reusableCopy("librive-desktopx64.dll", library, root)

        assertEquals(first, second)
        assertEquals(longAgo, second.lastModified(), "The copy must not be written again")
        assertEquals(1, root.listFiles()!!.size, "Expected a single copy")
    }

    @Test
    fun keepsLibrariesWithDifferentContentApart() {
        val other = ByteArray(4096) { (it + 1).toByte() }

        val copy = NativeLoader.reusableCopy("librive-desktopx64.dll", library, root)
        val otherCopy = NativeLoader.reusableCopy("librive-desktopx64.dll", other, root)

        assertNotEquals(copy, otherCopy)
        assertContentEquals(library, copy.readBytes())
        assertContentEquals(other, otherCopy.readBytes())
    }

    @Test
    fun restoresACopyWhoseContentChanged() {
        val copy = NativeLoader.reusableCopy("librive-desktopx64.dll", library, root)
        copy.writeBytes(byteArrayOf(1, 2, 3))

        val restored = NativeLoader.reusableCopy("librive-desktopx64.dll", library, root)

        assertEquals(copy, restored)
        assertContentEquals(library, restored.readBytes())
    }
}
