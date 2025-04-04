package com.github.encryptsl.lite.eco.api.economy

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.PlayerAccount
import com.github.encryptsl.lite.eco.api.interfaces.LiteEconomyAPI
import com.github.encryptsl.lite.eco.common.database.entity.User
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.math.BigDecimal
import java.util.Optional
import java.util.concurrent.CompletableFuture

abstract class DeprecatedLiteEcoEconomyImpl : LiteEconomyAPI {

    override fun cacheAccount(player: OfflinePlayer, currency: String, amount: BigDecimal) {
        PlayerAccount.cacheAccount(player.uniqueId, currency, amount)
    }

    override fun deleteAccount(player: OfflinePlayer, currency: String): Boolean {
        return getUserByUUID(player, currency).thenApply {
            PlayerAccount.clearFromCache(player.uniqueId)
            LiteEco.instance.databaseEcoModel.deletePlayerAccount(player.uniqueId, currency)
            return@thenApply true
        }.exceptionally {
            return@exceptionally false
        }.join()
    }

    override fun hasAccount(player: OfflinePlayer, currency: String): CompletableFuture<Boolean> {
        return LiteEco.instance.databaseEcoModel.getExistPlayerAccount(player.uniqueId, currency)
    }

    override fun has(player: OfflinePlayer, currency: String, amount: BigDecimal): Boolean {
        return amount <= getBalance(player, currency)
    }

    override fun getUserByUUID(player: OfflinePlayer, currency: String): CompletableFuture<Optional<User>> {
        val future = CompletableFuture<Optional<User>>()

        if (PlayerAccount.isPlayerOnline(player.uniqueId) || PlayerAccount.isAccountCached(player.uniqueId, currency)) {
            future.completeAsync { Optional.of(User(player.name.toString(), player.uniqueId, PlayerAccount.getBalance(player.uniqueId, currency))) }
        } else {
            return LiteEco.instance.databaseEcoModel.getUserByUUID(player.uniqueId, currency)
        }

        return future
    }

    override fun getBalance(player: OfflinePlayer, currency: String): BigDecimal {
        return getUserByUUID(player, currency).join().get().money
    }

    override fun getCheckBalanceLimit(amount: BigDecimal, currency: String): Boolean {
        return (amount > LiteEco.instance.currencyImpl.getCurrencyLimit(currency)) && LiteEco.instance.currencyImpl.getCurrencyLimitEnabled(currency)
    }

    override fun getCheckBalanceLimit(player: OfflinePlayer, currentBalance: BigDecimal, currency: String, amount: BigDecimal): Boolean {
        return ((getBalance(player, currency).plus(amount) > LiteEco.instance.currencyImpl.getCurrencyLimit(currency)) && LiteEco.instance.currencyImpl.getCurrencyLimitEnabled(currency))
    }

    override fun depositMoney(player: OfflinePlayer, currency: String, amount: BigDecimal) {
        if (PlayerAccount.isPlayerOnline(player.uniqueId)) {
            cacheAccount(player, currency, getBalance(player, currency).plus(amount))
        } else {
            LiteEco.instance.databaseEcoModel.depositMoney(player.uniqueId, currency, amount)
        }
    }

    override fun withDrawMoney(player: OfflinePlayer, currency: String, amount: BigDecimal) {
        if (PlayerAccount.isPlayerOnline(player.uniqueId)) {
            cacheAccount(player, currency, getBalance(player, currency).minus(amount))
        } else {
            LiteEco.instance.databaseEcoModel.withdrawMoney(player.uniqueId, currency, amount)
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
            LiteEco.instance.databaseEcoModel.setMoney(player.uniqueId, currency, amount)
        }
    }

    override fun syncAccount(offlinePlayer: OfflinePlayer, currency: String) {
        if (getCheckBalanceLimit(getBalance(offlinePlayer, currency), currency) && offlinePlayer.player?.hasPermission("lite.eco.admin.bypass") != true)
            return PlayerAccount.syncAccount(offlinePlayer.uniqueId, currency, LiteEco.instance.currencyImpl.getCurrencyLimit(currency))

        PlayerAccount.syncAccount(offlinePlayer.uniqueId)
    }
}