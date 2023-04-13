package org.example

import org.openjdk.jmh.annotations.*
import java.io.Serializable

inline fun <T, R> Collection<T>.mapOpt(transform: (T) -> R): List<R> {
    return mapTo(ArrayList(size), transform)
}

enum class HostilityMode {
    NONE,
    TYPE,
    TYPE_AND_KLASS_CACHE
}

@State(Scope.Benchmark)
open class CollectionMapBenchmark {
    private var targetCollection: Collection<Int> = emptyList()
    private var additionalCollections = listOf(emptySet<Int>(), mutableListOf(), emptyList())

    @Param("NONE", "TYPE", "TYPE_AND_KLASS_CACHE")
    var mode: HostilityMode = HostilityMode.NONE

    @Param("0", "1", "10")
    var size: Int = 0

    @Setup(Level.Trial)
    fun setupCollection() {
        targetCollection = (0 until size).toMutableList()
    }

    @Setup(Level.Iteration)
    fun setupIteration() {
        if (mode != HostilityMode.NONE) {
            // Both baselineImplementation and optImplementation are marked as non-inlinable,
            // by passing instances of different classes we're polluting type profiles for
            // these methods and preventing an optimization that would otherwise ignore
            // 'instanceof' "bytecode" and will only check the concrete class of an argument.
            additionalCollections.forEach {
                baselineImplementation(it)
                optImplementation(it)
                resetClassCache(it)
            }
        }
    }

    // Iterable.map use Iterable.collectionSizeOrDefault method which checks
    // whether an iterable is instance of Collection (which is an interface, so-called secondary type).
    // (At least) Hotspot JVM optimize secondary type checks for a class by caching the last successfully
    // detected secondary superclass within a special single-entry cache inside the class.
    // To reset that cache we're simply testing a collection for being an implementation of another interface.
    // Please refer to paper "Fast Subtype Checking in the HotSpot JVM" by Cliff Click and John Rose for the idea
    // of how instanceof works under the hood.
    private fun resetCache() {
        if (mode == HostilityMode.TYPE_AND_KLASS_CACHE) {
            resetClassCache(targetCollection)
        }
    }

    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    fun resetClassCache(o: Any) = o is Serializable

    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    fun baselineImplementation(c: Collection<Int>) = c.map { it + 1 }

    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    fun optImplementation(c: Collection<Int>) = c.mapOpt { it + 1 }

    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    @Group("baseline")
    fun baseline() = baselineImplementation(targetCollection)

    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    @Group("opt")
    fun opt() = optImplementation(targetCollection)

    @Benchmark
    @Group("baseline")
    fun baselineCacheReset() = resetCache()

    @Benchmark
    @Group("opt")
    fun optCacheReset() = resetCache()
}