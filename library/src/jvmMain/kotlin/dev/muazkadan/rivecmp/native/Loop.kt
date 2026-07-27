package dev.muazkadan.rivecmp.native

public enum class Loop {
    ONESHOT, LOOP, PINGPONG;

    public companion object {
        /**
         * Returns the [Loop] associated to [index].
         *
         * @throws IllegalArgumentException If the index is out of bounds.
         */
        public fun fromIndex(index: Int): Loop {
            val maxIndex = entries.size
            if (index < 0 || index >= maxIndex)
                throw IndexOutOfBoundsException("Invalid Loop index value $index. It must be between 0 and ${maxIndex - 1}")

            return entries[index]
        }
    }
}
