package com.github.encryptsl.lite.eco.api.economy

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.economy.account.AccountCache
import com.github.encryptsl.lite.eco.api.economy.account.AccountHolder
import com.github.encryptsl.lite.eco.api.interfaces.LiteEconomyAPI
import java.math.BigDecimal
import java.util.*

abstract class ModernLiteEcoEconomyImpl : LiteEconomyAPI {

    private val account: AccountHolder by lazy { AccountHolder() }

    override fun batchInsert(importData: List<Triple<UUID, String, BigDecimal>>, currency: String) {
        LiteEco.instance.databaseEcoModel.batchInsert(importData, currency)
    }

    override fun getTopBalance(currency: String): Map<String, BigDecimal> {
        val currencyData = LiteEco.instance.baseConfig.economy.currencies[currency]
        val topSettings = currencyData?.topBalances

        val isFilteringEnabled = topSettings?.filtering ?: false

        val blackList = if (isFilteringEnabled) {
            topSettings.blacklist
        } else {
            emptyList()
        }

        val combinedList = blackList.toSet().plus(setOf("NULL", "CONSOLE", "SERVER"))

        return LiteEco.instance.databaseEcoModel.getTopBalance(currency)
            .mapValues { e ->
                if (AccountCache.isAccountCached(e.value.uuid, currency))
                    AccountCache.getBalance(e.value.uuid, currency)
                else
                    e.value.money
            }
            .filterKeys { name ->
                combinedList.none { it.equals(name, ignoreCase = true) }
            }
            .toList()
            .sortedByDescending { (_, balance) -> balance }
            .toMap()
    }

    override fun getUUIDNameMap(currency: String): MutableMap<UUID, String> {
        return LiteEco.instance.databaseEcoModel.getUUIDNameMap(currency)
    }

    override fun account(): AccountHolder = account
}