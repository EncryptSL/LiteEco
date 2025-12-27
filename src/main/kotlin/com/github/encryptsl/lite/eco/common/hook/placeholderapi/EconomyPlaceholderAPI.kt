package com.github.encryptsl.lite.eco.common.hook.placeholderapi

import com.github.encryptsl.lite.eco.LiteEco
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.OfflinePlayer

class EconomyPlaceholderAPI(private val liteEco: LiteEco, private val extVersion: String) : PlaceholderExpansion() {

    override fun getIdentifier(): String = "liteeco"

    override fun getAuthor(): String = "EncryptSL"

    override fun getVersion(): String = extVersion

    override fun getRequiredPlugin(): String = liteEco.name

    override fun persist(): Boolean = true

    override fun canRegister(): Boolean {
        return liteEco.server.pluginManager.isPluginEnabled(requiredPlugin)
    }

    override fun onRequest(player: OfflinePlayer?, identifier: String): String? {
        if (player == null) return null
        val args = identifier.split("_")
        val helper = liteEco.placeholderHelper

        return when {

            // Format: total_balance_dollars
            identifier.startsWith("total_balance") -> {
                val curr = helper.parseCurrency(args, 2)
                liteEco.currencyImpl.formatted(helper.getTotalBalance(curr))
            }

            // Formats: liteeco_balance, liteeco_balance_formatted, liteeco_balance_compacted
            identifier.startsWith("balance") -> {
                val isFormatted = identifier.contains("formatted")
                val isCompacted = identifier.contains("compacted")

                // Currency index shifts based on the prefix length
                // e.g., liteeco_balance_dollars (index 1) vs liteeco_balance_formatted_dollars (index 2)
                val currencyIndex = if (isFormatted || isCompacted) 2 else 1
                val curr = helper.parseCurrency(args, currencyIndex)
                val bal = helper.getBalance(player.uniqueId, curr)

                when {
                    isFormatted -> liteEco.currencyImpl.fullFormatting(bal, curr)
                    isCompacted -> liteEco.currencyImpl.compacted(bal)
                    else -> bal.toPlainString()
                }
            }
            // Formats: liteeco_top_formatted_1_dollars, liteeco_top_compacted_1_dollars, liteeco_top_balance_1_dollars
            identifier.startsWith("top_") -> {
                val rank = helper.parseRank(args, 2) ?: return null
                val curr = helper.parseCurrency(args, 3)
                val bal = helper.getBalanceByRank(rank, curr)

                when {
                    identifier.startsWith("top_formatted") -> liteEco.currencyImpl.fullFormatting(bal, curr)
                    identifier.startsWith("top_compacted") -> liteEco.currencyImpl.compacted(bal)
                    identifier.startsWith("top_balance") -> bal.toPlainString()
                    identifier.startsWith("top_player") -> helper.getNameByRank(rank, curr)
                    else -> null
                }
            }

            // --- 4. TOP RANK PLAYER (Special Case) ---
            // Format: %liteeco_top_rank_player_dollars%
            identifier.startsWith("top_rank_player") -> {
                val curr = helper.parseCurrency(args, 3)
                helper.getNameByRank(1, curr)
            }

            else -> null
        }
    }
}
