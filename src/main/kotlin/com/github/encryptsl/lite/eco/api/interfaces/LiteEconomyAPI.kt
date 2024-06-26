package com.github.encryptsl.lite.eco.api.interfaces

import com.github.encryptsl.lite.eco.common.database.entity.User
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.util.concurrent.CompletableFuture

interface LiteEconomyAPI {
    /**
     * Create player account to database
     * @param player is OfflinePlayer
     * @param startAmount an amount added to player when accounts is created.
     * @return Boolean
     * @see Boolean
     * @see OfflinePlayer
     */
    fun createAccount(player: OfflinePlayer, startAmount: Double): Boolean

    /**
     * Cache player account during login
     * @param player is OfflinePlayer
     * @param amount is value of player account from database.
     * @return Boolean
     * @see OfflinePlayer
     * @see com.github.encryptsl.lite.eco.common.database.models.DatabaseEcoModel.getUserByUUID(uuid)
     */
    fun cacheAccount(player: OfflinePlayer, amount: Double)

    /**
     * Delete player account from database
     * @param player is OfflinePlayer
     * @return Boolean
     * @see OfflinePlayer
     */
    fun deleteAccount(player: OfflinePlayer): Boolean

    /**
     * Boolean for check if player have account in database
     * @param player is {@link OfflinePlayer}
     * @return Boolean
     * @see OfflinePlayer
     */
    fun hasAccount(player: OfflinePlayer): CompletableFuture<Boolean>

    /**
     * Boolean for check if player have enough money
     * @param player is OfflinePlayer
     * @return Boolean
     * @see OfflinePlayer
     */
    fun has(player: OfflinePlayer, amount: Double): Boolean

    /**
     * Get user account
     * @param player is OfflinePlayer
     * @return User
     * @see OfflinePlayer
     */
    fun getUserByUUID(player: OfflinePlayer): CompletableFuture<User>

    /**
     * Get balance of player account
     * @param player is OfflinePlayer
     * @return Double
     * @see OfflinePlayer
     */
    fun getBalance(player: OfflinePlayer): Double

    /**
     * Get check limit of player balance.
     * @param amount is Double
     * @return Boolean
     * @see OfflinePlayer
     */
    fun getCheckBalanceLimit(amount: Double): Boolean

    /**
     * Get check limit of player balance.
     * @param player is OfflinePlayer
     * @return Boolean
     * @see OfflinePlayer
     */
    fun getCheckBalanceLimit(player: OfflinePlayer, amount: Double): Boolean

    /**
     * Deposit money to player account
     * @param player is OfflinePlayer
     * @param amount is amount added to player account
     * @see OfflinePlayer
     */
    fun depositMoney(player: OfflinePlayer, amount: Double)

    /**
     * Withdraw money from player account
     * @param player is OfflinePlayer
     * @param amount is amount removed from player account
     * @see OfflinePlayer
     */
    fun withDrawMoney(player: OfflinePlayer, amount: Double)

    /**
     * Transfer of money from player account to another player account.
     * @param fromPlayer is sender Player
     * @param target is offline player who receive money.
     * @see Player
     * @see OfflinePlayer
     */
    fun transfer(fromPlayer: Player, target: OfflinePlayer, amount: Double)

    /**
     * Set fixed money to player account
     * @param player is OfflinePlayer
     * @param amount is amount fixed value
     * @see OfflinePlayer
     */
    fun setMoney(player: OfflinePlayer, amount: Double)

    /**
     * Synchronize cache with database
     * @param offlinePlayer is OfflinePlayer
     * @see OfflinePlayer
     */
    fun syncAccount(offlinePlayer: OfflinePlayer)

    /**
     * Synchronize all saved data in cache with database
     */
    fun syncAccounts()

    /**
     * Get top player accounts
     * @return MutableMap
     */
    fun getTopBalance(): Map<String, Double>

    /**
     * Compacted money value
     * @param amount is only formatted to a compacted value
     * @return String
     */
    fun compacted(amount: Double): String

    /**
     * Formatted money value
     * @param amount is formatted to readable value
     * @return String
     */
    fun formatted(amount: Double): String

    /**
     * Formatting currency value
     * @param amount is formatted to readable value with currency prefix and name
     * @return String
     */
    fun fullFormatting(amount: Double): String

}