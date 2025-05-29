package com.github.encryptsl.lite.eco.api.economy

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.PlayerAccount
import com.github.encryptsl.lite.eco.api.interfaces.LiteEconomyAPI
import com.github.encryptsl.lite.eco.api.objects.ModernText
import com.github.encryptsl.lite.eco.common.database.entity.User
import com.github.encryptsl.lite.eco.common.extensions.compactFormat
import com.github.encryptsl.lite.eco.common.extensions.moneyFormat
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import java.math.BigDecimal
import java.util.*

class ModernLiteEcoEconomyImpl : LiteEconomyAPI {

    override fun createAccount(player: OfflinePlayer, currency: String, startAmount: BigDecimal): Boolean {
        val user = getUserByUUID(player.uniqueId, currency).orElse(null)
        return if (user == null) {
            LiteEco.instance.databaseEcoModel.createPlayerAccount(player.name.toString(), player.uniqueId, currency, startAmount)
            true
        } else {
            LiteEco.instance.databaseEcoModel.updatePlayerName(player.uniqueId, player.name.toString(), currency)
            false
        }
    }

    override fun createAccount(uuid: UUID, username: String, currency: String, startAmount: BigDecimal): Boolean {
        val user = getUserByUUID(uuid, currency).orElse(null)
        return if (user == null) {
            LiteEco.instance.databaseEcoModel.createPlayerAccount(username, uuid, currency, startAmount)
            true
        } else {
            LiteEco.instance.databaseEcoModel.updatePlayerName(uuid, username, currency)
            false
        }
    }

    override fun getUserByUUID(uuid: UUID, currency: String): Optional<User> {
        return if (PlayerAccount.isPlayerOnline(uuid) || PlayerAccount.isAccountCached(uuid, currency)) {
            Optional.of(User(Bukkit.getPlayer(uuid)?.name.toString(), uuid, PlayerAccount.getBalance(uuid, currency)))
        } else {
            Optional.ofNullable(LiteEco.instance.databaseEcoModel.getUserByUUID(uuid, currency))
        }
    }

    override fun deleteAccount(uuid: UUID, currency: String): Boolean {
        return try {
            getUserByUUID(uuid, currency).orElse(null)?.let {
                PlayerAccount.clearFromCache(uuid)
                LiteEco.instance.databaseEcoModel.deletePlayerAccount(uuid, currency)
                true
            } ?: false
        } catch (e: Exception) {
            LiteEco.instance.logger.severe("Chyba při mazání účtu: ${e.message}")
            false
        }
    }

    override fun hasAccount(uuid: UUID, currency: String): Boolean {
        return LiteEco.instance.databaseEcoModel.getExistPlayerAccount(uuid, currency)
    }

    override fun has(uuid: UUID, currency: String, amount: BigDecimal): Boolean {
        return amount <= getBalance(uuid, currency)
    }

    override fun depositMoney(uuid: UUID, currency: String, amount: BigDecimal) {
        if (PlayerAccount.isPlayerOnline(uuid) && PlayerAccount.isAccountCached(uuid, currency)) {
            cacheAccount(uuid, currency, getBalance(uuid, currency).plus(amount))
        } else {
            LiteEco.instance.databaseEcoModel.depositMoney(uuid, currency, amount)
        }
    }

    override fun withDrawMoney(uuid: UUID, currency: String, amount: BigDecimal) {
        if (PlayerAccount.isPlayerOnline(uuid) && PlayerAccount.isAccountCached(uuid, currency)) {
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
        return getUserByUUID(uuid, currency).map { it.money }.orElse(BigDecimal.ZERO)
    }

    override fun getCheckBalanceLimit(amount: BigDecimal, currency: String): Boolean {
        return (amount > LiteEco.instance.currencyImpl.getCurrencyLimit(currency)) && LiteEco.instance.currencyImpl.getCurrencyLimitEnabled(currency)
    }

    override fun getCheckBalanceLimit(uuid: UUID, currentBalance: BigDecimal, currency: String, amount: BigDecimal): Boolean {
        return ((currentBalance.plus(amount) > LiteEco.instance.currencyImpl.getCurrencyLimit(currency)) && LiteEco.instance.currencyImpl.getCurrencyLimitEnabled(currency))
    }

    override fun transfer(fromUUID: UUID, target: UUID, currency: String, amount: BigDecimal) {
        withDrawMoney(fromUUID, currency, amount)
        depositMoney(target, currency, amount)
    }

    override fun cacheAccount(uuid: UUID, currency: String, amount: BigDecimal) {
        PlayerAccount.cacheAccount(uuid, currency, amount)
    }

    override fun syncAccount(uuid: UUID, currency: String) {
        if (getCheckBalanceLimit(getBalance(uuid, currency), currency))
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