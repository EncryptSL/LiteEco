package com.github.encryptsl.lite.eco.api.economy

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.account.PlayerAccount
import com.github.encryptsl.lite.eco.api.interfaces.LiteEconomyAPI
import kotlinx.coroutines.runBlocking
import java.math.BigDecimal
import java.util.*

abstract class ModernLiteEcoEconomyImpl : LiteEconomyAPI {

    override fun hasAccount(uuid: UUID, currency: String): Boolean =
        LiteEco.instance.databaseEcoModel.getExistPlayerAccount(uuid, currency)

    override fun has(uuid: UUID, currency: String, requiredAmount: BigDecimal): Boolean
            = runBlocking { requiredAmount <= getBalance(uuid, currency) }

    override fun syncAccounts() {
        PlayerAccount.syncAccounts()
    }

    override fun batchInsert(importData: List<Triple<UUID, String, BigDecimal>>, currency: String) {
        LiteEco.instance.databaseEcoModel.batchInsert(importData, currency)
    }

    override fun getTopBalance(currency: String): Map<String, BigDecimal> {
        val isFilteringEnabled = LiteEco.instance.config.getBoolean("economy.currencies.$currency.top_balances.filtering")

        val blackList = if (isFilteringEnabled) {
            LiteEco.instance.config.getStringList("economy.currencies.$currency.top_balances.blacklist")
        } else {
            emptyList()
        }
        val combinedList = blackList.toSet().plus(setOf("NULL", "CONSOLE", "SERVER"))

        val database = LiteEco.instance.databaseEcoModel.getTopBalance(currency)
            .mapValues { e -> if (PlayerAccount.isAccountCached(e.value.uuid, currency)) PlayerAccount.getBalance(e.value.uuid, currency) else e.value.money}
            .filterKeys { e -> !combinedList.contains(e) }.toList()

        return database.sortedByDescending { (_, e) -> e }.toMap()
    }

    override fun getUUIDNameMap(currency: String): MutableMap<UUID, String> {
        return LiteEco.instance.databaseEcoModel.getUUIDNameMap(currency)
    }
}