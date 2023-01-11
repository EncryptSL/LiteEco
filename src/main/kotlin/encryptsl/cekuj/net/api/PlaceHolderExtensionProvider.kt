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

    override fun getVersion(): String = "1.0.0"

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
            val split = this.splitator(identifier, 2)
            return liteEco.api.formatting(balanceByRank(split.toInt()))
        }

        if (identifier.startsWith("top_balance_")) {
            val split = this.splitator(identifier, 2)
            return balanceByRank(split.toInt()).toString()
        }

        if (identifier.startsWith("top_player_")) {
            val split = this.splitator(identifier, 2)

            return if (nameByRank(split.toInt()) == "EMPTY") {
                return nameByRank(split.toInt())
            } else Bukkit.getOfflinePlayer(UUID.fromString(nameByRank(split.toInt()))).name.orEmpty()
        }

        return when(identifier) {
            "balance" -> liteEco.api.getBalance(player).toString()
            "balance_formatted" -> liteEco.api.formatting(liteEco.api.getBalance(player))
            "top_rank_player" -> nameByRank(1)
            else -> null
        }
    }

    private fun splitator(pattern: String, index: Int): String {
        val args: List<String> = pattern.split("_")
        return args[index]
    }

    private fun nameByRank(rank: Int): String {
        topBalance()?.playerPosition { index, entry ->
            if (index == rank) {
                return entry.key.ifEmpty { "EMPTY" }
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
          return liteEco.preparedStatements.getTopBalance(10)
            .entries
            .stream()
            .sorted(compareByDescending{o1 -> o1.value})
            .collect(
                Collectors.toMap({ e -> e.key }, { e -> e.value }, { _, e2 -> e2 }) { LinkedHashMap() })
    }
}