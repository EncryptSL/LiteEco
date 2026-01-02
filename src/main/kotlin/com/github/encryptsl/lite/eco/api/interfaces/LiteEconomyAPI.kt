package com.github.encryptsl.lite.eco.api.interfaces

import com.github.encryptsl.lite.eco.common.database.entity.UserEntity
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
     * Retrieves a user by UUID and currency.
     *
     * @param uuid UUID of the player
     * @param currency currency name (default "dollars")
     * @return [Optional] containing the user if found, otherwise empty
     */
    suspend fun getUserByUUID(uuid: UUID, currency: String = "dollars"): Optional<UserEntity>

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
     * Deletes a player account if it exists.
     *
     * @param uuid UUID of the player
     * @param currency currency of the account
     * @return true if the account was deleted, false if it didn’t exist
     */
    suspend fun deleteAccount(uuid: UUID, currency: String = "dollars"): Boolean

    /** Deletes all accounts for the specified currency. */
    suspend fun purgeAccounts(currency: String)

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
     * Withdraws money from a player account.
     *
     * @param uuid UUID of the player
     * @param currency currency of the account
     * @param amount amount to withdraw
     */
    suspend fun withdraw(uuid: UUID, currency: String = "dollars", amount: BigDecimal)

    /**
     * Deposits money into a player account.
     *
     * @param uuid UUID of the player
     * @param currency currency of the account
     * @param amount amount to deposit
     */
    suspend fun deposit(uuid: UUID, currency: String = "dollars", amount: BigDecimal)

    /**
     * Sets the balance of a player account.
     *
     * @param uuid UUID of the player
     * @param currency currency of the account
     * @param amount new balance value
     */
    suspend fun set(uuid: UUID, currency: String = "dollars", amount: BigDecimal)

    /**
     * Synchronizes a single account between cache and database.
     *
     * @param uuid UUID of the player
     */
    suspend fun syncAccount(uuid: UUID)

    /** Synchronizes all accounts at once. */
    fun syncAccounts()

    /**
     * Transfers money between two player accounts.
     *
     * @param sender UUID of the sender
     * @param target UUID of the target player
     * @param currency currency of the account
     * @param amount amount to transfer
     */
    suspend fun transfer(sender: UUID, target: UUID, currency: String = "dollars", amount: BigDecimal)

    /**
     * Gets the current balance of a player.
     *
     * @param uuid UUID of the player
     * @param currency currency of the account
     * @return the player’s balance or 0 if no account exists
     */
    suspend fun getBalance(uuid: UUID, currency: String = "dollars"): BigDecimal

    /**
     * Checks whether a player has an account for the given currency.
     *
     * @param uuid UUID of the player
     * @param currency currency of the account
     * @return true if the account exists, false otherwise
     */
    fun hasAccount(uuid: UUID, currency: String = "dollars"): Boolean

    /**
     * Checks whether a player has at least the required amount.
     *
     * @param uuid UUID of the player
     * @param currency currency of the account
     * @param requiredAmount amount to check against
     * @return true if the player has enough, false otherwise
     */
    fun has(uuid: UUID, currency: String = "dollars", requiredAmount: BigDecimal): Boolean

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
}