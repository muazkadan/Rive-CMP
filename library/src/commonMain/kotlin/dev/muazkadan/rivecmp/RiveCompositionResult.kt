package dev.muazkadan.rivecmp

import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf

@Stable
interface RiveCompositionResult : State<RiveComposition?> {
    /**
     * The composition or null if it hasn't yet loaded or failed to load.
     */
    override val value: RiveComposition?

    /**
     * Whether the composition is currently loading.
     */
    val isLoading: Boolean

    /**
     * The error that occurred during loading, if any.
     */
    val error: Throwable?
}

internal class RiveCompositionResultImpl : RiveCompositionResult {
    private val _compositionState = mutableStateOf<RiveComposition?>(null)
    private val _isLoadingState = mutableStateOf(true)
    private val _errorState = mutableStateOf<Throwable?>(null)

    override val value: RiveComposition?
        get() = _compositionState.value

    override val isLoading: Boolean
        get() = _isLoadingState.value

    override val error: Throwable?
        get() = _errorState.value

    internal fun complete(composition: RiveComposition?) {
        _compositionState.value = composition
        _isLoadingState.value = false
        _errorState.value = null
    }

    internal fun completeWithError(throwable: Throwable) {
        _compositionState.value = null
        _isLoadingState.value = false
        _errorState.value = throwable
    }
}