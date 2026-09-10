package dev.muazkadan.rivecmp.native


/**
 * The [StateMachineInstance] is a helper to wrap common operations to play a state machine.
 *
 * This object has a counterpart in C++, which implements a lot of functionality. The
 * [unsafeCppPointer] keeps track of this relationship.
 *
 * Use this to keep track of a state machine's current state and progress, and to help [apply]
 * changes that the state machine makes to components in an [Artboard].
 */
public class StateMachineInstance(unsafeCppPointer: Long) :
    PlayableInstance, NativeObject(unsafeCppPointer) {

    private external fun cppAdvance(pointer: Long, elapsedTime: Float): Boolean
    private external fun cppInputCount(cppPointer: Long): Int
    private external fun cppSMIInputByIndex(cppPointer: Long, index: Int): Long
    private external fun cppName(cppPointer: Long): String
    private external fun cppSetViewModelInstance(cppPointer: Long, viewModel: Long)

    external override fun cppDelete(pointer: Long)

    /** @return The name of state machine. */
    override val name: String
        get() = cppName(cppPointer)

    /**
     * The [ViewModelInstance] assigned to this artboard. Once assigned, modifications to the.
     * properties of the instance will be reflected in the bindings of this state machine.
     *
     * Assigning will apply to both the [StateMachineInstance] and the parent [Artboard]. Assigning
     * null detaches the currently bound instance.
     */
    public var viewModelInstance: ViewModelInstance? = null
        set(value) {
            cppSetViewModelInstance(cppPointer, value?.cppPointer ?: NULL_POINTER)
            field = value
        }

    /**
     * Advance the state machine.
     *
     * @param elapsed The time in seconds to advance by.
     * @return `true` if the state machine will continue to animate after this advance.
     */
    public fun advance(elapsed: Float): Boolean = cppAdvance(cppPointer, elapsed)

    /** @return The number of inputs configured for the state machine. */
    public val inputCount: Int
        get() = cppInputCount(cppPointer)

    private fun convertInput(input: SMIInput): SMIInput = when {
        input.isBoolean -> SMIBoolean(input.cppPointer)
        input.isTrigger -> SMITrigger(input.cppPointer)
        input.isNumber -> SMINumber(input.cppPointer)
        else -> throw StateMachineInputException("Unknown State Machine Input Instance for ${input.name}.")
    }

    /**
     * Get the input instance at a given [index] in the state machine.
     *
     * This starts at 0.
     *
     * @throws StateMachineInputException If no [SMIInput] is found at the given [index].
     */
    @Throws(StateMachineInputException::class)
    public fun input(index: Int): SMIInput {
        val stateMachineInputPointer = cppSMIInputByIndex(cppPointer, index)
        if (stateMachineInputPointer == NULL_POINTER)
            throw StateMachineInputException("No StateMachineInput found at index $index.")

        val input = SMIInput(stateMachineInputPointer)
        return convertInput(input)
    }

    /**
     * Get the input with a given [name] in the state machine.
     *
     * @throws StateMachineInputException If no [SMIInput] is found with the given [name].
     */
    @Throws(StateMachineInputException::class)
    public fun input(name: String): SMIInput {
        for (i in 0 until inputCount) {
            val output = input(i)
            if (output.name == name) return output
        }
        throw StateMachineInputException("No StateMachineInput found with name $name.")
    }
}
