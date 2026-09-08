package com.github.encryptsl.lite.eco.api.interfaces

import com.github.encryptsl.lite.eco.api.migrator.entity.PlayerBalances
import java.math.BigDecimal
import java.util.*

/**
 * API interface for managing player economy accounts in LiteEco.
 *
 * This interface combines both suspendable (asynchronous, coroutine-friendly)
 * and synchronous methods (used mainly for Vault and other integrations).
 */
interface LiteEconomyAPI {

    /**
     * Creates or updates a player account.
     *
     * @param uuid UUID of the player
     * @param username the player's username
     * @param currency currency of the account
     * @param value initial balance value
     * @param ignoreUpdate if true, the player name will not be updated if the account exists
     * @return true if a new account was created, false if the account already existed
     */
    suspend fun createOrUpdateAccount(
        uuid: UUID,
        username: String,
        currency: String = "dollars",
        value: BigDecimal,
        ignoreUpdate: Boolean = true
    ): Boolean

    /**
     * Creates or updates a player account and caches it.
     *
     * @param uuid UUID of the player
     * @param username the player's username
     * @param currency currency of the account
     * @param start initial balance value
     */
    suspend fun createOrUpdateAndCache(
        uuid: UUID,
        username: String,
        currency: String = "dollars",
        start: BigDecimal
    )

    /**
     * Deletes **all** player accounts for the specified currency.
     *
     * *Use with extreme caution!*
     *
     * @param currency The key or name of the currency whose accounts should be purged.
     * @return The total number of accounts deleted.
     */
    suspend fun purgeAccounts(currency: String): Int

    /**
     * Deletes **all** test accounts created by the `/eco debug stress-shutdown` command.
     *
     * @return The total number of test accounts deleted.
     */
    suspend fun purgeTestAccounts(): Int

    /**
     * Deletes accounts that hold the **default balance** for the specified currency.
     *
     * Typically used to clean up inactive or unused player accounts that have not made any transactions.
     *
     * @param currency The key or name of the currency the accounts belong to.
     * @param defaultValue The [BigDecimal] balance value considered as default.
     * @return The total number of default accounts deleted.
     */
    suspend fun purgeDefaultAccounts(currency: String, defaultValue: BigDecimal): Int

    /**
     * Deletes all invalid or corrupted accounts (e.g., missing essential fields or broken data).
     *
     * @param currency The key or name of the currency to scan for invalid entries.
     * @return The total number of invalid accounts deleted.
     */
    suspend fun purgeInvalidAccounts(currency: String): Int

    /**
     * Retrieves all player balances for the specified currency, merged with active in-memory cache states.
     *
     * This method fetches persistent records from the database in bulk and overlays any
     * unsaved balance modifications currently held in the memory cache for active player sessions.
     *
     * @param currency currency of the accounts (default "dollars")
     * @return a list of [PlayerBalances.PlayerBalance] containing up-to-date player balances
     */
    suspend fun getBalancesForCurrency(currency: String = "dollars"): List<PlayerBalances.PlayerBalance>


    /**
     * Performs a high-performance batch insertion or update of multiple accounts.
     * * This method is designed for large-scale data migrations (e.g., importing from other plugins).
     * It sends multiple records in a single SQL query, significantly reducing database latency.
     *
     * @param importData a list of [Triple] containing player UUID, username, and balance
     * @param currency currency name (default "dollars")
     */
    fun batchInsert(importData: List<Triple<UUID, String, BigDecimal>>, currency: String = "dollars")

    /**
     * Returns the top balances in the given currency.
     *
     * @param currency currency of the accounts
     * @return a map of playerName -> balance, sorted in descending order
     */
    fun getTopBalance(currency: String = "dollars"): Map<String, BigDecimal>

    /**
     * Returns a map of player UUIDs to usernames for the given currency.
     *
     * @param currency currency of the accounts
     * @return mutable map of UUID -> player name
     */
    fun getUUIDNameMap(currency: String = "dollars"): MutableMap<UUID, String>

    fun account(): IAccountHolder
}