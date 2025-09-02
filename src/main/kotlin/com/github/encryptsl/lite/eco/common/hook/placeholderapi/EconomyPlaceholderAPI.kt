package com.github.encryptsl.lite.eco.common.hook.placeholderapi

import com.github.encryptsl.lite.eco.LiteEco
import kotlinx.coroutines.runBlocking
import me.clip.placeholderapi.expansion.PlaceholderExpansion
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
        return liteEco.server.pluginManager.isPluginEnabled(requiredPlugin)
    }

    override fun onRequest(player: OfflinePlayer?, identifier: String): String? {
        if (player == null) return null
        val args = identifier.split("_")
        val rank = args.getOrNull(2)?.toIntOrNull()

        return when (identifier) {
            "balance" -> runBlocking { liteEco.api.getBalance(player.uniqueId, liteEco.currencyImpl.defaultCurrency()).toString() }
            "balance_formatted" -> runBlocking { liteEco.currencyImpl.fullFormatting(liteEco.api.getBalance(player.uniqueId, liteEco.currencyImpl.defaultCurrency())) }
            "balance_compacted" -> runBlocking { liteEco.currencyImpl.compacted(liteEco.api.getBalance(player.uniqueId, liteEco.currencyImpl.defaultCurrency())) }
            "top_rank_player" -> nameByRank(1, liteEco.currencyImpl.defaultCurrency())
            else -> {
                when {
                    identifier.startsWith("balance_") -> {
                        if (identifier.startsWith("balance_formatted_")) {
                            val currency = extractPlaceholderIdentifierName(2, args)
                            return runBlocking { liteEco.currencyImpl.fullFormatting(liteEco.api.getBalance(player.uniqueId, currency), currency) }
                        }

                        if (identifier.startsWith("balance_compacted_")) {
                            val currency = extractPlaceholderIdentifierName(2, args)
                            return runBlocking { liteEco.currencyImpl.compacted(liteEco.api.getBalance(player.uniqueId, currency)) }
                        }
                        val currency = extractPlaceholderIdentifierName(1, args)

                        return runBlocking { liteEco.api.getBalance(player.uniqueId, currency).toString() }
                    }
                    identifier.startsWith("top_rank_player_") -> {
                        val currency = extractPlaceholderIdentifierName(3, args)
                        return nameByRank(1, currency)
                    }
                    identifier.startsWith("total_balance_") -> {
                        val currency = extractPlaceholderIdentifierName(2, args)
                        return liteEco.currencyImpl.getCurrencyFormat(totalBalanceOfServerByCurrency(currency))
                    }
                }
                return rank?.let {
                    when {
                        identifier.startsWith("top_formatted_") -> {
                            val currency = extractPlaceholderIdentifierName(3, args)
                            return liteEco.currencyImpl.fullFormatting(balanceByRank(rank, currency), currency)
                        }
                        identifier.startsWith("top_compacted_") -> {
                            val currency = extractPlaceholderIdentifierName(3, args)
                            return liteEco.currencyImpl.compacted(balanceByRank(rank, currency))
                        }
                        identifier.startsWith("top_balance_") -> {
                            val currency = extractPlaceholderIdentifierName(3, args)
                            return balanceByRank(rank, currency).toString()
                        }
                        identifier.startsWith("top_player_") -> {
                            val currency = extractPlaceholderIdentifierName(3, args)
                            return nameByRank(rank, currency)
                        }
                        else -> null
                    }
                }
            }
        }
    }

    private fun extractPlaceholderIdentifierName(position: Int, args: List<String>): String {
        return Optional.ofNullable(args.getOrNull(position))
            .orElse(liteEco.currencyImpl.defaultCurrency())
    }

    private fun nameByRank(rank: Int, currency: String): String {
        val topBalance = topBalance(currency)
        return if (rank in 1..topBalance.size) {
            val name = topBalance.keys.elementAt(rank - 1)
            Optional.ofNullable(name).orElse("UNKNOWN")
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
