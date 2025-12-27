package com.github.encryptsl.lite.eco.common.hook.miniplaceholder

import com.github.encryptsl.lite.eco.LiteEco
import io.github.miniplaceholders.kotlin.asInsertingTag
import io.github.miniplaceholders.kotlin.audience
import io.github.miniplaceholders.kotlin.expansion
import io.github.miniplaceholders.kotlin.global
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import java.math.BigDecimal


class EconomyMiniPlaceholder(private val liteEco: LiteEco) {

    private fun BigDecimal.toTag() = Component.text(this.toPlainString()).asInsertingTag()
    private fun String.toTag() = Component.text(this).asInsertingTag()

    fun register() {
        val expansion = expansion("lite-eco") {
            audience<Player>("balance") { p, s, _ ->
                val currency = liteEco.placeholderHelper.parseCurrency(s)
                liteEco.placeholderHelper.getBalance(p.uniqueId, currency).toTag()
            }

            audience<Player>("balance_formatted") { p, s, _ ->
                val currency = liteEco.placeholderHelper.parseCurrency(s)
                liteEco.currencyImpl.fullFormatting(liteEco.placeholderHelper.getBalance(p.uniqueId, currency)).toTag()
            }

            audience<Player>("balance_compacted") { p, s, _ ->
                val currency = liteEco.placeholderHelper.parseCurrency(s)
                liteEco.currencyImpl.compacted(liteEco.placeholderHelper.getBalance(p.uniqueId, currency)).toTag()
            }

            global("top_rank_player") { i, _ ->
                val currency = liteEco.placeholderHelper.parseCurrency(i)
                liteEco.placeholderHelper.getNameByRank(1, currency).toTag()
            }

            global("total_balance") { i, _ ->
                val currency = liteEco.placeholderHelper.parseCurrency(i)
                liteEco.currencyImpl.formatted(liteEco.placeholderHelper.getTotalBalance(currency)).toTag()
            }

            global("top_balance_formatted") { i, _ ->
                val (rank, currency) = liteEco.placeholderHelper.parseRankAndCurrency(i) ?: return@global null
                liteEco.currencyImpl.fullFormatting(liteEco.placeholderHelper.getBalanceByRank(rank, currency)).toTag()
            }

            global("top_balance_compacted") { i, _ ->
                val (rank, currency) = liteEco.placeholderHelper.parseRankAndCurrency(i) ?: return@global null
                liteEco.currencyImpl.compacted(liteEco.placeholderHelper.getBalanceByRank(rank, currency)).toTag()
            }

            global("top_balance") { i, _ ->
                val (rank, currency) = liteEco.placeholderHelper.parseRankAndCurrency(i) ?: return@global null
                liteEco.placeholderHelper.getBalanceByRank(rank, currency).toPlainString().toTag()
            }

            global("top_player") { i, _ ->
                val (rank, currency) = liteEco.placeholderHelper.parseRankAndCurrency(i) ?: return@global null
                liteEco.placeholderHelper.getNameByRank(rank, currency).toTag()
            }
        }
        expansion.register()
    }

}