package com.github.encryptsl.lite.eco.api.interfaces

import com.github.encryptsl.lite.eco.common.database.entity.User
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.math.BigDecimal
import java.util.concurrent.CompletableFuture

interface LiteEconomyAPI {
    /**
     * Create player account to database
     * @param player is OfflinePlayer
     * @param currency name of currency.
     * @param startAmount an amount added to player when accounts is created.
     * @return Boolean
     * @see Boolean
     * @see OfflinePlayer
     */
    fun createAccount(player: OfflinePlayer, currency: String = "dollars", startAmount: BigDecimal): Boolean

    /**
     * Cache player account during login
     * @param player is OfflinePlayer
     * @param currency name of currency
     * @param amount is value of player account from database.
     * @return Boolean
     * @see OfflinePlayer
     * @see com.github.encryptsl.lite.eco.common.database.models.DatabaseEcoModel.getUserByUUID(uuid)
     */
    fun cacheAccount(player: OfflinePlayer, currency: String = "dollars", amount: BigDecimal)

    /**
     * Delete player account from database
     * @param player is OfflinePlayer
     * @param currency name of currency
     * @return Boolean
     * @see OfflinePlayer
     */
    fun deleteAccount(player: OfflinePlayer, currency: String = "dollars"): Boolean

    /**
     * Boolean for check if player have account in database
     * @param player is {@link OfflinePlayer}
     * @param currency name of currency
     * @return Boolean
     * @see OfflinePlayer
     */
    fun hasAccount(player: OfflinePlayer, currency: String = "dollars"): CompletableFuture<Boolean>

    /**
     * Boolean for check if player have enough money
     * @param player is OfflinePlayer
     * @param currency name of currency
     * @return Boolean
     * @see OfflinePlayer
     */
    fun has(player: OfflinePlayer, currency: String = "dollars", amount: BigDecimal): Boolean

    /**
     * Get user account
     * @param player is OfflinePlayer
     * @param currency name of currency
     * @return User
     * @see OfflinePlayer
     */
    fun getUserByUUID(player: OfflinePlayer, currency: String = "dollars"): CompletableFuture<User>

    /**
     * Get balance of player account
     * @param player is OfflinePlayer
     * @param currency name of currency
     * @return Double
     * @see OfflinePlayer
     */
    fun getBalance(player: OfflinePlayer, currency: String = "dollars"): BigDecimal

    /**
     * Get check limit of player balance.
     * @param amount is BigDecimal
     * @param currency name of currency
     * @return Boolean
     * @see OfflinePlayer
     */
    fun getCheckBalanceLimit(amount: BigDecimal, currency: String = "dollars"): Boolean

    /**
     * Get check limit of player balance.
     * @param player is OfflinePlayer
     * @param currency name of currency
     * @return Boolean
     * @see OfflinePlayer
     */
    fun getCheckBalanceLimit(player: OfflinePlayer, currency: String = "dollars", amount: BigDecimal): Boolean

    /**
     * Deposit money to player account
     * @param player is OfflinePlayer
     * @param currency name of currency
     * @param amount is amount added to player account
     * @see OfflinePlayer
     */
    fun depositMoney(player: OfflinePlayer, currency: String = "dollars", amount: BigDecimal)

    /**
     * Withdraw money from player account
     * @param player is OfflinePlayer
     * @param currency name of currency
     * @param amount is amount removed from player account
     * @see OfflinePlayer
     */
    fun withDrawMoney(player: OfflinePlayer, currency: String = "dollars", amount: BigDecimal)

    /**
     * Transfer of money from player account to another player account.
     * @param fromPlayer is sender Player
     * @param target is offline player who receive money.
     * @param currency name of currency
     * @see Player
     * @see OfflinePlayer
     */
    fun transfer(fromPlayer: Player, target: OfflinePlayer, currency: String = "dollars", amount: BigDecimal)

    /**
     * Set fixed money to player account
     * @param player is OfflinePlayer
     * @param currency name of currency
     * @param amount is amount fixed value
     * @see OfflinePlayer
     */
    fun setMoney(player: OfflinePlayer, currency: String = "dollars", amount: BigDecimal)

    /**
     * Synchronize cache with database
     * @param offlinePlayer is OfflinePlayer
     * @see OfflinePlayer
     */
    fun syncAccount(offlinePlayer: OfflinePlayer, currency: String = "dollars")

    /**
     * Synchronize all saved data in cache with database
     */
    fun syncAccounts()

    /**
     * Get top player accounts
     * @param currency name of currency
     * @return MutableMap
     */
    fun getTopBalance(currency: String = "dollars"): Map<String, BigDecimal>

    /**
     * Compacted money value
     * @param amount is only formatted to a compacted value
     * @return String
     */
    fun compacted(amount: BigDecimal): String

    /**
     * Formatted money value
     * @param amount is formatted to readable value
     * @param currency name of currency
     * @return String
     */
    fun formatted(amount: BigDecimal, currency: String = "dollars"): String

    /**
     * Formatting currency value
     * @param amount is formatted to readable value with currency prefix and name
     * @param currency name of currency
     * @return String
     */
    fun fullFormatting(amount: BigDecimal, currency: String = "dollars"): String

}