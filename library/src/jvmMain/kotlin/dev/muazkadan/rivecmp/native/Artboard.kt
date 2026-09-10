package dev.muazkadan.rivecmp.native

import dev.muazkadan.rivecmp.native.model.Alignment
import dev.muazkadan.rivecmp.native.model.Fit

/**
 * [Artboard]s as designed in the Rive animation editor.
 *
 * [Artboard]s provide access to available [animation]s, and some basic properties. You can [draw]
 * artboards using a [Renderer][Renderer] that is tied to a
 * canvas.
 *
 * @param unsafeCppPointer Pointer to the C++ counterpart.
 */
public class Artboard(
    unsafeCppPointer: Long,
) : NativeObject(unsafeCppPointer) {

    private external fun cppName(cppPointer: Long): String

    private external fun cppAnimationByName(cppPointer: Long, name: String): Long
    private external fun cppAnimationCount(cppPointer: Long): Int
    private external fun cppAnimationNameByIndex(cppPointer: Long, index: Int): String

    private external fun cppStateMachineByName(cppPointer: Long, name: String): Long
    private external fun cppStateMachineCount(cppPointer: Long): Int
    private external fun cppStateMachineNameByIndex(cppPointer: Long, index: Int): String

    private external fun cppInputByNameAtPath(cppPointer: Long, name: String, path: String): Long

    private external fun cppAdvance(cppPointer: Long, elapsedTime: Float): Boolean

    private external fun cppDrawAligned(
        cppPointer: Long,
        rendererPointer: Long,
        fitOrdinal: Int,
        alignmentOrdinal: Int,
        scaleFactor: Float,
    )

    private external fun cppSetViewModelInstance(cppPointer: Long, instancePointer: Long)

    external override fun cppDelete(pointer: Long)

    /** Get the [name] of the Artboard. */
    public val name: String
        get() = cppName(cppPointer)

    /**
     * Get the animation with a given [name] in the [Artboard].
     *
     * @throws AnimationException If the animation does not exist.
     */
    @Throws(AnimationException::class)
    public fun animation(name: String): LinearAnimationInstance {
        val animationPointer = cppAnimationByName(cppPointer, name)
        if (animationPointer == NULL_POINTER)
            throw AnimationException(
                "Animation \"$name\" not found. " +
                        "Available Animations: ${animationNames.map { "\"$it\"" }}\"",
            )

        val lai = LinearAnimationInstance(animationPointer)
        dependencies.add(lai)
        return lai
    }

    /**
     * Get the state machine with a given [name] in the artboard.
     *
     * @throws StateMachineException If the state machine does not exist.
     */
    @Throws(StateMachineException::class)
    public fun stateMachine(name: String): StateMachineInstance {
        val stateMachinePointer = cppStateMachineByName(cppPointer, name)
        if (stateMachinePointer == NULL_POINTER)
            throw StateMachineException("No StateMachine found with name $name.")

        val smi = StateMachineInstance(stateMachinePointer)
        dependencies.add(smi)
        return smi
    }

    /**
     * Get the input instance with a given [name] on the nested artboard represented at [path].
     *
     * @throws StateMachineInputException If the input does not exist.
     */
    @Throws(StateMachineInputException::class)
    public fun input(name: String, path: String): SMIInput {
        val stateMachineInputPointer = cppInputByNameAtPath(cppPointer, name, path)
        if (stateMachineInputPointer == NULL_POINTER)
            throw StateMachineInputException("No StateMachineInput found with name \"$name\" in nested artboard $path.")

        val input = SMIInput(stateMachineInputPointer)
        return convertInput(input)
    }

    /** @return The number of animations stored inside the artboard. */
    public val animationCount: Int
        get() = cppAnimationCount(cppPointer)

    /** @return The number of state machines stored inside the artboard. */
    public val stateMachineCount: Int
        get() = cppStateMachineCount(cppPointer)

    /**
     * The [ViewModelInstance] assigned to this artboard. Once assigned, modifications to the
     * properties of the instance will be reflected in the bindings of this artboard.
     *
     * Assigning null will do nothing.
     *
     * You should only use assign this property if your file does not contain a state machine. If it
     * does, prefer [StateMachineInstance.viewModelInstance] instead, as it will set the view model
     * for both the state machine and the artboard.
     */
    public var viewModelInstance: ViewModelInstance? = null
        set(value) {
            value?.let {
                cppSetViewModelInstance(cppPointer, it.cppPointer)
                field = value
            }
        }

    /**
     * Advancing the artboard:
     * - Updates the layout for all dirty components contained in the artboard
     * - Updates the positions
     * - Forces all components in the artboard to be laid out
     *
     * Components are all the shapes, bones and groups of an artboard. Whenever components are added
     * to an artboard, for example when an artboard is first loaded, they are considered dirty.
     * Whenever animations change properties of components, move a shape, or change a color, they
     * are marked as dirty.
     *
     * Before any changes to components will be visible in the next rendered frame, the artboard
     * needs to be [advanced][advance].
     *
     * [elapsedTime] is currently not taken into account.
     */
    public fun advance(elapsedTime: Float): Boolean = cppAdvance(cppPointer, elapsedTime)

    /**
     * Draw the the artboard to the [renderer][Renderer]. Also
     * align the artboard to the render surface.
     */
    public fun draw(
        rendererPointer: Long,
        fit: Fit,
        alignment: Alignment,
        scaleFactor: Float = 1.0f,
    ) {
        if (!hasCppObject) return
        cppDrawAligned(
            cppPointer,
            rendererPointer,
            fit.ordinal,
            alignment.ordinal,
            scaleFactor,
        )
    }

    /** @return The names of all animations in the artboard. */
    public val animationNames: List<String>
        get() = (0 until animationCount).map { index ->
            cppAnimationNameByIndex(cppPointer, index)
        }

    /** @return The names of all stateMachines in the artboard. */
    public val stateMachineNames: List<String>
        get() = (0 until stateMachineCount).map { index ->
            cppStateMachineNameByIndex(cppPointer, index)
        }

    private fun convertInput(input: SMIInput): SMIInput =
        when {
            input.isBoolean -> SMIBoolean(input.cppPointer)
            input.isTrigger -> SMITrigger(input.cppPointer)
            input.isNumber -> SMINumber(input.cppPointer)
            else -> throw StateMachineInputException("Unknown State Machine Input Instance for ${input.name}.")
        }
}
