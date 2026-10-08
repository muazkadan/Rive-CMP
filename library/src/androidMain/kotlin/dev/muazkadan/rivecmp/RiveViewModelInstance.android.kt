/**
 * Android implementation of the view model instance API over rive-android's ViewModelInstance, and
 * the helper that hands the bound instance to the caller. Writes wake rive-android's renderer, which
 * stops once the state machine settles. Once the instance is disposed with its view, lookups find
 * nothing and writes do nothing.
 */
package dev.muazkadan.rivecmp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import app.rive.runtime.kotlin.RiveAnimationView
import app.rive.runtime.kotlin.core.ViewModelInstance
import app.rive.runtime.kotlin.core.ViewModelProperty
import app.rive.runtime.kotlin.core.ViewModelTriggerProperty
import app.rive.runtime.kotlin.core.errors.RiveException
import app.rive.runtime.kotlin.core.errors.ViewModelException
import dev.muazkadan.rivecmp.utils.ExperimentalRiveCmpApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalRiveCmpApi::class)
internal class AndroidRiveViewModelInstance(
    private val instance: ViewModelInstance,
    private val onWrite: () -> Unit,
) : RiveViewModelInstance {

    override fun number(path: String): RiveProperty<Float>? = property { instance.getNumberProperty(path) }
    override fun string(path: String): RiveProperty<String>? = property { instance.getStringProperty(path) }
    override fun boolean(path: String): RiveProperty<Boolean>? = property { instance.getBooleanProperty(path) }
    override fun color(path: String): RiveProperty<Int>? = property { instance.getColorProperty(path) }
    override fun enum(path: String): RiveProperty<String>? = property { instance.getEnumProperty(path) }

    override fun trigger(path: String): RiveTrigger? =
        lookup { instance.getTriggerProperty(path) }?.let { AndroidRiveTrigger(it, ::write) }

    private fun <T> property(get: () -> ViewModelProperty<T>): RiveProperty<T>? =
        lookup(get)?.let { AndroidRiveProperty(it, ::write) }

    /** rive-android reports a missing or differently typed path by throwing. */
    private fun <P> lookup(get: () -> P): P? {
        if (!instance.hasCppObject) return null
        return try {
            get()
        } catch (e: RiveException) {
            null
        }
    }

    /** Runs [change] against the native property and wakes the renderer, while the instance lives. */
    private fun write(change: () -> Unit) {
        if (!instance.hasCppObject) return
        change()
        onWrite()
    }
}

@OptIn(ExperimentalRiveCmpApi::class)
private class AndroidRiveProperty<T>(
    private val property: ViewModelProperty<T>,
    private val write: (change: () -> Unit) -> Unit,
) : RiveProperty<T> {
    override var value: T
        get() = property.value
        set(value) = write { property.value = value }

    override val valueFlow: StateFlow<T> get() = property.valueFlow
}

@OptIn(ExperimentalRiveCmpApi::class)
private class AndroidRiveTrigger(
    private val property: ViewModelTriggerProperty,
    private val write: (change: () -> Unit) -> Unit,
) : RiveTrigger {
    override fun trigger() = write { property.trigger() }

    // The flow starts with a placeholder firing, which is not a firing by the graphic.
    override val triggers: Flow<Unit> = property.valueFlow.drop(1).map { }
}

/**
 * Calls [onViewModelInstance] with the view model instance bound to [view]: once rive-android has
 * set up the scene, and again with a fresh instance each time [composition] is reset.
 *
 * rive-android binds on attach for bytes and after the download for URLs and offers no notification
 * for it, so this polls for the active artboard. It polls with a delay rather than per frame, so a
 * file that never loads (a failed download, say) doesn't keep requesting frames while it is shown. A view created before the callback
 * was set has nothing bound yet, and a reset unbinds, so both bind a fresh default instance here.
 */
@OptIn(ExperimentalRiveCmpApi::class)
@Composable
internal fun BindViewModelInstance(
    view: RiveAnimationView?,
    composition: RiveComposition?,
    onViewModelInstance: ((RiveViewModelInstance) -> Unit)?,
) {
    val currentOnViewModelInstance by rememberUpdatedState(onViewModelInstance)
    val enabled = onViewModelInstance != null

    LaunchedEffect(view, enabled) {
        if (view == null || !enabled) return@LaunchedEffect
        while (view.controller.activeArtboard == null) delay(ARTBOARD_POLL_INTERVAL_MS)
        val instance = view.controller.activeArtboard?.viewModelInstance ?: view.bindDefaultViewModelInstance()
        instance?.let { currentOnViewModelInstance?.invoke(view.adapt(it)) }
    }

    DisposableEffect(view, composition, enabled) {
        if (view != null && enabled) {
            composition?.afterReset = {
                view.bindDefaultViewModelInstance()?.let { currentOnViewModelInstance?.invoke(view.adapt(it)) }
            }
        }
        onDispose { composition?.afterReset = null }
    }
}

@OptIn(ExperimentalRiveCmpApi::class)
private fun RiveAnimationView.adapt(instance: ViewModelInstance): RiveViewModelInstance =
    AndroidRiveViewModelInstance(instance) {
        // Resumes the settled state machines; while they play the write is picked up anyway.
        if (!isPlaying) play(settleInitialState = false)
    }

/**
 * Binds a new default instance of the active artboard's default view model to the artboard and its
 * state machines, as rive-android's auto-binding does. Returns null when there is nothing to bind.
 */
private fun RiveAnimationView.bindDefaultViewModelInstance(): ViewModelInstance? {
    val artboard = controller.activeArtboard ?: return null
    val instance = try {
        controller.file?.defaultViewModelForArtboard(artboard)?.createDefaultInstance()
    } catch (e: ViewModelException) {
        null
    } ?: return null
    artboard.viewModelInstance = instance
    controller.stateMachines.forEach { it.viewModelInstance = instance }
    return instance
}

private const val ARTBOARD_POLL_INTERVAL_MS = 16L
