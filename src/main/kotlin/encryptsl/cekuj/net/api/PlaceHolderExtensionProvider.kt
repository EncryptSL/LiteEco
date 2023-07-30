package encryptsl.cekuj.net.api

import encryptsl.cekuj.net.LiteEco
import encryptsl.cekuj.net.extensions.isNumeric
import encryptsl.cekuj.net.extensions.playerPosition
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import java.util.*
import java.util.stream.Collectors

class PlaceHolderExtensionProvider(private val liteEco: LiteEco) : PlaceholderExpansion() {

    override fun getIdentifier(): String = "liteeco"

    override fun getAuthor(): String = "EncryptSL"

    override fun getVersion(): String = "1.0.2"

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
            val split = this.spliterator(identifier)
            return if (split.isNumeric()) liteEco.api.formatting(balanceByRank(split.toInt())) else null
        }

        if (identifier.startsWith("top_balance_")) {
            val split = this.spliterator(identifier)
            return if (split.isNumeric()) balanceByRank(split.toInt()).toString() else null
        }

        if (identifier.startsWith("top_player_")) {
            val split = this.spliterator(identifier)
            return if (!split.isNumeric()) {
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

    private fun spliterator(pattern: String, index: Int = 2): String {
        val args: List<String> = pattern.split("_")
        return args[index]
    }

    private fun nameByRank(rank: Int): String {
        topBalance().playerPosition { index, entry ->
            if (index == rank) {
                return entry.key
            }
        }
        return "EMPTY"
    }

    private fun balanceByRank(rank: Int): Double {
        topBalance().playerPosition { index, entry ->
            if (index == rank) {
                return entry.value
            }
        }

        return 0.0
    }

    private fun topBalance(): LinkedHashMap<String, Double> {
        return liteEco.api.getTopBalance()
            .filterKeys { uuid -> Bukkit.getOfflinePlayer(UUID.fromString(uuid)).hasPlayedBefore() }
            .toList()
            .sortedByDescending { (_, balance) -> balance }
            .toMap()
            .let { LinkedHashMap<String, Double>(it) }
    }
}