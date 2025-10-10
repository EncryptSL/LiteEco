package com.github.encryptsl.lite.eco.api.interfaces

import java.math.BigDecimal
import java.util.*

/**
 * Interface defining the primary API for managing and accessing player accounts,
 * focusing heavily on caching and synchronization operations.
 *
 * This API is typically used to provide fast access to player data (like balances)
 * without constantly hitting the main database (SQL).
 */
interface AccountAPI {

    /**
     * Caches the player's account balance in memory or a fast storage layer.
     *
     * This method is usually called after data is loaded from the persistent store (SQL).
     *
     * @param uuid The unique identifier (UUID) of the player.
     * @param currency The key/name of the currency being cached.
     * @param value The [BigDecimal] balance value to store in the cache.
     */
    fun cacheAccount(uuid: UUID, currency: String, value: BigDecimal)

    /**
     * Synchronizes a single player's account data from the cache back to the persistent store (SQL).
     *
     * This operation typically saves the current cached balance to the database.
     *
     * @param uuid The unique identifier (UUID) of the player to synchronize.
     */
    fun syncAccount(uuid: UUID)

    /**
     * Synchronizes all currently cached account data back to the persistent store (SQL).
     *
     * This is generally used as a mass save operation or a cleanup on shutdown.
     */
    fun syncAccounts()

    /**
     * Removes the player's account data entirely from the cache.
     *
     * This is typically performed when a player logs out or when an account is deleted.
     *
     * @param uuid The unique identifier (UUID) of the player to remove from the cache.
     */
    fun clearFromCache(uuid: UUID)

    /**
     * Retrieves the current balance of a player, preferably from the cache for speed.
     *
     * If the account is not found in the cache, the implementation may fall back to the SQL database.
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
     * @param currency The key/name of the currency, or `null` to check for any cached data for the player.
     * @return `true` if the account is cached (for the given currency or any currency), otherwise `false`.
     */
    fun isAccountCached(uuid: UUID, currency: String?): Boolean

    /**
     * Checks if the player is currently online/active in the system.
     *
     * This is often used to determine if an account needs immediate caching or synchronization.
     *
     * @param uuid The unique identifier (UUID) of the player.
     * @return `true` if the player is online, otherwise `false`.
     */
    fun isPlayerOnline(uuid: UUID): Boolean
}