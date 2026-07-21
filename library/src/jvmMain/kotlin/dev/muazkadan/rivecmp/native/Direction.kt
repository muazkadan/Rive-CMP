package dev.muazkadan.rivecmp.native

public enum class Direction(public val value: Int) {
    BACKWARDS(-1),
    FORWARDS(1),
    AUTO(0);

    public companion object {

        private val map = entries.associateBy(Direction::value)
        public fun fromInt(type: Int): Direction? = map[type]
    }
}
