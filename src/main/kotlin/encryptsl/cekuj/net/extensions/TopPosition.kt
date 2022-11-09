package encryptsl.cekuj.net.extensions

inline fun <K, V, M : Map<out K, V>> M.playerPosition(action: (index: Int, Map.Entry<K, V>) -> Unit) {
    var index = 1
    for (item in this) action(index++, item)
}

inline fun <T, R> Iterable<T>.positionIndexed(transform: (index: Int, T) -> R): List<R> {
    var index = 1
    for (item in this) transform(index++, item)
    return mapIndexedTo(ArrayList<R>(collectionSizeOrDefault(10)), transform)
}

fun <T> Iterable<T>.collectionSizeOrDefault(default: Int): Int = if (this is Collection<*>) this.size else default