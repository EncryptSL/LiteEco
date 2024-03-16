package com.github.encryptsl.lite.eco.api.economy

import com.github.encryptsl.lite.eco.api.PlayerAccount
import com.github.encryptsl.lite.eco.api.interfaces.LiteEconomyAPIProvider
import com.github.encryptsl.lite.eco.common.database.models.PreparedStatements
import com.github.encryptsl.lite.eco.common.extensions.compactFormat
import com.github.encryptsl.lite.eco.common.extensions.moneyFormat
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.plugin.Plugin
import java.util.*

class LiteEcoEconomyAPI(val plugin: Plugin) : LiteEconomyAPIProvider {

    private val preparedStatements: PreparedStatements by lazy { PreparedStatements() }
    private val playerAccount: PlayerAccount by lazy { PlayerAccount(plugin) }

    override fun createAccount(player: OfflinePlayer, startAmount: Double): Boolean {
        if (hasAccount(player)) return false

        preparedStatements.createPlayerAccount(player.name.toString(), player.uniqueId, startAmount)
        return true
    }

    override fun cacheAccount(player: OfflinePlayer, amount: Double): Boolean {
        if (!hasAccount(player)) return false

        playerAccount.cacheAccount(player.uniqueId, amount)
        return true
    }

    override fun deleteAccount(player: OfflinePlayer): Boolean {
        if (!hasAccount(player)) return false

        playerAccount.removeAccount(player.uniqueId)
        preparedStatements.deletePlayerAccount(player.uniqueId)

        return true
    }

    override fun hasAccount(player: OfflinePlayer): Boolean {
        return preparedStatements.getExistPlayerAccount(player.uniqueId)
    }

    override fun has(player: OfflinePlayer, amount: Double): Boolean {
        return amount <= getBalance(player)
    }

    override fun getBalance(player: OfflinePlayer): Double {
        return if (playerAccount.isPlayerOnline(player.uniqueId) || playerAccount.isAccountCached(player.uniqueId))
            playerAccount.getBalance(player.uniqueId)
        else
            preparedStatements.getBalance(player.uniqueId)
    }

    override fun getCheckBalanceLimit(amount: Double): Boolean {
        return (amount > plugin.config.getInt("economy.balance_limit")) && plugin.config.getBoolean("economy.balance_limit_check")
    }

    override fun getCheckBalanceLimit(player: OfflinePlayer, amount: Double): Boolean {
        return ((getBalance(player).plus(amount).toInt() > plugin.config.getInt("economy.balance_limit")) && plugin.config.getBoolean("economy.balance_limit_check"))
    }

    override fun depositMoney(player: OfflinePlayer, amount: Double) {
        if (playerAccount.isPlayerOnline(player.uniqueId)) {
            cacheAccount(player, getBalance(player).plus(amount))
        } else {
            preparedStatements.depositMoney(player.uniqueId, amount)
        }
    }

    override fun withDrawMoney(player: OfflinePlayer, amount: Double) {
        if (playerAccount.isPlayerOnline(player.uniqueId)) {
            cacheAccount(player, getBalance(player).minus(amount))
        } else {
            preparedStatements.withdrawMoney(player.uniqueId, amount)
        }
    }

    override fun setMoney(player: OfflinePlayer, amount: Double) {
        if (playerAccount.isPlayerOnline(player.uniqueId)) {
            cacheAccount(player, amount)
        } else {
            preparedStatements.setMoney(player.uniqueId, amount)
        }
    }

    override fun syncAccount(offlinePlayer: OfflinePlayer) {
        if (getCheckBalanceLimit(getBalance(offlinePlayer)) && offlinePlayer.player?.hasPermission("lite.eco.admin.bypass") != true)
            return playerAccount.syncAccount(offlinePlayer.uniqueId, 1_000_000.00)

        playerAccount.syncAccount(offlinePlayer.uniqueId)
    }

    override fun syncAccounts() {
        playerAccount.syncAccounts()
    }

    override fun getTopBalance(): MutableMap<String, Double> {
        val databaseStoredBalance = preparedStatements.getTopBalance().filterNot { e -> Bukkit.getOfflinePlayer(e.key).name == null }
        return databaseStoredBalance.mapValues { getBalance(Bukkit.getOfflinePlayer(UUID.fromString(it.key))) }.toMutableMap()
    }

    override fun compacted(amount: Double): String {
        return amount.compactFormat(plugin.config.getString("formatting.currency_pattern").toString(), plugin.config.getString("formatting.compacted_pattern").toString(), plugin.config.getString("formatting.currency_locale").toString())
    }

    override fun formatted(amount: Double): String {
        return amount.moneyFormat(plugin.config.getString("formatting.currency_pattern").toString(), plugin.config.getString("formatting.currency_locale").toString())
    }

    override fun fullFormatting(amount: Double): String {
        val value = if (plugin.config.getBoolean("economy.compact_display")) {
            compacted(amount)
        }
        else {
            formatted(amount)
        }
        return plugin.config.getString("economy.currency_format")
            .toString()
            .replace("{balance}", value)
            .replace("<balance>", value)
            .replace("%balance%", value)
    }
}