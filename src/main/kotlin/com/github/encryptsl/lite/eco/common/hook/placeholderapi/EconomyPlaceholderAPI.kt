package com.github.encryptsl.lite.eco.common.hook.placeholderapi

import com.github.encryptsl.lite.eco.LiteEco
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import java.math.BigDecimal
import java.util.*

class EconomyPlaceholderAPI(private val liteEco: LiteEco, private val extVersion: String) : PlaceholderExpansion() {

    override fun getIdentifier(): String = "liteeco"

    override fun getAuthor(): String = "EncryptSL"

    override fun getVersion(): String = extVersion

    override fun getRequiredPlugin(): String = liteEco.name

    override fun persist(): Boolean = true

    override fun canRegister(): Boolean {
        return liteEco.server.pluginManager.getPlugin(requiredPlugin)!!.isEnabled
    }

    override fun onRequest(player: OfflinePlayer?, identifier: String): String? {
        if (player == null) return null
        val args = identifier.split("_")
        val currencyName = args.getOrNull(1) ?: args.getOrNull(2).toString()
        val rank = args.getOrNull(3)?.toIntOrNull()

        return when (identifier) {
            "balance" -> liteEco.api.getBalance(player, Optional.ofNullable(currencyName).orElse(liteEco.currencyImpl.defaultCurrency())).toString()
            "balance_formatted" -> liteEco.api.fullFormatting(liteEco.api.getBalance(player, Optional.ofNullable(currencyName).orElse(liteEco.currencyImpl.defaultCurrency())))
            "balance_compacted" -> liteEco.api.compacted(liteEco.api.getBalance(player, Optional.ofNullable(currencyName).orElse(liteEco.currencyImpl.defaultCurrency())))
            "top_rank_player" -> nameByRank(1, Optional.ofNullable(currencyName).orElse(liteEco.currencyImpl.defaultCurrency()))
            else -> rank?.let {
                when {
                    identifier.startsWith("top_formatted_") -> liteEco.api.fullFormatting(balanceByRank(rank, Optional.ofNullable(currencyName).orElse(liteEco.currencyImpl.defaultCurrency())))
                    identifier.startsWith("top_compacted_") -> liteEco.api.compacted(balanceByRank(rank, Optional.ofNullable(currencyName).orElse(liteEco.currencyImpl.defaultCurrency())))
                    identifier.startsWith("top_balance_") -> balanceByRank(rank, Optional.ofNullable(currencyName).orElse(liteEco.currencyImpl.defaultCurrency())).toString()
                    identifier.startsWith("top_player_") -> nameByRank(rank, Optional.ofNullable(currencyName).orElse(liteEco.currencyImpl.defaultCurrency()))
                    else -> null
                }
            }
        }
    }

    private fun nameByRank(rank: Int, currency: String): String {
        val topBalance = topBalance(currency)
        return if (rank in 1..topBalance.size) {
            val playerUuid = topBalance.keys.elementAt(rank - 1)
            Bukkit.getOfflinePlayer(UUID.fromString(playerUuid)).name ?: "UNKNOWN"
        } else {
            "EMPTY"
        }
    }

    private fun balanceByRank(rank: Int, currency: String): BigDecimal {
        val topBalance = topBalance(currency)
        return if (rank in 1..topBalance.size) {
            topBalance.values.elementAt(rank - 1)
        } else {
            BigDecimal.ZERO
        }
    }

    private fun topBalance(currency: String): Map<String, BigDecimal> {
        return liteEco.api.getTopBalance(currency)
    }
}
