package com.github.encryptsl.lite.eco.api.interfaces

import com.github.encryptsl.lite.eco.common.database.entity.UserEntity
import java.math.BigDecimal
import java.util.*

/**
 * Interface for accessing and managing player data within an SQL database.
 *
 * This interface defines the core operations for manipulating player accounts,
 * including creation, updates, retrieving player information, and managing
 * their monetary balances.
 *
 * Implementations should ensure secure and efficient interaction with the database.
 */
interface PlayerSQL {

    /**
     * Creates a new player account in the database.
     *
     * @param username The username of the player.
     * @param uuid The unique identifier (UUID) of the player.
     * @param currency The key/name of the currency the account belongs to.
     * @param money The initial balance for the player's account.
     */
    fun createPlayerAccount(username: String, uuid: UUID, currency: String, money: BigDecimal)

    /**
     * Updates the username of a player based on their UUID and currency.
     *
     * @param uuid The unique identifier (UUID) of the player whose name should be updated.
     * @param username The new username for the player.
     * @param currency The key/name of the currency the account belongs to.
     */
    fun updatePlayerName(uuid: UUID, username: String, currency: String)

    /**
     * Retrieves user data (a [UserEntity] object) by the player's UUID and currency.
     *
     * @param uuid The unique identifier (UUID) of the player.
     * @param currency The key/name of the currency the account belongs to.
     * @return The [UserEntity] object containing player data, or `null` if the account does not exist.
     */
    fun getUserByUUID(uuid: UUID, currency: String): UserEntity?

    /**
     * Retrieves the current monetary balance of a player.
     *
     * @param uuid The unique identifier (UUID) of the player.
     * @param currency The key/name of the currency the account belongs to.
     * @return The player's current balance as a [BigDecimal].
     */
    fun getBalance(uuid: UUID, currency: String): BigDecimal

    /**
     * Deletes the player's account for the specified currency from the database.
     *
     * @param uuid The unique identifier (UUID) of the player whose account should be deleted.
     * @param currency The key/name of the currency the account belongs to.
     */
    fun deletePlayerAccount(uuid: UUID, currency: String)

    /**
     * Checks whether a player account exists for the given UUID and currency.
     *
     * @param uuid The unique identifier (UUID) of the player.
     * @param currency The key/name of the currency the account belongs to.
     * @return `true` if the account exists, otherwise `false`.
     */
    fun getExistPlayerAccount(uuid: UUID, currency: String): Boolean

    /**
     * Retrieves a map of players with the highest balance for a given currency (a leaderboard).
     *
     * @param currency The key/name of the currency to check the balance for.
     * @return A mutable map where the key is the player's name ([String])
     * and the value is the [UserEntity] object.
     */
    fun getTopBalance(currency: String): MutableMap<String, UserEntity>

    /**
     * Retrieves a map of UUIDs and names for all players who have an account in the given currency.
     *
     * @param currency The key/name of the currency the accounts belong to.
     * @return A mutable map where the key is the player's UUID and the value is their name.
     */
    fun getUUIDNameMap(currency: String): MutableMap<UUID, String>

    /**
     * Retrieves a collection of all player UUIDs who have an account in the specified currency.
     *
     * @param currency The key/name of the currency the accounts belong to.
     * @return A mutable collection of player UUIDs.
     */
    fun getPlayersIds(currency: String): MutableCollection<UUID>

    /**
     * Adds (deposits) money to the player's account.
     *
     * @param uuid The unique identifier (UUID) of the player.
     * @param currency The key/name of the currency the account belongs to.
     * @param money The [BigDecimal] amount to add.
     */
    fun deposit(uuid: UUID, currency: String, money: BigDecimal)

    /**
     * Subtracts (withdraws) money from the player's account.
     *
     * *Note: The implementation should handle negative balances if required.*
     *
     * @param uuid The unique identifier (UUID) of the player.
     * @param currency The key/name of the currency the account belongs to.
     * @param money The [BigDecimal] amount to subtract.
     */
    fun withdraw(uuid: UUID, currency: String, money: BigDecimal)

    /**
     * Sets the exact monetary balance value for the player's account.
     *
     * @param uuid The unique identifier (UUID) of the player.
     * @param currency The key/name of the currency the account belongs to.
     * @param money The new balance value as a [BigDecimal].
     */
    fun set(uuid: UUID, currency: String, money: BigDecimal)

    /**
     * Deletes **all** accounts related to the specified currency.
     *
     * *Use with extreme caution!*
     *
     * @param currency The key/name of the currency whose accounts should be purged.
     */
    fun purgeAccounts(currency: String)

    /**
     * Deletes accounts that have the **default balance** for the specified currency.
     *
     * This is typically used to clean up unused/inactive accounts.
     *
     * @param defaultMoney The [BigDecimal] default balance value accounts must have to be deleted.
     * @param currency The key/name of the currency the accounts belong to.
     */
    fun purgeDefaultAccounts(defaultMoney: BigDecimal, currency: String)

    /**
     * Deletes accounts that are considered invalid (e.g., accounts with duplicate
     * UUIDs or other database inconsistencies, depending on the implementation).
     *
     * @param currency The key/name of the currency the accounts belong to.
     */
    fun purgeInvalidAccounts(currency: String)

    /**
     * Executes a batch insertion or replacement of player records directly in the database.
     *
     * This method leverages SQL batching to process multiple records (UUID, username, balance)
     * in a single transaction, minimizing overhead and maximizing throughput.
     *
     * @param importData A list of [Triple] containing the player's UUID, username, and balance.
     * @param currency The key/name of the currency the accounts belong to.
     */
    fun batchInsert(importData: List<Triple<UUID, String, BigDecimal>>, currency: String)
}