package com.github.encryptsl.lite.eco.common.hook.miniplaceholder

import com.github.encryptsl.lite.eco.LiteEco
import io.github.miniplaceholders.kotlin.asInsertingTag
import io.github.miniplaceholders.kotlin.audience
import io.github.miniplaceholders.kotlin.expansion
import io.github.miniplaceholders.kotlin.global
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import java.math.BigDecimal
import java.util.*


class EconomyMiniPlaceholder(private val liteEco: LiteEco) {

    fun register() {
        val expansion = expansion("lite-eco") {
            audience<Player>("balance") { p, s, _ ->
                val currency = if (s.hasNext()) s.pop().value() else liteEco.currencyImpl.defaultCurrency()
                if (!liteEco.currencyImpl.getCurrencyNameExist(currency))
                    return@audience null

                return@audience Component.text(
                    balance(p.uniqueId, currency)
                ).asInsertingTag()
            }
            audience<Player>("balance_formatted") { p, s, _ ->
                val currency = if (s.hasNext()) s.pop().value() else liteEco.currencyImpl.defaultCurrency()
                if (!liteEco.currencyImpl.getCurrencyNameExist(currency))
                    return@audience null

                return@audience Component.text(
                    liteEco.currencyImpl.fullFormatting(balance(p.uniqueId, currency).toBigDecimal())
                ).asInsertingTag()
            }
            audience<Player>("balance_compacted") { p, s, _ ->
                val currency = if (s.hasNext()) s.pop().value() else liteEco.currencyImpl.defaultCurrency()
                return@audience Component.text(
                    liteEco.currencyImpl.fullFormatting(
                        balance(p.uniqueId, currency).toBigDecimal()
                    )
                ).asInsertingTag()
            }
            global("top_rank_player") { i, _ ->
                val currency = if (i.hasNext()) i.pop().value() else liteEco.currencyImpl.defaultCurrency()
                if (!liteEco.currencyImpl.getCurrencyNameExist(currency))
                    return@global null

                return@global Component.text(nameByRank(1, currency)).asInsertingTag()
            }
            global("total_balance") { i, _ ->
                val currency = if (i.hasNext()) i.pop().value() else liteEco.currencyImpl.defaultCurrency()
                if (!liteEco.currencyImpl.getCurrencyNameExist(currency))
                    return@global null

                return@global Component.text(
                    liteEco.currencyImpl.getCurrencyFormat(
                        totalBalanceOfServerByCurrency(currency)
                    )
                ).asInsertingTag()
            }
            global("top_balance_formatted") { i, _ ->
                val argument = i.popOr("You need provide context").value().split("_")
                val rank = extractPlaceholderIdentifierName(0, argument).toInt()
                val currency = Optional.ofNullable(extractPlaceholderIdentifierName(1, argument))
                    .orElse(liteEco.currencyImpl.defaultCurrency())
                if (!liteEco.currencyImpl.getCurrencyNameExist(currency))
                    return@global null

                return@global Component.text(
                    liteEco.currencyImpl.fullFormatting(
                        balanceByRank(
                            rank,
                            currency
                        )
                    )
                ).asInsertingTag()
            }
            global("top_balance_compacted") { i, _ ->
                val argument = i.popOr("You need provide context").value().split("_")
                val rank = extractPlaceholderIdentifierName(0, argument).toInt()
                val currency = Optional.ofNullable(extractPlaceholderIdentifierName(1, argument))
                    .orElse(liteEco.currencyImpl.defaultCurrency())
                if (!liteEco.currencyImpl.getCurrencyNameExist(currency))
                    return@global null

                return@global Component.text(liteEco.currencyImpl.compacted(balanceByRank(rank, currency)))
                    .asInsertingTag()
            }
            global("top_balance") { i, _ ->
                val argument = i.popOr("You need provide context").value().split("_")
                val rank = extractPlaceholderIdentifierName(0, argument).toInt()
                val currency = Optional.ofNullable(extractPlaceholderIdentifierName(1, argument))
                    .orElse(liteEco.currencyImpl.defaultCurrency())
                if (!liteEco.currencyImpl.getCurrencyNameExist(currency))
                    return@global null

                return@global Component.text(balanceByRank(rank, currency).toPlainString()).asInsertingTag()
            }
            global("top_player") { i, _ ->
                val argument = i.popOr("You need provide context").value().split("_")
                val rank = extractPlaceholderIdentifierName(0, argument).toInt()
                val currency = Optional.ofNullable(extractPlaceholderIdentifierName(1, argument))
                    .orElse(liteEco.currencyImpl.defaultCurrency())
                if (!liteEco.currencyImpl.getCurrencyNameExist(currency))
                    return@global null

                return@global Component.text(nameByRank(rank, currency)).asInsertingTag()
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

    private fun balance(uuid: UUID, currency: String): String = runBlocking { liteEco.api.getBalance(uuid, currency).toPlainString() }

    private fun totalBalanceOfServerByCurrency(currency: String): String {
        return topBalance(currency).values.sumOf { it }.toString()
    }

    private fun topBalance(currency: String): Map<String, BigDecimal> {
        return liteEco.api.getTopBalance(currency)
    }

}