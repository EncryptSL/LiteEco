package encryptsl.cekuj.net.api

import encryptsl.cekuj.net.LiteEco
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import java.util.*

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
        val args = identifier.split("_")

        return when {
            identifier.startsWith("top_formatted_") -> {
                val rank = args[2].toIntOrNull()
                rank?.let { liteEco.api.formatting(balanceByRank(it)) }
            }
            identifier.startsWith("top_balance_") -> {
                val rank = args[2].toIntOrNull()
                rank?.let { balanceByRank(it).toString() }
            }
            identifier.startsWith("top_player_") -> {
                val rank = args[2].toIntOrNull()
                rank?.let { nameByRank(it) }
            }
            identifier == "balance" -> liteEco.api.getBalance(player).toString()
            identifier == "balance_formatted" -> liteEco.api.formatting(liteEco.api.getBalance(player))
            identifier == "top_rank_player" -> nameByRank(1)
            else -> null
        }
    }

    private fun nameByRank(rank: Int): String {
        val topBalance = topBalance()
        return if (rank in 1..topBalance.size) {
            val playerUuid = topBalance.keys.elementAt(rank - 1)
            Bukkit.getOfflinePlayer(UUID.fromString(playerUuid)).name ?: "UNKNOWN"
        } else {
            "EMPTY"
        }
    }

    private fun balanceByRank(rank: Int): Double {
        val topBalance = topBalance()
        return if (rank in 1..topBalance.size) {
            topBalance.values.elementAt(rank - 1)
        } else {
            0.0
        }
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