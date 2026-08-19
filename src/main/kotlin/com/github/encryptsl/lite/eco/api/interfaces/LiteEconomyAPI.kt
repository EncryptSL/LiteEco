package com.github.encryptsl.lite.eco.api.interfaces

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

    /** Deletes all accounts for the specified currency. */
    suspend fun purgeAccounts(currency: String)

    /** Deletes all accounts created by command eco debug stress-shutdown */
    suspend fun purgeTestAccounts()

    /** Deletes all invalid accounts (e.g., corrupted or broken data). */
    suspend fun purgeInvalidAccounts(currency: String)

    /**
     * Deletes all accounts that only hold the default value.
     *
     * @param currency currency of the accounts
     * @param defaultValue the value considered as "default"
     */
    suspend fun purgeDefaultAccounts(currency: String, defaultValue: BigDecimal)


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