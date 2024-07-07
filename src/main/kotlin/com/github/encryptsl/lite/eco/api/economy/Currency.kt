package com.github.encryptsl.lite.eco.api.economy

import com.github.encryptsl.lite.eco.LiteEco
import java.math.BigDecimal
import java.util.*

class Currency(private val liteEco: LiteEco) {

    fun defaultCurrency(): String = getKeyOfCurrency(getCurrenciesKeys().first())

    fun defaultStartBalance(): BigDecimal
        = liteEco.config.getDouble("economy.currencies.${defaultCurrency()}.starting_balance", 30.00).toBigDecimal()

    private fun defaultCurrencySymbol(): String
        = liteEco.config.getString("economy.currencies.${defaultCurrency()}.currency_symbol").toString()


    fun getCurrencyName(currency: String): String {
        return Optional.ofNullable(getCurrenciesNames().find { el -> el.contains(currency) }).orElse("dollars")
    }

    fun getCurrencyPlural(currency: String): String {
        return liteEco.config.getString("economy.currencies.$currency.currency_plural_name").toString()
    }

    fun getCurrencySingular(currency: String): String {
        return liteEco.config.getString("economy.currencies.$currency.currency_singular_name").toString()
    }

    fun getCurrencySymbol(currency: String): String {
        return liteEco.config.getString("economy.currencies.$currency.currency_symbol").toString()
    }

    fun getCurrencyStartBalance(currency: String): BigDecimal {
        return liteEco.config.getDouble("economy.currencies.$currency.starting_balance", 30.00).toBigDecimal()
    }

    fun getCurrencyLimit(currency: String): BigDecimal {
        return liteEco.config.getDouble("economy.currencies.$currency.balance_limit", 0.00).toBigDecimal()
    }

    fun getCurrencyLimitEnabled(currency: String): Boolean {
        return liteEco.config.getBoolean("economy.currencies.$currency.balance_limit_check")
    }

    fun getCurrencyNameExist(currency: String): Boolean {
        return getCurrenciesKeys().contains(currency)
    }

    private fun getCurrenciesNames(): List<String> = getCurrenciesKeys().map {
        liteEco.config.getString("economy.currencies.$it.currency_name").toString()
    }

    fun getKeyOfCurrency(currency: String): String {
        return getCurrenciesKeys().firstOrNull { el -> el.contains(currency) } ?: "dollars"
    }

    fun getCurrenciesKeys(): MutableSet<String> {
        return liteEco.config.getConfigurationSection("economy.currencies")?.getKeys(false) ?: mutableSetOf()
    }

    fun currencyModularNameConvert(currency: String, value: BigDecimal): String {
       return if (value == BigDecimal.valueOf(1)) getCurrencySingular(currency) else getCurrencyPlural(currency)
    }

}