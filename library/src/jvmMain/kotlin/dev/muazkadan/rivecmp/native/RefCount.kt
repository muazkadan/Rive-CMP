package dev.muazkadan.rivecmp.native

import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.decrementAndFetch
import kotlin.concurrent.atomics.incrementAndFetch

public interface RefCount {

    public var refs: AtomicInt
    public val refCount: Int
        get() = refs.load()

    public fun acquire(): Int = refs.incrementAndFetch()

    public fun release(): Int = refs.decrementAndFetch()
}
