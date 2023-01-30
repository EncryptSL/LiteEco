package encryptsl.cekuj.net.api

import encryptsl.cekuj.net.LiteEco
import encryptsl.cekuj.net.extensions.playerPosition
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import java.util.*
import java.util.stream.Collectors

class PlaceHolderExtensionProvider(private val liteEco: LiteEco) : PlaceholderExpansion() {

    override fun getIdentifier(): String = "liteeco"

    override fun getAuthor(): String = "EncryptSL"

    override fun getVersion(): String = "1.0.1"

    override fun persist(): Boolean = true

    override fun getRequiredPlugin(): String {
        return liteEco.description.name
    }

    override fun canRegister(): Boolean {
        return liteEco.server.pluginManager.getPlugin(requiredPlugin)!!.isEnabled
    }

    override fun onRequest(player: OfflinePlayer?, identifier: String): String? {
        if (player == null) return null

        if (identifier.startsWith("top_formatted_")) {
            val split = this.spliterator(identifier, 2)
            return if (split.isNotEmpty()) liteEco.api.formatting(balanceByRank(split.toInt())) else null
        }

        if (identifier.startsWith("top_balance_")) {
            val split = this.spliterator(identifier, 2)
            return if (split.isNotEmpty()) balanceByRank(split.toInt()).toString() else null
        }

        if (identifier.startsWith("top_player_")) {
            val split = this.spliterator(identifier, 2)
            return if (split.isEmpty()) {
                null
            } else if (nameByRank(split.toInt()) == "EMPTY") {
                nameByRank(split.toInt())
            } else {
                Bukkit.getOfflinePlayer(UUID.fromString(nameByRank(split.toInt()))).name
            }
        }

        return when(identifier) {
            "balance" -> liteEco.api.getBalance(player).toString()
            "balance_formatted" -> liteEco.api.formatting(liteEco.api.getBalance(player))
            "top_rank_player" -> nameByRank(1)
            else -> null
        }
    }

    private fun spliterator(pattern: String, index: Int): String {
        val args: List<String> = pattern.split("_")
        return args[index]
    }

    private fun isNumeric(str: String): Boolean {
        return str.matches("(\\d*)")
    }

    private fun nameByRank(rank: Int): String {
        topBalance()?.playerPosition { index, entry ->
            if (index == rank) {
                return entry.key
            }
        }
        return "EMPTY"
    }

    private fun balanceByRank(rank: Int): Double {
        topBalance()?.playerPosition { index, entry ->
            if (index == rank) {
                return entry.value
            }
        }

        return 0.0
    }

    private fun topBalance(): LinkedHashMap<String, Double>? {
          return liteEco.api.getTopBalance()
            .entries
            .stream()
            .filter { data -> Bukkit.getOfflinePlayer(UUID.fromString(data.key)).hasPlayedBefore() }
            .sorted(compareByDescending{o1 -> o1.value})
            .collect(
                Collectors.toMap({ e -> e.key }, { e -> e.value }, { _, e2 -> e2 }) { LinkedHashMap() })
    }
}