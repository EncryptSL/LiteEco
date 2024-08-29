package com.github.encryptsl.lite.eco.api.economy

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.PlayerAccount
import com.github.encryptsl.lite.eco.api.objects.ModernText
import com.github.encryptsl.lite.eco.common.database.entity.User
import com.github.encryptsl.lite.eco.common.extensions.compactFormat
import com.github.encryptsl.lite.eco.common.extensions.moneyFormat
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Bukkit
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.CompletableFuture

class ModernLiteEcoEconomyImpl : DeprecatedLiteEcoEconomyImpl() {

    override fun getUserByUUID(uuid: UUID, currency: String): CompletableFuture<User> {
        val future = CompletableFuture<User>()

        if (PlayerAccount.isPlayerOnline(uuid) || PlayerAccount.isAccountCached(uuid, currency)) {
            future.completeAsync { User(Bukkit.getPlayer(uuid)?.name.toString(), uuid, PlayerAccount.getBalance(uuid, currency)) }
        } else {
            return LiteEco.instance.databaseEcoModel.getUserByUUID(uuid, currency)
        }

        return future
    }

    override fun deleteAccount(uuid: UUID, currency: String): Boolean {
        return getUserByUUID(uuid, currency).thenApply {
            PlayerAccount.clearFromCache(uuid)
            LiteEco.instance.databaseEcoModel.deletePlayerAccount(uuid, currency)
            return@thenApply true
        }.exceptionally {
            return@exceptionally false
        }.join()
    }

    override fun hasAccount(uuid: UUID, currency: String): CompletableFuture<Boolean> {
        return LiteEco.instance.databaseEcoModel.getExistPlayerAccount(uuid, currency)
    }

    override fun has(uuid: UUID, currency: String, amount: BigDecimal): Boolean {
        return amount <= getBalance(uuid, currency)
    }

    override fun depositMoney(uuid: UUID, currency: String, amount: BigDecimal) {
        if (PlayerAccount.isPlayerOnline(uuid)) {
            cacheAccount(uuid, currency, getBalance(uuid, currency).plus(amount))
        } else {
            LiteEco.instance.databaseEcoModel.depositMoney(uuid, currency, amount)
        }
    }

    override fun withDrawMoney(uuid: UUID, currency: String, amount: BigDecimal) {
        if (PlayerAccount.isPlayerOnline(uuid)) {
            cacheAccount(uuid, currency, getBalance(uuid, currency).minus(amount))
        } else {
            LiteEco.instance.databaseEcoModel.withdrawMoney(uuid, currency, amount)
        }
    }

    override fun setMoney(uuid: UUID, currency: String, amount: BigDecimal) {
        if (PlayerAccount.isPlayerOnline(uuid)) {
            cacheAccount(uuid, currency, amount)
        } else {
            LiteEco.instance.databaseEcoModel.setMoney(uuid, currency, amount)
        }
    }

    override fun getBalance(uuid: UUID, currency: String): BigDecimal {
        return getUserByUUID(uuid, currency).join().money
    }

    override fun getCheckBalanceLimit(uuid: UUID, currency: String, amount: BigDecimal): Boolean {
        return ((getBalance(uuid, currency).plus(amount) > LiteEco.instance.currencyImpl.getCurrencyLimit(currency)) && LiteEco.instance.currencyImpl.getCurrencyLimitEnabled(currency))
    }

    override fun transfer(fromUUID: UUID, target: UUID, currency: String, amount: BigDecimal) {
        withDrawMoney(fromUUID, currency, amount)
        depositMoney(target, currency, amount)
    }

    override fun cacheAccount(uuid: UUID, currency: String, amount: BigDecimal) {
        PlayerAccount.cacheAccount(uuid, currency, amount)
    }

    override fun syncAccount(uuid: UUID, currency: String) {
        if (getCheckBalanceLimit(getBalance(uuid, currency), currency) && Bukkit.getOfflinePlayer(uuid).player?.hasPermission("lite.eco.admin.bypass") != true)
            return PlayerAccount.syncAccount(uuid, currency, LiteEco.instance.currencyImpl.getCurrencyLimit(currency))

        PlayerAccount.syncAccount(uuid)
    }

    override fun syncAccounts() {
        PlayerAccount.syncAccounts()
    }

    override fun getTopBalance(currency: String): Map<String, BigDecimal> {
        val database = LiteEco.instance.databaseEcoModel.getTopBalance(currency)
            .mapValues { e -> if (PlayerAccount.isAccountCached(e.value.uuid, currency)) PlayerAccount.getBalance(e.value.uuid, currency) else e.value.money}
            .filterKeys { e -> !e.equals("NULL", true)  }.toList()

        return database.sortedByDescending { (_, e) -> e }.toMap()
    }


    override fun getUUIDNameMap(currency: String): MutableMap<UUID, String> {
        return LiteEco.instance.databaseEcoModel.getUUIDNameMap(currency)
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

    override fun fullFormatting(amount: BigDecimal, currency: String): String {

        val value = when(LiteEco.instance.currencyImpl.isCurrencyDisplayCompactEnabled(currency)) {
            true -> compacted(amount)
            false -> formatted(amount)
        }
        return LiteEco.instance.locale.plainTextTranslation(
            ModernText.miniModernText(
            LiteEco.instance.currencyImpl.getCurrencyFormat(currency), Placeholder.parsed("money", value))
        )
    }

}