package encryptsl.cekuj.net.api.interfaces

import org.bukkit.OfflinePlayer

interface LiteEconomyAPIProvider {
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
     * @see Boolean
     * @see OfflinePlayer
     * @see PreparedStatements.getBalance(uuid: UUID)
     */
    fun cacheAccount(player: OfflinePlayer, amount: Double): Boolean

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
    fun hasAccount(player: OfflinePlayer): Boolean

    /**
     * Boolean for check if player have enough money
     * @param player is OfflinePlayer
     * @return Boolean
     * @see OfflinePlayer
     */
    fun has(player: OfflinePlayer, amount: Double): Boolean

    /**
     * Get balance of player account
     * @param player is OfflinePlayer
     * @return Double
     * @see OfflinePlayer
     */
    fun getBalance(player: OfflinePlayer): Double

    /**
     * Deposit money to player account
     * @param player is OfflinePlayer
     * @param amount is amount added to player account
     * @see OfflinePlayer
     */
    fun depositMoney(player: OfflinePlayer, amount: Double)

    /**
     * WithDraw money from player account
     * @param player is OfflinePlayer
     * @param amount is amount removed from player account
     * @see OfflinePlayer
     */
    fun withDrawMoney(player: OfflinePlayer, amount: Double)

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
    fun getTopBalance(): MutableMap<String, Double>

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