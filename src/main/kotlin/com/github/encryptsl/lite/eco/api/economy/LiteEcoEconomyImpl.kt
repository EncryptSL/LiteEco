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
import java.util.*
import java.util.concurrent.CompletableFuture

class LiteEcoEconomyImpl : LiteEconomyAPI {

    private val databaseEcoModel: DatabaseEcoModel by lazy { DatabaseEcoModel() }

    override fun createAccount(player: OfflinePlayer, startAmount: Double): Boolean {
        return getUserByUUID(player).thenApply {
            return@thenApply false
        }.exceptionally {
            databaseEcoModel.createPlayerAccount(player.name.toString(), player.uniqueId, startAmount)
            return@exceptionally true
        }.join()
    }

    override fun cacheAccount(player: OfflinePlayer, amount: Double) {
        getUserByUUID(player).thenAccept { PlayerAccount.cacheAccount(player.uniqueId, amount) }
    }

    override fun deleteAccount(player: OfflinePlayer): Boolean {
        return getUserByUUID(player).thenApply {
            PlayerAccount.clearFromCache(player.uniqueId)
            databaseEcoModel.deletePlayerAccount(player.uniqueId)
            return@thenApply true
        }.exceptionally {
            return@exceptionally false
        }.join()
    }

    override fun hasAccount(player: OfflinePlayer): CompletableFuture<Boolean> {
        return databaseEcoModel.getExistPlayerAccount(player.uniqueId)
    }

    override fun has(player: OfflinePlayer, amount: Double): Boolean {
        return amount <= getBalance(player)
    }

    override fun getUserByUUID(player: OfflinePlayer): CompletableFuture<User> {
        val future = CompletableFuture<User>()

        if (PlayerAccount.isPlayerOnline(player.uniqueId) || PlayerAccount.isAccountCached(player.uniqueId)) {
            future.completeAsync { User(player.name.toString(), player.uniqueId, PlayerAccount.getBalance(player.uniqueId)) }
        } else {
            return databaseEcoModel.getUserByUUID(player.uniqueId)
        }

        return future
    }

    override fun getBalance(player: OfflinePlayer): Double {
        return getUserByUUID(player).join().money
    }

    override fun getCheckBalanceLimit(amount: Double): Boolean {
        return (amount > LiteEco.instance.config.getInt("economy.balance_limit")) && LiteEco.instance.config.getBoolean("economy.balance_limit_check")
    }

    override fun getCheckBalanceLimit(player: OfflinePlayer, amount: Double): Boolean {
        return ((getBalance(player).plus(amount).toInt() > LiteEco.instance.config.getInt("economy.balance_limit")) && LiteEco.instance.config.getBoolean("economy.balance_limit_check"))
    }

    override fun depositMoney(player: OfflinePlayer, amount: Double) {
        if (PlayerAccount.isPlayerOnline(player.uniqueId)) {
            cacheAccount(player, getBalance(player).plus(amount))
        } else {
            databaseEcoModel.depositMoney(player.uniqueId, amount)
        }
    }

    override fun withDrawMoney(player: OfflinePlayer, amount: Double) {
        if (PlayerAccount.isPlayerOnline(player.uniqueId)) {
            cacheAccount(player, getBalance(player).minus(amount))
        } else {
            databaseEcoModel.withdrawMoney(player.uniqueId, amount)
        }
    }

    override fun transfer(fromPlayer: Player, target: OfflinePlayer, amount: Double) {
        withDrawMoney(fromPlayer, amount)
        depositMoney(target, amount)
    }

    override fun setMoney(player: OfflinePlayer, amount: Double) {
        if (PlayerAccount.isPlayerOnline(player.uniqueId)) {
            cacheAccount(player, amount)
        } else {
            databaseEcoModel.setMoney(player.uniqueId, amount)
        }
    }

    override fun syncAccount(offlinePlayer: OfflinePlayer) {
        if (getCheckBalanceLimit(getBalance(offlinePlayer)) && offlinePlayer.player?.hasPermission("lite.eco.admin.bypass") != true)
            return PlayerAccount.syncAccount(offlinePlayer.uniqueId, LiteEco.instance.config.getDouble("economy.balance_limit", 1_000_000.00))

        PlayerAccount.syncAccount(offlinePlayer.uniqueId)
    }

    override fun syncAccounts() {
        PlayerAccount.syncAccounts()
    }

    override fun getTopBalance(): Map<String, Double> {
        val databaseStoredBalance = databaseEcoModel.getTopBalance().filterKeys { e -> Bukkit.getOfflinePlayer(UUID.fromString(e)).hasPlayedBefore() }
        return databaseStoredBalance
            .mapValues { getBalance(Bukkit.getOfflinePlayer(UUID.fromString(it.key))) }
            .toList()
            .sortedByDescending { (_, e) -> e }.toMap()
    }

    override fun compacted(amount: Double): String {
        return amount.compactFormat(
            LiteEco.instance.config.getString("formatting.currency_pattern").toString(),
            LiteEco.instance.config.getString("formatting.compacted_pattern").toString(),
            LiteEco.instance.config.getString("formatting.currency_locale").toString()
        )
    }

    override fun formatted(amount: Double): String {
        return amount.moneyFormat(
            LiteEco.instance.config.getString("formatting.currency_pattern").toString(),
            LiteEco.instance.config.getString("formatting.currency_locale").toString()
        )
    }

    override fun fullFormatting(amount: Double): String {
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