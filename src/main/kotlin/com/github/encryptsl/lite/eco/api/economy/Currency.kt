package com.github.encryptsl.lite.eco.api.economy

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.objects.ModernText
import com.github.encryptsl.lite.eco.common.extensions.compactFormat
import com.github.encryptsl.lite.eco.common.extensions.moneyFormat
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import java.math.BigDecimal

class Currency(private val liteEco: LiteEco) {

    fun defaultCurrency(): String = getCurrenciesKeys().first()

    fun defaultStartBalance(): BigDecimal
        = liteEco.config.getDouble("economy.currencies.${defaultCurrency()}.starting_balance", 30.00).toBigDecimal()

    fun getCurrencyPlural(currency: String): String {
        return liteEco.config.getString("economy.currencies.$currency.currency_plural_name").toString()
    }

    fun getCurrencySingular(currency: String): String {
        return liteEco.config.getString("economy.currencies.$currency.currency_singular_name").toString()
    }

    fun getCurrencyFormat(currency: String): String {
        return liteEco.config.getString("economy.currencies.$currency.currency_format").toString()
    }

    fun getCurrencyStartBalance(currency: String): BigDecimal {
        return liteEco.config.getDouble("economy.currencies.$currency.starting_balance", 30.00).toBigDecimal()
    }

    fun getCurrencyLimit(currency: String): BigDecimal {
        return liteEco.config.getDouble("economy.currencies.$currency.balance_limit", 0.00).toBigDecimal()
    }

    fun getCheckBalanceLimit(amount: BigDecimal, currency: String = "dollars"): Boolean {
        return (amount > getCurrencyLimit(currency)) && getCurrencyLimitEnabled(currency)
    }

    fun getCheckBalanceLimit(currentBalance: BigDecimal, currency: String = "dollars", amount: BigDecimal): Boolean {
        return ((currentBalance.plus(amount) > getCurrencyLimit(currency)) && getCurrencyLimitEnabled(currency))
    }

    fun getCurrencyLimitEnabled(currency: String): Boolean {
        return liteEco.config.getBoolean("economy.currencies.$currency.balance_limit_check")
    }

    fun isCurrencyDisplayCompactEnabled(currency: String): Boolean
        = liteEco.config.getBoolean("economy.currencies.$currency.compact_display")

    fun getCurrencyNameExist(currency: String): Boolean {
        return getCurrenciesKeys().contains(currency)
    }

    private fun getCurrenciesNames(): List<String> = getCurrenciesKeys().map {
        liteEco.config.getString("economy.currencies.$it.currency_name").toString()
    }

    fun getKeyOfCurrency(currency: String): String {
        return getCurrenciesKeys().singleOrNull { it.contains(currency) } ?: defaultCurrency()
    }

    fun getCurrenciesKeys(): MutableSet<String> {
        return liteEco.config.getConfigurationSection("economy.currencies")?.getKeys(false) ?: mutableSetOf()
    }

    fun currencyModularNameConvert(currency: String, value: BigDecimal): String {
        return if (value.compareTo(BigDecimal.ONE) == 0) {
            getCurrencySingular(currency)
        } else {
            getCurrencyPlural(currency)
        }
    }

    fun compacted(amount: BigDecimal): String = amount.compactFormat(
        liteEco.config.getString("formatting.currency_pattern").toString(),
        liteEco.config.getString("formatting.compacted_pattern").toString(),
        liteEco.config.getString("formatting.currency_locale").toString()
    )

    fun formatted(amount: BigDecimal): String = amount.moneyFormat(
        liteEco.config.getString("formatting.currency_pattern").toString(),
        liteEco.config.getString("formatting.currency_locale").toString()
    )

    fun fullFormatting(amount: BigDecimal, currency: String = "dollars"): String {
        val value = when(isCurrencyDisplayCompactEnabled(currency)) {
            true -> compacted(amount)
            false -> formatted(amount)
        }
        return liteEco.locale.plainTextTranslation(
            ModernText.miniModernText(getCurrencyFormat(currency), Placeholder.parsed("money", value))
        )
    }
}