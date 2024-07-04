package com.github.encryptsl.lite.eco.common.hook.miniplaceholder

import com.github.encryptsl.lite.eco.LiteEco
import io.github.miniplaceholders.kotlin.asInsertingTag
import io.github.miniplaceholders.kotlin.expansion
import net.kyori.adventure.text.Component
import org.bukkit.OfflinePlayer
import java.math.BigDecimal


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
            globalPlaceholder("top_rank_player") { _, _ ->
                return@globalPlaceholder Component.text(nameByRank(1)).asInsertingTag()
            }
            globalPlaceholder("top_formatted") { i, _ ->
                return@globalPlaceholder Component.text(liteEco.api.fullFormatting(balanceByRank(i.popOr("You need provide position.").value().toInt()))).asInsertingTag()
            }
            globalPlaceholder("top_compacted") { i, _ ->
                return@globalPlaceholder Component.text(liteEco.api.compacted(balanceByRank(i.popOr("You need provide position.").value().toInt()))).asInsertingTag()
            }
            globalPlaceholder("top_balance") { i, _ ->
                return@globalPlaceholder Component.text(balanceByRank(i.popOr("You need provide position.").value().toInt()).toPlainString()).asInsertingTag()
            }
            globalPlaceholder("top_player") { i, _ ->
                return@globalPlaceholder Component.text(nameByRank(i.popOr("You need provide position.").value().toInt())).asInsertingTag()
            }
        }
        expansion.register()
    }

    private fun nameByRank(rank: Int): String {
        val topBalance = topBalance()
        return if (rank in 1..topBalance.size) {
            topBalance.keys.elementAt(rank - 1)
        } else {
            "EMPTY"
        }
    }

    private fun balanceByRank(rank: Int): BigDecimal {
        val topBalance = topBalance()
        return if (rank in 1..topBalance.size) {
            topBalance.values.elementAt(rank - 1)
        } else {
            BigDecimal.ZERO
        }
    }

    private fun topBalance(): Map<String, BigDecimal> {
        return liteEco.api.getTopBalance()
    }

}