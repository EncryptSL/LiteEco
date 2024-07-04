package com.github.encryptsl.lite.eco.api.economy

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.PlayerAccount
import com.github.encryptsl.lite.eco.api.interfaces.LiteEconomyAPI
import com.github.encryptsl.lite.eco.common.database.entity.User
import com.github.encryptsl.lite.eco.common.database.models.DatabaseEcoModel
import com.github.encryptsl.lite.eco.common.extensions.compactFormat
import com.github.encryptsl.lite.eco.common.extensions.moneyFormat
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.CompletableFuture

class LiteEcoEconomyImpl : LiteEconomyAPI {

    private val databaseEcoModel: DatabaseEcoModel by lazy { DatabaseEcoModel() }

    override fun createAccount(player: OfflinePlayer, currency: String, startAmount: BigDecimal): Boolean {
        return getUserByUUID(player, currency).thenApply {
            return@thenApply false
        }.exceptionally {
            databaseEcoModel.createPlayerAccount(player.name.toString(), player.uniqueId, currency, startAmount)
            return@exceptionally true
        }.join()
    }

    override fun cacheAccount(player: OfflinePlayer, currency: String, amount: BigDecimal) {
        getUserByUUID(player, currency).thenAccept { PlayerAccount.cacheAccount(player.uniqueId, currency, amount) }
    }

    override fun deleteAccount(player: OfflinePlayer, currency: String): Boolean {
        return getUserByUUID(player, currency).thenApply {
            PlayerAccount.clearFromCache(player.uniqueId)
            databaseEcoModel.deletePlayerAccount(player.uniqueId, currency)
            return@thenApply true
        }.exceptionally {
            return@exceptionally false
        }.join()
    }

    override fun hasAccount(player: OfflinePlayer, currency: String): CompletableFuture<Boolean> {
        return databaseEcoModel.getExistPlayerAccount(player.uniqueId, currency)
    }

    override fun has(player: OfflinePlayer, currency: String, amount: BigDecimal): Boolean {
        return amount <= getBalance(player, currency)
    }

    override fun getUserByUUID(player: OfflinePlayer, currency: String): CompletableFuture<User> {
        val future = CompletableFuture<User>()

        if (PlayerAccount.isPlayerOnline(player.uniqueId) || PlayerAccount.isAccountCached(player.uniqueId)) {
            future.completeAsync { User(player.name.toString(), player.uniqueId, PlayerAccount.getBalance(player.uniqueId, currency)) }
        } else {
            return databaseEcoModel.getUserByUUID(player.uniqueId, currency)
        }

        return future
    }

    override fun getBalance(player: OfflinePlayer, currency: String): BigDecimal {
        return getUserByUUID(player, currency).join().money
    }

    override fun getCheckBalanceLimit(amount: BigDecimal, currency: String): Boolean {
        return (amount > LiteEco.instance.currencyImpl.getCurrencyLimit(currency)) && LiteEco.instance.currencyImpl.getCurrencyLimitEnabled(currency)
    }

    override fun getCheckBalanceLimit(player: OfflinePlayer, currency: String, amount: BigDecimal): Boolean {
        return ((getBalance(player, currency).plus(amount) > LiteEco.instance.currencyImpl.getCurrencyLimit(currency)) && LiteEco.instance.currencyImpl.getCurrencyLimitEnabled(currency))
    }

    override fun depositMoney(player: OfflinePlayer, currency: String, amount: BigDecimal) {
        if (PlayerAccount.isPlayerOnline(player.uniqueId)) {
            cacheAccount(player, currency, getBalance(player, currency).plus(amount))
        } else {
            databaseEcoModel.depositMoney(player.uniqueId, currency, amount)
        }
    }

    override fun withDrawMoney(player: OfflinePlayer, currency: String, amount: BigDecimal) {
        if (PlayerAccount.isPlayerOnline(player.uniqueId)) {
            cacheAccount(player, currency, getBalance(player, currency).minus(amount))
        } else {
            databaseEcoModel.withdrawMoney(player.uniqueId, currency, amount)
        }
    }

    override fun transfer(fromPlayer: Player, target: OfflinePlayer, currency: String, amount: BigDecimal) {
        withDrawMoney(fromPlayer, currency, amount)
        depositMoney(target, currency, amount)
    }

    override fun setMoney(player: OfflinePlayer, currency: String, amount: BigDecimal) {
        if (PlayerAccount.isPlayerOnline(player.uniqueId)) {
            cacheAccount(player, currency, amount)
        } else {
            databaseEcoModel.setMoney(player.uniqueId, currency, amount)
        }
    }

    override fun syncAccount(offlinePlayer: OfflinePlayer, currency: String) {
        if (getCheckBalanceLimit(getBalance(offlinePlayer, currency), currency) && offlinePlayer.player?.hasPermission("lite.eco.admin.bypass") != true)
            return PlayerAccount.syncAccount(offlinePlayer.uniqueId, currency, LiteEco.instance.currencyImpl.getCurrencyLimit(currency))

        PlayerAccount.syncAccount(offlinePlayer.uniqueId)
    }

    override fun syncAccounts() {
        PlayerAccount.syncAccounts()
    }

    override fun getTopBalance(currency: String): Map<String, BigDecimal> {
        val databaseStoredBalance = databaseEcoModel.getTopBalance(currency).filterKeys { e -> Bukkit.getOfflinePlayer(UUID.fromString(e)).hasPlayedBefore() }
        return databaseStoredBalance
            .mapValues { getBalance(Bukkit.getOfflinePlayer(it.key), currency) }
            .toList()
            .sortedByDescending { (_, e) -> e }.toMap()
    }

    override fun compacted(amount: BigDecimal): String {
        return amount.compactFormat(
            LiteEco.instance.config.getString("formatting.currency_pattern").toString(),
            LiteEco.instance.config.getString("formatting.compacted_pattern").toString(),
            LiteEco.instance.config.getString("formatting.currency_locale").toString()
        )
    }

    override fun formatted(amount: BigDecimal): String {
        return amount.moneyFormat(
            LiteEco.instance.config.getString("formatting.currency_pattern").toString(),
            LiteEco.instance.config.getString("formatting.currency_locale").toString()
        )
    }

    override fun fullFormatting(amount: BigDecimal): String {
        val value = if (LiteEco.instance.config.getBoolean("economy.compact_display")) {
            compacted(amount)
        } else {
            formatted(amount)
        }
        return LiteEco.instance.config.getString("economy.currency_format")
            .toString()
            .replace("{balance}", value)
            .replace("<balance>", value)
            .replace("%balance%", value)
    }
}