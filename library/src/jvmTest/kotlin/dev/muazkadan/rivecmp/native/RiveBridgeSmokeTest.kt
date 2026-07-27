package dev.muazkadan.rivecmp.native

import dev.muazkadan.rivecmp.RiveDesktop
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Exercises the JNI bridge end-to-end: loads the native library, imports a real .riv file, and
 * reads its artboard/state-machine metadata back out. Catches native init and JNI symbol
 * mismatches that unit tests over pure-Kotlin code wouldn't.
 */
class RiveBridgeSmokeTest {

    @Test
    fun importsAndInspectsARealRivFile() {
        RiveDesktop.init()
        val url = requireNotNull(javaClass.classLoader.getResource("mode_switch.riv")) {
            "Test asset 'mode_switch.riv' not found in resources"
        }
        val bytes = url.readBytes()

        val file = File(bytes)
        try {
            assertTrue(file.artboardCount > 0, "Expected at least one artboard")

            val artboard = file.firstArtboard
            assertTrue(artboard.name.isNotEmpty(), "Expected artboard to have a name")
        } finally {
            file.release()
        }
    }
}
