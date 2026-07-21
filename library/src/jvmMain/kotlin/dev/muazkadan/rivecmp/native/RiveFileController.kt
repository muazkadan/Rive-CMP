package dev.muazkadan.rivecmp.native

import dev.muazkadan.rivecmp.native.model.Alignment
import dev.muazkadan.rivecmp.native.model.Fit

public class RiveFileController(
    public val autoplay: Boolean,
    private val autoBind: Boolean = false,
    private val animationName: String? = null,
    public val stateMachineName: String?,
    public val file: File,
    artboard: Artboard = file.firstArtboard,
    public val alignment: Alignment = Alignment.CENTER,
    public val fit: Fit = Fit.LAYOUT,
    public val layoutScaleFactorActive: Float = 1.0f,
    public val loop: Loop = Loop.LOOP,
) {
    init {
        RiveLog.d(TAG, "Initializing.")
    }

    private val changedInputs: ArrayDeque<ChangedInput> = ArrayDeque()

    public val artboardRenderer: RiveArtboardRenderer = RiveArtboardRenderer(this)

    private val animations = mutableListOf<LinearAnimationInstance>()

    private val stateMachines = mutableListOf<StateMachineInstance>()

    private val playingAnimations = mutableSetOf<LinearAnimationInstance>()

    private val playingStateMachines = mutableSetOf<StateMachineInstance>()

    private val pausedAnimations: Set<LinearAnimationInstance>
        get() = animations subtract playingAnimations

    private val pausedStateMachines: Set<StateMachineInstance>
        get() = stateMachines subtract playingStateMachines

    public val isAdvancing: Boolean
        get() = playingAnimations.isNotEmpty() || playingStateMachines.isNotEmpty() || changedInputs.isNotEmpty()

    public var artboard: Artboard = artboard
        private set

    init {
        setArtboard()
    }

    /**
     * Instances the artboard]. If this controller is set to [autoplay] it will also start playing.
     */
    public fun selectArtboard() {
        RiveLog.d(TAG, "Selecting artboard: ${artboard.name}")
        artboard = file.artboard(artboard.name)
        setArtboard()
    }

    private fun setArtboard() {
        if (autoBind) {
            val defaultInstance = file.defaultViewModelForArtboard(artboard).createDefaultInstance()
            artboard.viewModelInstance = defaultInstance
            // Since state machines aren't created until play(),
            // we need to check if they need to be created now.
            (stateMachineName
                ?: artboard.stateMachineNames.firstOrNull())?.let(::getOrCreateStateMachines)
            stateMachines.forEach { it.viewModelInstance = defaultInstance }
        }
        if (autoplay) {
            if (animationName != null) play(animationName)
            else if (stateMachineName != null) play(
                stateMachineName,
                settleInitialState = true,
                isStateMachine = true,
            )
            else play(settleInitialState = true)
        } else {
            artboard.advance(0f)
            // Schedule a single frame.
            artboardRenderer.start()
        }
    }

    public fun advance(elapsed: Float) {
        // Process all the inputs right away.
        processAllInputs()
        // animations could change, lets cut a list.
        // order of animations is important.....
        var shouldArtboardAdvance = false
        animations.toList().forEach { animationInstance ->
            if (playingAnimations.contains(animationInstance)) {
                val advanceResult = animationInstance.advanceAndGetResult(elapsed)
                animationInstance.apply()
                when (advanceResult) {
                    AdvanceResult.ONESHOT -> stop(animationInstance)
                    AdvanceResult.LOOP, AdvanceResult.PINGPONG -> {}
                    AdvanceResult.ADVANCED ->
                        // The controller needs to explicitly call `artboard.advance()`
                        // only if there are no State Machines playing. That is because
                        // `StateMachineInstance`s call `advanceAndApply()` that will
                        // internally advance the artboard.
                        shouldArtboardAdvance = playingStateMachines.isEmpty()

                    AdvanceResult.NONE -> Unit // NOP
                }
            }
        }

        if (shouldArtboardAdvance) artboard.advance(elapsed)

        val stateMachinesToPause = mutableListOf<StateMachineInstance>()
        stateMachines.forEach { stateMachineInstance ->
            if (playingStateMachines.contains(stateMachineInstance)) {
                val stillPlaying = resolveStateMachineAdvance(stateMachineInstance, elapsed)

                if (!stillPlaying) stateMachinesToPause.add(stateMachineInstance)
            }
        }

        // Only remove the state machines if the elapsed time was
        // greater than 0. 0 elapsed time causes no changes so it's
        // no-op advance.
        if (elapsed > 0.0) stateMachinesToPause.forEach { pause(stateMachine = it) }

        // Poll the assigned view model instances for changes.
        playingStateMachines.mapNotNull(StateMachineInstance::viewModelInstance)
            .forEach(ViewModelInstance::pollChanges)
    }

    public fun play(
        animationName: String,
        loop: Loop = Loop.ONESHOT,
        direction: Direction = Direction.AUTO,
        isStateMachine: Boolean = false,
        settleInitialState: Boolean = true,
    ): Unit = playAnimation(
        animationName = animationName,
        loop = loop,
        direction = direction,
        isStateMachine = isStateMachine,
        settleInitialState = settleInitialState,
    )

    /**
     * Restarts paused animations if there are any. Otherwise, it starts playing the first animation
     * (timeline or state machine) in the Artboard.
     */
    public fun play(
        loop: Loop = Loop.ONESHOT,
        direction: Direction = Direction.AUTO,
        settleInitialState: Boolean = true,
    ) {
        if (pausedAnimations.isNotEmpty() || pausedStateMachines.isNotEmpty()) {
            animations.forEach { instance ->
                play(
                    animationInstance = instance,
                    direction = direction,
                    loop = loop,
                )
            }
            stateMachines.forEach { instance ->
                play(stateMachineInstance = instance, settleStateMachineState = settleInitialState)
            }
        } else {
            val animationNames = artboard.animationNames
            if (animationNames.isNotEmpty()) {
                playAnimation(
                    animationName = animationNames.first(),
                    loop = loop,
                    direction = direction,
                )
            }
            val stateMachineNames = artboard.stateMachineNames
            if (stateMachineNames.isNotEmpty())
                playAnimation(
                    animationName = stateMachineNames.first(),
                    loop = loop,
                    direction = direction,
                    isStateMachine = true,
                    settleInitialState = settleInitialState,
                )
        }
    }

    public fun pause() {
        artboardRenderer.stop()
        // pause will modify playing animations, so we cut a list of it first.
        playingAnimations.toList().forEach(::pause)
        playingStateMachines.toList().forEach(::pause)
    }

    /** Named [stopAnimations] to avoid conflicting with [stop]. */
    public fun stopAnimations() {
        // stop will modify animations, so we cut a list of it first.
        if (animations.isNotEmpty()) animations.toList().forEach(::stop)
        if (stateMachines.isNotEmpty()) stateMachines.toList().forEach(::stop)
    }

    /**
     * Queues an input with [inputName] so that it can be processed on the next advance. It also
     * tries to start if it's not started, which will internally cause advance to happen
     * at least once.
     */
    private fun queueInput(
        stateMachineName: String,
        inputName: String,
        value: Any? = null,
        path: String? = null,
    ) = queueInputs(
        ChangedInput(
            stateMachineName = stateMachineName,
            name = inputName,
            value = value,
            nestedArtboardPath = path,
        ),
    )

    private fun queueInputs(vararg inputs: ChangedInput) {
        changedInputs.addAll(inputs)
        // Restart if needed.
        artboardRenderer.start()
    }

    private fun processAllInputs() {
        // Gather all state machines that need playing and do that only once.
        val playableSet = mutableSetOf<StateMachineInstance>()
        // No need to lock this: this is being called from `advance()` which is `synchronized(file)`
        while (changedInputs.isNotEmpty()) {
            val input = changedInputs.removeFirst()
            if (input.nestedArtboardPath == null) {
                val stateMachines = getOrCreateStateMachines(input.stateMachineName)
                stateMachines.forEach { stateMachineInstance ->
                    playableSet.add(stateMachineInstance)
                    when (val smiInput = stateMachineInstance.input(input.name)) {
                        is SMITrigger -> smiInput.fire()
                        is SMIBoolean -> smiInput.value = input.value as Boolean
                        is SMINumber -> smiInput.value = input.value as Float
                    }
                }
            } else when (val smiInput = artboard.input(input.name, input.nestedArtboardPath)) {
                is SMITrigger -> smiInput.fire()
                is SMIBoolean -> smiInput.value = input.value as Boolean
                is SMINumber -> smiInput.value = input.value as Float
            }
        }
        playableSet.forEach { instance -> play(instance, false) }
    }

    public fun fireState(stateMachineName: String, inputName: String, path: String? = null): Unit =
        queueInput(stateMachineName = stateMachineName, inputName = inputName, path = path)

    public fun setBooleanState(
        stateMachineName: String,
        inputName: String,
        value: Boolean,
        path: String? = null,
    ): Unit = queueInput(
        stateMachineName = stateMachineName,
        inputName = inputName,
        value = value,
        path = path,
    )

    public fun setNumberState(
        stateMachineName: String,
        inputName: String,
        value: Float,
        path: String? = null,
    ): Unit = queueInput(
        stateMachineName = stateMachineName,
        inputName = inputName,
        value = value,
        path = path,
    )

    private fun animations(animationName: String): List<LinearAnimationInstance> =
        animations(listOf(animationName))

    private fun stateMachines(animationName: String): List<StateMachineInstance> =
        stateMachines(listOf(animationName))

    private fun animations(animationNames: Collection<String>): List<LinearAnimationInstance> =
        animations.filter { animationNames.contains(it.name) }

    private fun stateMachines(animationNames: Collection<String>): List<StateMachineInstance> =
        stateMachines.filter { animationNames.contains(it.name) }

    private fun getOrCreateStateMachines(animationName: String): List<StateMachineInstance> {
        val stateMachineInstances = stateMachines(animationName)
        return stateMachineInstances.ifEmpty {
            val stateMachineInstance = artboard.stateMachine(animationName)
            stateMachines.add(stateMachineInstance)
            listOf(stateMachineInstance)
        }
    }

    private fun playAnimation(
        animationName: String,
        loop: Loop = Loop.ONESHOT,
        direction: Direction = Direction.AUTO,
        isStateMachine: Boolean = false,
        settleInitialState: Boolean = true,
    ) {
        if (isStateMachine) {
            val stateMachineInstances = getOrCreateStateMachines(animationName)
            stateMachineInstances.forEach { instance -> play(instance, settleInitialState) }
        } else {
            val animationInstances = animations(animationName)
            animationInstances.forEach { instance -> play(instance, loop, direction) }
            if (animationInstances.isEmpty()) {
                val animationInstance = artboard.animation(animationName)
                play(animationInstance, loop, direction)
            }
        }
    }

    private fun resolveStateMachineAdvance(
        stateMachineInstance: StateMachineInstance,
        elapsed: Float,
    ): Boolean = stateMachineInstance.advance(elapsed)

    private fun play(
        stateMachineInstance: StateMachineInstance,
        settleStateMachineState: Boolean = true,
    ) {
        if (!stateMachines.contains(stateMachineInstance)) stateMachines.add(stateMachineInstance)
        // Special case:
        // When we start to "play" a state machine, we want it to settle on its initial state
        // otherwise it maybe "stuck" on the Enter state causing issues for fireState triggers.
        // https://2dimensions.slack.com/archives/CLLCU09T6/p1638984141105200
        if (settleStateMachineState) resolveStateMachineAdvance(stateMachineInstance, 0f)
        playingStateMachines.add(stateMachineInstance)
        artboardRenderer.start()
    }

    private fun play(
        animationInstance: LinearAnimationInstance,
        loop: Loop,
        direction: Direction,
    ) {
        // If a loop mode was specified, use it, otherwise fall back to a predefined default loop,
        // otherwise just use what the animation is configured to be.
        // not really sure if sticking loop into the xml thing makes much sense...
        val appliedLoop = if (loop == Loop.ONESHOT) this.loop else loop
        if (appliedLoop != Loop.ONESHOT) {
            animationInstance.loop = appliedLoop
        }
        if (!animations.contains(animationInstance)) {
            if (direction == Direction.BACKWARDS) animationInstance.time(animationInstance.endTime)
            animations.add(animationInstance)
        }
        if (direction != Direction.AUTO) animationInstance.direction = direction
        playingAnimations.add(animationInstance)
        artboardRenderer.start()
    }

    private fun pause(animation: LinearAnimationInstance) = playingAnimations.remove(animation)

    private fun pause(stateMachine: StateMachineInstance) =
        playingStateMachines.remove(stateMachine)

    private fun stop(animation: LinearAnimationInstance) {
        playingAnimations.remove(animation)
        animations.remove(animation)
    }

    private fun stop(stateMachine: StateMachineInstance) {
        playingStateMachines.remove(stateMachine)
        stateMachines.remove(stateMachine)
    }

    /**
     * We want to clear out all references to objects with potentially stale native counterparts.
     */
    internal fun reset() {
        RiveLog.d(TAG, "Resetting.")
        playingAnimations.clear()
        animations.clear()
        playingStateMachines.clear()
        stateMachines.clear()
        changedInputs.clear()
    }

    public fun dispose() {
        RiveLog.d(TAG, "Releasing.")
        artboardRenderer.release()
    }

    public companion object {
        public const val TAG: String = "RiveL/RiveFileController"
    }
}

/**
 * Wraps the data necessary for grabbing an input with [name] with [value] [value] is necessary when
 * wrapping [SMINumber] and [SMIBoolean] inputs.
 */
public data class ChangedInput(
    val stateMachineName: String,
    val name: String,
    val value: Any? = null,
    val nestedArtboardPath: String? = null,
)

