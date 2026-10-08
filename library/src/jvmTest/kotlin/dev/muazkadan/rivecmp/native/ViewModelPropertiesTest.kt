package dev.muazkadan.rivecmp.native

import dev.muazkadan.rivecmp.RiveDesktop
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotSame
import kotlin.test.assertSame

/**
 * Reads, writes and observes each view model property type through the JNI bridge, using a view
 * model instance that is not bound to any artboard.
 */
class ViewModelPropertiesTest {

    private lateinit var file: File
    private lateinit var instance: ViewModelInstance

    @BeforeTest
    fun loadInstance() {
        RiveDesktop.init()
        val bytes = requireNotNull(javaClass.classLoader.getResource("viewmodel_runtime_file.riv")) {
            "Test asset 'viewmodel_runtime_file.riv' not found in resources"
        }.readBytes()
        file = File(bytes)
        instance = file.getViewModelByName("vm").createDefaultInstance()
    }

    @AfterTest
    fun releaseFile() {
        file.release()
    }

    @Test
    fun numberRoundTrips() {
        val number = instance.getNumberProperty("num")
        number.value = 12.5f
        assertEquals(12.5f, number.value)
        assertEquals(12.5f, number.valueFlow.value)
    }

    @Test
    fun stringRoundTrips() {
        val string = instance.getStringProperty("str")
        string.value = "héllo"
        assertEquals("héllo", string.value)
        assertEquals("héllo", string.valueFlow.value)
    }

    @Test
    fun booleanRoundTrips() {
        val boolean = instance.getBooleanProperty("boo")
        boolean.value = !boolean.value
        val written = boolean.value
        boolean.value = !written
        assertEquals(!written, boolean.value)
        assertEquals(!written, boolean.valueFlow.value)
    }

    @Test
    fun colorRoundTripsAsArgb() {
        val color = instance.getColorProperty("col")
        val argb = 0x80FF4020.toInt()
        color.value = argb
        assertEquals(argb, color.value)
        assertEquals(argb, color.valueFlow.value)
    }

    @Test
    fun enumRoundTripsByValueName() {
        val enum = instance.getEnumProperty("enu")
        enum.value = "Right align"
        assertEquals("Right align", enum.value)
        enum.value = "Center align"
        assertEquals("Center align", enum.valueFlow.value)
    }

    @Test
    fun nestedPathReachesTheChildViewModel() {
        val nested = instance.getNumberProperty("chi/chi-num")
        nested.value = 7f
        assertEquals(7f, nested.value)
    }

    @Test
    fun theSamePathReturnsTheSameProperty() {
        assertSame(instance.getNumberProperty("num"), instance.getNumberProperty("num"))
    }

    @Test
    fun aFiredTriggerReachesTheFlowWhenPolled() {
        val trigger = instance.getTriggerProperty("tri")
        val before = trigger.valueFlow.value
        trigger.trigger()
        instance.pollChanges()
        assertNotSame(before, trigger.valueFlow.value, "Expected the trigger to report a new firing")
    }

    @Test
    fun aMissingPathFails() {
        assertFailsWith<ViewModelException> { instance.getNumberProperty("missing") }
        assertFailsWith<ViewModelException> { instance.getNumberProperty("chi/missing") }
        assertFailsWith<ViewModelException> { instance.getNumberProperty("missing/chi-num") }
    }

    @Test
    fun aPathOfAnotherTypeFails() {
        assertFailsWith<ViewModelException> { instance.getNumberProperty("str") }
        assertFailsWith<ViewModelException> { instance.getStringProperty("num") }
        assertFailsWith<ViewModelException> { instance.getTriggerProperty("boo") }
    }

    @Test
    fun anUnknownViewModelFails() {
        assertFailsWith<ViewModelException> { file.getViewModelByName("nope") }
    }
}
