/**
 * Desktop implementation of the view model instance API over the JNI bridge's ViewModelInstance.
 * Writes notify the controller so a settled state machine resumes and applies them. Once the
 * instance is disposed with its animation, lookups find nothing and writes do nothing, so a
 * property kept past its animation never reaches freed native memory.
 */
package dev.muazkadan.rivecmp

import dev.muazkadan.rivecmp.native.ViewModelException
import dev.muazkadan.rivecmp.native.ViewModelInstance
import dev.muazkadan.rivecmp.native.ViewModelProperty
import dev.muazkadan.rivecmp.native.ViewModelTriggerProperty
import dev.muazkadan.rivecmp.utils.ExperimentalRiveCmpApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalRiveCmpApi::class)
internal class DesktopRiveViewModelInstance(
    private val instance: ViewModelInstance,
    private val onWrite: () -> Unit,
) : RiveViewModelInstance {

    override fun number(path: String): RiveProperty<Float>? = property { instance.getNumberProperty(path) }
    override fun string(path: String): RiveProperty<String>? = property { instance.getStringProperty(path) }
    override fun boolean(path: String): RiveProperty<Boolean>? = property { instance.getBooleanProperty(path) }
    override fun color(path: String): RiveProperty<Int>? = property { instance.getColorProperty(path) }
    override fun enum(path: String): RiveProperty<String>? = property { instance.getEnumProperty(path) }

    override fun trigger(path: String): RiveTrigger? =
        lookup { instance.getTriggerProperty(path) }?.let { DesktopRiveTrigger(it, ::write) }

    private fun <T> property(get: () -> ViewModelProperty<T>): RiveProperty<T>? =
        lookup(get)?.let { DesktopRiveProperty(it, ::write) }

    /** The bridge reports a missing or differently typed path by throwing. */
    private fun <P> lookup(get: () -> P): P? {
        if (!instance.hasCppObject) return null
        return try {
            get()
        } catch (e: ViewModelException) {
            null
        }
    }

    /** Runs [change] against the native property and wakes the controller, while the instance lives. */
    private fun write(change: () -> Unit) {
        if (!instance.hasCppObject) return
        change()
        onWrite()
    }
}

@OptIn(ExperimentalRiveCmpApi::class)
private class DesktopRiveProperty<T>(
    private val property: ViewModelProperty<T>,
    private val write: (change: () -> Unit) -> Unit,
) : RiveProperty<T> {
    override var value: T
        get() = property.value
        set(value) = write { property.value = value }

    override val valueFlow: StateFlow<T> get() = property.valueFlow
}

@OptIn(ExperimentalRiveCmpApi::class)
private class DesktopRiveTrigger(
    private val property: ViewModelTriggerProperty,
    private val write: (change: () -> Unit) -> Unit,
) : RiveTrigger {
    override fun trigger() = write { property.trigger() }

    // The flow starts with a placeholder firing, which is not a firing by the graphic.
    override val triggers: Flow<Unit> = property.valueFlow.drop(1).map { }
}
