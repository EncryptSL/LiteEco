package encryptsl.cekuj.net.extensions

inline fun <K, V, M : Map<out K, V>> M.playerPosition(action: (index: Int, Map.Entry<K, V>) -> Unit) {
    var index = 1
    for (item in this) action(index++, item)
}

inline fun <T, R> Iterable<T>.positionIndexed(transform: (index: Int, T) -> R): List<R> {
    return mapIndexedTo(ArrayList<R>(collectionSizeOrDefault(10)), transform)
}

inline fun <T, R, C : MutableCollection<in R>> Iterable<T>.mapIndexedTo(destination: C, transform: (index: Int, T) -> R): C {
    var index = 1
    for (item in this)
        destination.add(transform(index++, item))
    return destination
}

fun <T> Iterable<T>.collectionSizeOrDefault(default: Int): Int = if (this is Collection<*>) this.size else default