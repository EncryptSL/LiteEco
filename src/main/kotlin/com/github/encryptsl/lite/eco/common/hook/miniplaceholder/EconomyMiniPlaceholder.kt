package com.github.encryptsl.lite.eco.common.hook.miniplaceholder

import com.github.encryptsl.lite.eco.LiteEco
import io.github.miniplaceholders.kotlin.asInsertingTag
import io.github.miniplaceholders.kotlin.expansion
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.Component
import org.bukkit.OfflinePlayer
import java.math.BigDecimal
import java.util.*


class EconomyMiniPlaceholder(private val liteEco: LiteEco) {

    fun register() {
        val expansion = expansion("lite-eco") {
            audiencePlaceholder("balance") { p, s, _ ->
                val player: OfflinePlayer = p as OfflinePlayer
                val currency = if (s.hasNext()) s.pop().value() else liteEco.currencyImpl.defaultCurrency()
                if (!liteEco.currencyImpl.getCurrencyNameExist(currency))
                    return@audiencePlaceholder null
                return@audiencePlaceholder runBlocking { Component.text(liteEco.api.getBalance(player.uniqueId, currency).toPlainString()).asInsertingTag() }
            }
            audiencePlaceholder("balance_formatted") { p, s, _ ->
                val player: OfflinePlayer = p as OfflinePlayer
                val currency = if (s.hasNext()) s.pop().value() else liteEco.currencyImpl.defaultCurrency()
                if (!liteEco.currencyImpl.getCurrencyNameExist(currency))
                    return@audiencePlaceholder null

                return@audiencePlaceholder runBlocking { Component.text(liteEco.currencyImpl.fullFormatting(liteEco.api.getBalance(player.uniqueId, currency))).asInsertingTag() }
            }
            audiencePlaceholder("balance_compacted") { p, s, _ ->
                val player: OfflinePlayer = p as OfflinePlayer
                val currency = if (s.hasNext()) s.pop().value() else liteEco.currencyImpl.defaultCurrency()
                return@audiencePlaceholder runBlocking { Component.text(liteEco.currencyImpl.fullFormatting(liteEco.api.getBalance(player.uniqueId, currency))).asInsertingTag() }
            }
            globalPlaceholder("top_rank_player") { i, _ ->
                val currency = if (i.hasNext()) i.pop().value() else liteEco.currencyImpl.defaultCurrency()
                if (!liteEco.currencyImpl.getCurrencyNameExist(currency))
                    return@globalPlaceholder null

                return@globalPlaceholder Component.text(nameByRank(1, currency)).asInsertingTag()
            }
            globalPlaceholder("total_balance") { i, _ ->
                val currency = if (i.hasNext()) i.pop().value() else liteEco.currencyImpl.defaultCurrency()
                if (!liteEco.currencyImpl.getCurrencyNameExist(currency))
                    return@globalPlaceholder null

                return@globalPlaceholder Component.text(liteEco.currencyImpl.getCurrencyFormat(totalBalanceOfServerByCurrency(currency))).asInsertingTag()
            }
            globalPlaceholder("top_balance_formatted") { i, _ ->
                val argument = i.popOr("You need provide context").value().split("_")
                val rank = extractPlaceholderIdentifierName(0, argument).toInt()
                val currency = Optional.ofNullable(extractPlaceholderIdentifierName(1, argument)).orElse(liteEco.currencyImpl.defaultCurrency())
                if (!liteEco.currencyImpl.getCurrencyNameExist(currency))
                    return@globalPlaceholder null

                return@globalPlaceholder Component.text(liteEco.currencyImpl.fullFormatting(balanceByRank(rank, currency))).asInsertingTag()
            }
            globalPlaceholder("top_balance_compacted") { i, _ ->
                val argument = i.popOr("You need provide context").value().split("_")
                val rank = extractPlaceholderIdentifierName(0, argument).toInt()
                val currency = Optional.ofNullable(extractPlaceholderIdentifierName(1, argument)).orElse(liteEco.currencyImpl.defaultCurrency())
                if (!liteEco.currencyImpl.getCurrencyNameExist(currency))
                    return@globalPlaceholder null

                return@globalPlaceholder Component.text(liteEco.currencyImpl.compacted(balanceByRank(rank, currency))).asInsertingTag()
            }
            globalPlaceholder("top_balance") { i, _ ->
                val argument = i.popOr("You need provide context").value().split("_")
                val rank = extractPlaceholderIdentifierName(0, argument).toInt()
                val currency = Optional.ofNullable(extractPlaceholderIdentifierName(1, argument)).orElse(liteEco.currencyImpl.defaultCurrency())
                if (!liteEco.currencyImpl.getCurrencyNameExist(currency))
                    return@globalPlaceholder null

                return@globalPlaceholder Component.text(balanceByRank(rank, currency).toPlainString()).asInsertingTag()
            }
            globalPlaceholder("top_player") { i, _ ->
                val argument = i.popOr("You need provide context").value().split("_")
                val rank = extractPlaceholderIdentifierName(0, argument).toInt()
                val currency = Optional.ofNullable(extractPlaceholderIdentifierName(1, argument)).orElse(liteEco.currencyImpl.defaultCurrency())
                if (!liteEco.currencyImpl.getCurrencyNameExist(currency))
                    return@globalPlaceholder null

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
            liteEco.config.getString("formatting.placeholders.empty-name", "EMPTY").toString()
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

    private fun totalBalanceOfServerByCurrency(currency: String): String {
        return topBalance(currency).values.sumOf { it }.toString()
    }

    private fun topBalance(currency: String): Map<String, BigDecimal> {
        return liteEco.api.getTopBalance(currency)
    }

}