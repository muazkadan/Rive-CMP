package dev.muazkadan.rivecmp.native

import dev.muazkadan.rivecmp.RiveDesktop
import dev.muazkadan.rivecmp.native.model.PixelFormat
import java.nio.ByteBuffer
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertFailsWith

/**
 * Renders an artboard into caller-owned pixel buffers and checks the bytes the native bridge
 * writes. A bridge that assumes the channel order or the row stride, instead of taking them from
 * the caller, still loads, imports files and draws, so only the pixels show it.
 *
 * The artboard is 500 x 500: a red square in the middle and a blue one in the top left corner,
 * over an opaque grey background.
 */
class RendererPixelBufferTest {

    private lateinit var file: File
    private lateinit var controller: RiveFileController

    @BeforeTest
    fun loadArtboard() {
        RiveDesktop.init()
        val bytes = requireNotNull(javaClass.classLoader.getResource("data_binding_test_triggers.riv")) {
            "Test asset 'data_binding_test_triggers.riv' not found in resources"
        }.readBytes()
        file = File(bytes)
        controller = RiveFileController(autoplay = true, stateMachineName = null, file = file)
    }

    @AfterTest
    fun releaseArtboard() {
        controller.dispose()
        file.release()
    }

    @Test
    fun writesRedGreenBlueAlphaIntoAnRgbaBuffer() {
        val pixels = render(PixelFormat.RGBA_8888)

        assertContentEquals(listOf(255, 0, 0, 255), pixels.pixelAt(RED_SQUARE))
        assertContentEquals(listOf(50, 0, 255, 255), pixels.pixelAt(BLUE_SQUARE))
    }

    @Test
    fun writesBlueGreenRedAlphaIntoABgraBuffer() {
        val pixels = render(PixelFormat.BGRA_8888)

        assertContentEquals(listOf(0, 0, 255, 255), pixels.pixelAt(RED_SQUARE))
        assertContentEquals(listOf(255, 0, 50, 255), pixels.pixelAt(BLUE_SQUARE))
    }

    @Test
    fun writesRowsAtTheDeclaredStride() {
        val padding = 24
        val paddedRowBytes = TIGHT_ROW_BYTES + padding
        val tight = render(PixelFormat.RGBA_8888)
        val padded = render(PixelFormat.RGBA_8888, rowBytes = paddedRowBytes)

        for (y in 0 until SIZE) {
            assertContentEquals(
                tight.bytes(y * TIGHT_ROW_BYTES, TIGHT_ROW_BYTES),
                padded.bytes(y * paddedRowBytes, TIGHT_ROW_BYTES),
                "Row $y of the padded buffer",
            )
        }
        // The buffer ends with the last row's pixels, so only the rows before it have padding
        for (y in 0 until SIZE - 1) {
            assertContentEquals(
                ByteArray(padding) { UNTOUCHED },
                padded.bytes(y * paddedRowBytes + TIGHT_ROW_BYTES, padding),
                "The padding after row $y",
            )
        }
    }

    @Test
    fun rejectsARowTooShortForItsPixels() {
        val buffer = ByteBuffer.allocateDirect(TIGHT_ROW_BYTES * SIZE)

        assertFailsWith<IllegalArgumentException> {
            controller.artboardRenderer.setBuffer(buffer, SIZE, SIZE, TIGHT_ROW_BYTES - 1, PixelFormat.RGBA_8888)
        }
    }

    @Test
    fun rejectsABufferTooSmallForItsRows() {
        val buffer = ByteBuffer.allocateDirect(TIGHT_ROW_BYTES * SIZE - 1)

        assertFailsWith<IllegalArgumentException> {
            controller.artboardRenderer.setBuffer(buffer, SIZE, SIZE, TIGHT_ROW_BYTES, PixelFormat.RGBA_8888)
        }
    }

    @Test
    fun rejectsABufferTheNativeSideCannotAddress() {
        val buffer = ByteBuffer.allocate(TIGHT_ROW_BYTES * SIZE)

        assertFailsWith<IllegalArgumentException> {
            controller.artboardRenderer.setBuffer(buffer, SIZE, SIZE, TIGHT_ROW_BYTES, PixelFormat.RGBA_8888)
        }
    }

    /** Draws one frame of the artboard at its own size into a new buffer filled with [UNTOUCHED]. */
    private fun render(pixelFormat: PixelFormat, rowBytes: Int = TIGHT_ROW_BYTES): ByteBuffer {
        val buffer = ByteBuffer.allocateDirect(rowBytes * (SIZE - 1) + TIGHT_ROW_BYTES)
        repeat(buffer.capacity()) { buffer.put(it, UNTOUCHED) }
        controller.artboardRenderer.setBuffer(buffer, SIZE, SIZE, rowBytes, pixelFormat)
        controller.artboardRenderer.doFrame(0f)
        return buffer
    }

    /** The four bytes of the pixel at [point] in a buffer without row padding, as unsigned values. */
    private fun ByteBuffer.pixelAt(point: Pair<Int, Int>): List<Int> {
        val (x, y) = point
        return bytes(y * TIGHT_ROW_BYTES + x * BYTES_PER_PIXEL, BYTES_PER_PIXEL).map { it.toInt() and 0xFF }
    }

    private fun ByteBuffer.bytes(offset: Int, length: Int): ByteArray =
        ByteArray(length) { get(offset + it) }

    private companion object {
        const val SIZE = 500
        const val BYTES_PER_PIXEL = 4
        const val TIGHT_ROW_BYTES = SIZE * BYTES_PER_PIXEL

        /** A value no pixel of the artboard has in any channel, to tell written bytes from untouched ones. */
        const val UNTOUCHED: Byte = 0x5A

        val RED_SQUARE = 250 to 250
        val BLUE_SQUARE = 50 to 50
    }
}
