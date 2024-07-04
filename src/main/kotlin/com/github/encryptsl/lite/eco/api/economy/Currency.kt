package com.github.encryptsl.lite.eco.api.economy

import com.github.encryptsl.lite.eco.LiteEco
import java.math.BigDecimal

class Currency(private val liteEco: LiteEco) {

    fun defaultCurrency(): String
        = liteEco.config.getString("economy.currencies.default.currency_name", "dollar").toString()

    fun defaultCurrencyPluralName(): String
            = liteEco.config.getString("economy.currencies.default.currency_plural_name", "dollars").toString()

    fun defaultCurrencySingularName(): String
            = liteEco.config.getString("economy.currencies.default.currency_singular_name", "dollar").toString()

    fun defaultStartBalance(): BigDecimal
        = liteEco.config.getDouble("economy.currencies.default.starting_balance", 30.00).toBigDecimal()

    fun getCurrencyStartBalance(currency: String): BigDecimal {
        val getKey = getCurrenciesKeys()?.firstOrNull { el -> el.equals(currency, true) } ?: return BigDecimal.ZERO

        return liteEco.config.getDouble("economy.currencies.$getKey.starting_balance").toBigDecimal()
    }

    fun getCurrencyLimit(currency: String): BigDecimal {
        val getKey = getCurrenciesKeys()?.firstOrNull { el -> el.equals(currency, true) } ?: return BigDecimal.ZERO

        return liteEco.config.getDouble("economy.currencies.$getKey.balance_limit").toBigDecimal()
    }

    fun getCurrencyLimitEnabled(currency: String): Boolean {
        val getKey = getCurrenciesKeys()?.firstOrNull { el -> el.equals(currency, true) } ?: return false

        return liteEco.config.getBoolean("economy.currencies.$getKey.balance_limit_check")
    }

    fun getCurrenciesNames(): List<String>?
        = getCurrenciesKeys()?.map {
            liteEco.config.getString("economy.currencies.$it.currency_name").toString()
        }

    private fun getCurrenciesKeys(): MutableSet<String>? {
        return liteEco.config.getConfigurationSection("economy.currencies")?.getKeys(false)
    }

}