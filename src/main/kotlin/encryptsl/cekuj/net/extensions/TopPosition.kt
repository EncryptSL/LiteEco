package encryptsl.cekuj.net.extensions

inline fun <K, V, M : Map<out K, V>> M.playerPosition(action: (index: Int, Map.Entry<K, V>) -> Unit) {
    var index = 1
    for (item in this) action(index++, item)
}