package com.github.encryptsl.lite.eco.api.interfaces

import com.github.encryptsl.lite.eco.common.database.entity.UserEntity
import java.math.BigDecimal
import java.util.*

interface IAccountHolder {

    /**
     * Atomically updates the balance of a player account using a transformation function.
     *
     * @param uuid UUID of the player
     * @param currency currency of the account (default "dollars")
     * @param transform functional block to calculate the new balance based on current balance
     * @return the newly calculated balance
     */
    suspend fun updateBalance(
        uuid: UUID,
        currency: String = "dollars",
        transform: (BigDecimal) -> BigDecimal
    ): BigDecimal

    /**
     * Retrieves a user by UUID and currency.
     *
     * @param uuid UUID of the player
     * @param currency currency name (default "dollars")
     * @return [UserEntity] containing the user if found, otherwise empty
     */
    suspend fun getUserByUUID(uuid: UUID, currency: String = "dollars"): UserEntity?

    /**
     * Deletes a player account if it exists.
     *
     * @param uuid UUID of the player
     * @param currency currency of the account
     * @return true if the account was deleted, false if it didn’t exist
     */
    suspend fun delete(uuid: UUID, currency: String = "dollars"): Boolean

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
     * @return true if synchronization and cache cleanup succeeded, false otherwise
     */
    suspend fun sync(uuid: UUID, shouldUnload: Boolean = false): Boolean

    /** Synchronizes all accounts at once. */
    fun syncAccounts()

    /**
     * Transfers money between two player accounts.
     *
     * @param sender UUID of the sender
     * @param target UUID of the target player
     * @param currency currency of the account
     * @param amount amount to transfer
     * @return true if the transfer was successful, false otherwise
     */
    suspend fun transfer(sender: UUID, target: UUID, currency: String = "dollars", amount: BigDecimal): Boolean

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
}