package com.github.encryptsl.lite.eco.utils

import com.github.encryptsl.lite.eco.LiteEco
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue
import java.math.BigDecimal
import java.util.*

class PlaceholderHelper(
    private val liteEco: LiteEco
) {
    /**
     * Gets the player's balance for a specific currency.
     */
    fun getBalance(uuid: UUID, currency: String): BigDecimal = runBlocking {
        liteEco.api.getBalance(uuid, currency)
    }

    /**
     * Gets the name of a player at a specific rank in the leaderboards.
     */
    fun getNameByRank(rank: Int, currency: String): String {
        val top = liteEco.api.getTopBalance(currency)
        return top.keys.elementAtOrNull(rank - 1)
            ?: liteEco.config.getString("formatting.placeholders.empty-name", "EMPTY")!!
    }

    /**
     * Gets the balance of a player at a specific rank in the leaderboards.
     */
    fun getBalanceByRank(rank: Int, currency: String): BigDecimal {
        val top = liteEco.api.getTopBalance(currency)
        return top.values.elementAtOrNull(rank - 1) ?: BigDecimal.ZERO
    }

    /**
     * Calculates the total balance of all players for a specific currency.
     */
    fun getTotalBalance(currency: String): BigDecimal {
        return liteEco.api.getTopBalance(currency).values.sumOf { it }
    }

    // --- PARSING LAYER FOR PAPI (List<String> based) ---

    /**
     * Extracts currency from a list of arguments at a specific index.
     * Falls back to the default currency if not found or invalid.
     */
    fun parseCurrency(args: List<String>, index: Int): String {
        val name = args.getOrNull(index) ?: liteEco.currencyImpl.defaultCurrency()
        return if (liteEco.currencyImpl.getCurrencyNameExist(name)) name else liteEco.currencyImpl.defaultCurrency()
    }

    /**
     * Safely parses a rank integer from the argument list.
     */
    fun parseRank(args: List<String>, index: Int): Int? = args.getOrNull(index)?.toIntOrNull()

    // --- PARSING LAYER FOR MINIPLACEHOLDERS (ArgumentQueue based) ---

    /**
     * Extracts currency from the MiniPlaceholders argument queue.
     */
    fun parseCurrency(queue: ArgumentQueue): String {
        val name = if (queue.hasNext()) queue.pop().value() else liteEco.currencyImpl.defaultCurrency()
        return if (liteEco.currencyImpl.getCurrencyNameExist(name)) name else liteEco.currencyImpl.defaultCurrency()
    }

    /**
     * Parses both Rank and Currency from a single queue argument (e.g., "1_USD").
     */
    fun parseRankAndCurrency(queue: ArgumentQueue): Pair<Int, String>? {
        val arg = queue.popOr("Missing context").value().split("_")
        val rank = arg.getOrNull(0)?.toIntOrNull() ?: return null
        val currency = arg.getOrNull(1) ?: liteEco.currencyImpl.defaultCurrency()
        val finalCurrency = if (liteEco.currencyImpl.getCurrencyNameExist(currency)) currency else liteEco.currencyImpl.defaultCurrency()
        return rank to finalCurrency
    }
}