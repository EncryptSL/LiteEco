package com.github.encryptsl.lite.eco.common.hook.miniplaceholder

import com.github.encryptsl.lite.eco.LiteEco
import io.github.miniplaceholders.kotlin.asInsertingTag
import io.github.miniplaceholders.kotlin.expansion
import net.kyori.adventure.text.Component
import org.bukkit.OfflinePlayer
import java.math.BigDecimal
import java.util.Optional


class EconomyMiniPlaceholder(private val liteEco: LiteEco) {

    fun register() {
        val expansion = expansion("lite-eco") {
            audiencePlaceholder("balance") { p, _, _ ->
                val player: OfflinePlayer = p as OfflinePlayer
                return@audiencePlaceholder Component.text(liteEco.api.getBalance(player).toPlainString()).asInsertingTag()
            }
            audiencePlaceholder("balance_formatted") { p, _, _ ->
                val player: OfflinePlayer = p as OfflinePlayer
                return@audiencePlaceholder Component.text(liteEco.api.fullFormatting(liteEco.api.getBalance(player))).asInsertingTag()
            }
            audiencePlaceholder("balance_compacted") { p, _, _ ->
                val player: OfflinePlayer = p as OfflinePlayer
                return@audiencePlaceholder Component.text(liteEco.api.fullFormatting(liteEco.api.getBalance(player))).asInsertingTag()
            }
            globalPlaceholder("top_rank_player") { i, _ ->
                val argument = i.popOr("You need provide currency").value().split("_")
                val currency = Optional.ofNullable(extractPlaceholderIdentifierName(0, argument)).orElse(liteEco.currencyImpl.defaultCurrency())
                return@globalPlaceholder Component.text(nameByRank(1, currency)).asInsertingTag()
            }
            globalPlaceholder("top_formatted") { i, _ ->
                val argument = i.popOr("You need provide context").value().split("_")
                val rank = extractPlaceholderIdentifierName(0, argument).toInt()
                val currency = Optional.ofNullable(extractPlaceholderIdentifierName(1, argument)).orElse(liteEco.currencyImpl.defaultCurrency())
                return@globalPlaceholder Component.text(liteEco.api.fullFormatting(balanceByRank(rank, currency))).asInsertingTag()
            }
            globalPlaceholder("top_compacted") { i, _ ->
                val argument = i.popOr("You need provide context").value().split("_")
                val rank = extractPlaceholderIdentifierName(0, argument).toInt()
                val currency = Optional.ofNullable(extractPlaceholderIdentifierName(1, argument)).orElse(liteEco.currencyImpl.defaultCurrency())
                return@globalPlaceholder Component.text(liteEco.api.compacted(balanceByRank(rank, currency))).asInsertingTag()
            }
            globalPlaceholder("top_balance") { i, _ ->
                val argument = i.popOr("You need provide context").value().split("_")
                val rank = extractPlaceholderIdentifierName(0, argument).toInt()
                val currency = Optional.ofNullable(extractPlaceholderIdentifierName(1, argument)).orElse(liteEco.currencyImpl.defaultCurrency())
                return@globalPlaceholder Component.text(balanceByRank(rank, currency).toPlainString()).asInsertingTag()
            }
            globalPlaceholder("top_player") { i, _ ->
                val argument = i.popOr("You need provide context").value().split("_")
                val rank = extractPlaceholderIdentifierName(0, argument).toInt()
                val currency = Optional.ofNullable(extractPlaceholderIdentifierName(1, argument)).orElse(liteEco.currencyImpl.defaultCurrency())
                return@globalPlaceholder Component.text(nameByRank(rank, currency)).asInsertingTag()
            }
        }
        expansion.register()
    }

    private fun extractPlaceholderIdentifierName(position: Int, list: List<String>): String {
        return list[position]
    }

    private fun nameByRank(rank: Int, currency: String): String {
        val topBalance = topBalance(currency)
        return if (rank in 1..topBalance.size) {
            topBalance.keys.elementAt(rank - 1)
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