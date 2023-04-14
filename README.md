# Benchmarks comparing performance of `Iterable::map` and its specialization for `Collection`.

`Iterable::map` relies on `Iterable::collectionSizeOrDefault` to find the size when allocation resulting list. `collectionSizeOrDefault` checks if its argument is an instance of `Collection` and depending on the environment where that check will be executed it may affect the performance. To avoid this performance impact a new specialized version of `map` for `Collection` could be added to get the size using `size` method of a corresponding collection.

These benchmarks check `map` performance in three different modes:
- `map` invoked on instances of the same class - in this mode JVM may use type profiling during JIT-compilation and replace `instanceof Collection` check with exact type check;
- `map` invoked on instances of the same class in main benchmark, but instances of other classes are passed to `map` in between runs - in this mode JVM will have to emit `instanceof` check during JIT-compilation;
- the same as the previous mode, but `Collaction`'s instance passed to `map` concurrently check for being an instance of some other interface - in this mode HotSpot JVM Klass' secondary type cache may not contain `Collection` as recently checked secondary type and that will force JIT-compiled code to perform full `instanceof` check.    

## Results
Results were obtained by running benchmarks on Apple M2 Pro based host using JDK8 and JDK11.

[JDK8](https://jmh.morethan.io/?source=https://raw.githubusercontent.com/fzhinkin/collection-map-benchmarks/main/results/jdk8-map-results.json)

[JDK11](https://jmh.morethan.io/?source=https://raw.githubusercontent.com/fzhinkin/collection-map-benchmarks/main/results/jdk11-map-results.json)

## Conclusions

Using a specialized version of `map` that avoids `instanceof` check when `map` invoked on a `Collection` implementations has only positive effect on `map`'s performance.