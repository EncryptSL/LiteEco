package com.github.encryptsl.lite.eco.api.interfaces

import com.github.encryptsl.lite.eco.LiteEco
import java.math.BigDecimal
import java.util.*

/**
 * Interface defining the primary API for managing and accessing player accounts,
 * focusing heavily on caching and synchronization operations.
 *
 * This API is typically used to provide fast access to player data (like balances)
 * without constantly hitting the main database (SQL).
 */
interface IAccount {

    /**
     * Atomically updates a player's cached balance using a transformation function.
     *
     * Performs a thread-safe read-modify-write operation directly in memory,
     * preventing race conditions during concurrent deposits or withdrawals.
     *
     * @param uuid The unique identifier (UUID) of the player.
     * @param currency The key/name of the currency to update.
     * @param transform The transformation function to compute the new balance.
     * @return The newly calculated [BigDecimal] balance.
     */
    fun updateBalance(uuid: UUID, currency: String, transform: (BigDecimal) -> BigDecimal): BigDecimal

    /**
     * Starts the Janitor service task that periodically synchronizes offline players' data.
     *
     * This service iterates through the cache and identifies players who are no longer online.
     * It attempts to synchronize their cached balances back to the database asynchronously.
     *
     * @param liteEco The plugin instance used to schedule the task and access the logger.
     */
    fun startJanitor(liteEco: LiteEco)

    /**
     * Caches the player's account balance and username in memory or a fast storage layer.
     *
     * @param uuid The unique identifier (UUID) of the player.
     * @param username The player's username.
     * @param currency The key/name of the currency being cached.
     * @param value The [BigDecimal] balance value to store in the cache.
     */
    fun cache(uuid: UUID, username: String?, currency: String, value: BigDecimal)

    /**
     * Synchronizes a single player's account data from the cache back to the persistent store (SQL).
     *
     * Marked as [suspend] because it performs non-blocking thread-safe database I/O.
     *
     * @param uuid The unique identifier (UUID) of the player to synchronize.
     * @param shouldUnload If true, removes the player from the cache upon successful save.
     * @return true if synchronization was successful, false otherwise.
     */
    suspend fun sync(uuid: UUID, shouldUnload: Boolean = false): Boolean

    /**
     * Synchronizes all currently cached account data back to the persistent store (SQL).
     *
     * Executed synchronously during server shutdown to guarantee final data persistence.
     */
    fun syncAccounts()

    /**
     * Removes the player's account data and associated locks entirely from the cache.
     *
     * @param uuid The unique identifier (UUID) of the player to remove.
     */
    fun clear(uuid: UUID)

    /**
     * Retrieves the current balance of a player from the cache.
     *
     * @param uuid The unique identifier (UUID) of the player.
     * @param currency The key/name of the currency requested.
     * @return The current [BigDecimal] balance of the player.
     */
    fun getBalance(uuid: UUID, currency: String): BigDecimal

    /**
     * Checks if the player's account data for the specified currency is currently held in the cache.
     *
     * @param uuid The unique identifier (UUID) of the player.
     * @param currency The key/name of the currency, or `null` to check for any cached data.
     * @return `true` if the account is cached, otherwise `false`.
     */
    fun isAccountCached(uuid: UUID, currency: String?): Boolean

    /**
     * Checks if the player is currently online on the server.
     *
     * @param uuid The unique identifier (UUID) of the player.
     * @return `true` if the player is online, otherwise `false`.
     */
    fun isPlayerOnline(uuid: UUID): Boolean
}