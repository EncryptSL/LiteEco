package com.github.encryptsl.lite.eco.api.interfaces

import com.github.encryptsl.lite.eco.common.database.entity.User
import org.bukkit.OfflinePlayer
import java.math.BigDecimal
import java.util.*

interface LiteEconomyAPI {
    /**
     * Creates a player account in the database.
     *
     * @param player The offline player instance.
     * @param currency The name of the currency. Defaults to "dollars".
     * @param startAmount The initial amount to be added to the account.
     * @return `true` if the account was successfully created, otherwise `false`.
     */
    fun createAccount(player: OfflinePlayer, currency: String = "dollars", startAmount: BigDecimal): Boolean

    /**
     * Creates a player account in the database.
     *
     * @param uuid The player's UUID.
     * @param username The player's username.
     * @param currency The name of the currency. Defaults to "dollars".
     * @param startAmount The initial amount to be added to the account.
     * @return `true` if the account was successfully created, otherwise `false`.
     */
    fun createAccount(uuid: UUID, username: String, currency: String = "dollars", startAmount: BigDecimal): Boolean

    /**
     * Caches a player's account during login.
     *
     * @param uuid The player's UUID.
     * @param currency The name of the currency. Defaults to "dollars".
     * @param amount The account balance from the database.
     */
    fun cacheAccount(uuid: UUID, currency: String = "dollars", amount: BigDecimal)

    /**
     * Deletes a player's account from the database.
     *
     * @param uuid The player's UUID.
     * @param currency The name of the currency. Defaults to "dollars".
     * @return `true` if the account was successfully deleted, otherwise `false`.
     */
    fun deleteAccount(uuid: UUID, currency: String = "dollars" ): Boolean

    /**
     * Checks if a player has an account in the database.
     *
     * @param uuid The player's UUID.
     * @param currency The name of the currency. Defaults to "dollars".
     * @return `true` if the account exists, otherwise `false`.
     */
    fun hasAccount(uuid: UUID, currency: String = "dollars" ): Boolean

    /**
     * Checks if a player has at least a certain amount of money.
     *
     * @param uuid The player's UUID.
     * @param currency The name of the currency. Defaults to "dollars".
     * @param amount The amount to check against.
     * @return `true` if the player has at least the specified amount, otherwise `false`.
     */
    fun has(uuid: UUID, currency: String = "dollars", amount: BigDecimal): Boolean

    /**
     * Retrieves a player's account by UUID.
     *
     * @param uuid The player's UUID.
     * @param currency The name of the currency. Defaults to "dollars".
     * @return An [Optional] containing the [User], if found.
     */
    fun getUserByUUID(uuid: UUID, currency: String = "dollars" ): Optional<User>

    /**
     * Retrieves the balance of a player's account.
     *
     * @param uuid The player's UUID.
     * @param currency The name of the currency. Defaults to "dollars".
     * @return The account balance.
     */
    fun getBalance(uuid: UUID, currency: String = "dollars" ): BigDecimal

    /**
     * Checks whether the specified amount exceeds the allowed balance limit.
     *
     * @param amount The amount to check.
     * @param currency The name of the currency. Defaults to "dollars".
     * @return `true` if the amount is within the limit, otherwise `false`.
     */
    fun getCheckBalanceLimit(amount: BigDecimal, currency: String = "dollars" ): Boolean

    /**
     * Checks whether the specified new balance exceeds the allowed balance limit for a player.
     *
     * @param uuid The player's UUID.
     * @param currentBalance The player's current balance.
     * @param currency The name of the currency. Defaults to "dollars".
     * @param amount The new amount to add.
     * @return `true` if the resulting balance is within the limit, otherwise `false`.
     */
    fun getCheckBalanceLimit(uuid: UUID, currentBalance: BigDecimal, currency: String = "dollars", amount: BigDecimal): Boolean

    /**
     * Deposits money into a player's account.
     *
     * @param uuid The player's UUID.
     * @param currency The name of the currency. Defaults to "dollars".
     * @param amount The amount to deposit.
     */
    fun depositMoney(uuid: UUID, currency: String = "dollars", amount: BigDecimal)

    /**
     * Withdraws money from a player's account.
     *
     * @param uuid The player's UUID.
     * @param currency The name of the currency. Defaults to "dollars".
     * @param amount The amount to withdraw.
     */
    fun withDrawMoney(uuid: UUID, currency: String = "dollars", amount: BigDecimal)

    /**
     * Transfers money from one player to another.
     *
     * @param fromUUID The UUID of the sender.
     * @param target The UUID of the receiver.
     * @param currency The name of the currency. Defaults to "dollars".
     * @param amount The amount to transfer.
     */
    fun transfer(fromUUID: UUID, target: UUID, currency: String = "dollars", amount: BigDecimal)

    /**
     * Sets a fixed amount of money for a player's account.
     *
     * @param uuid The player's UUID.
     * @param currency The name of the currency. Defaults to "dollars".
     * @param amount The amount to set.
     */
    fun setMoney(uuid: UUID, currency: String = "dollars", amount: BigDecimal)

    /**
     * Synchronizes a player's cached account with the database.
     *
     * @param uuid The player's UUID.
     * @param currency The name of the currency. Defaults to "dollars".
     */
    fun syncAccount(uuid: UUID, currency: String = "dollars")

    /**
     * Synchronizes all cached accounts with the database.
     */
    fun syncAccounts()

    /**
     * Retrieves the top player accounts by balance.
     *
     * @param currency The name of the currency. Defaults to "dollars".
     * @return A map of usernames to their balances.
     */
    fun getTopBalance(currency: String = "dollars" ): Map<String, BigDecimal>

    /**
     * Retrieves a map of UUIDs to player names.
     *
     * @param currency The name of the currency. Defaults to "dollars".
     * @return A mutable map of UUIDs to player names.
     */
    fun getUUIDNameMap(currency: String = "dollars" ): MutableMap<UUID, String>

    /**
     * Formats a value to a compact money representation.
     *
     * @param amount The amount to format.
     * @return A compacted string representation.
     */
    fun compacted(amount: BigDecimal): String

    /**
     * Formats a value to a human-readable money representation.
     *
     * @param amount The amount to format.
     * @return A formatted string representation.
     */
    fun formatted(amount: BigDecimal): String

    /**
     * Formats a value to a full string representation including the currency.
     *
     * @param amount The amount to format.
     * @param currency The name of the currency. Defaults to "dollars".
     * @return A fully formatted currency string.
     */
    fun fullFormatting(amount: BigDecimal, currency: String = "dollars" ): String
}