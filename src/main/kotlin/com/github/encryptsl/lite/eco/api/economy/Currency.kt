package com.github.encryptsl.lite.eco.api.economy

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.objects.ModernText
import com.github.encryptsl.lite.eco.common.extensions.compactFormat
import com.github.encryptsl.lite.eco.common.extensions.moneyFormat
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import java.math.BigDecimal

class Currency(private val liteEco: LiteEco) {

    companion object {
        private val DEFAULT_STARTING_BALANCE = BigDecimal.valueOf(30.0)
    }

    private val config get() = liteEco.baseConfig

    private fun getCurrencyData(currency: String) = config.economy.currencies[currency.lowercase()]

    fun defaultCurrency(): String {
        return getCurrenciesKeys().firstOrNull() ?: throw Exception("You must provide a default currency")
    }

    fun defaultStartBalance(): BigDecimal {
        return getCurrencyStartBalance(defaultCurrency())
    }

    fun getCurrencyPlural(currency: String): String {
        return getCurrencyData(currency)?.currencyPluralName ?: ""
    }

    fun getCurrencySingular(currency: String): String {
        return getCurrencyData(currency)?.currencySingularName ?: ""
    }

    private fun getCurrencyFormat(currency: String): String {
        return getCurrencyData(currency)?.currencyFormat ?: "$ <money>"
    }

    fun getCurrencyStartBalance(currency: String): BigDecimal {
        return getCurrencyData(currency)?.startingBalance?.toBigDecimal() ?: DEFAULT_STARTING_BALANCE
    }

    fun getCurrencyLimit(currency: String): BigDecimal {
        return getCurrencyData(currency)?.balanceLimit?.toBigDecimal() ?: BigDecimal.ZERO
    }

    fun getCheckBalanceLimit(amount: BigDecimal, currency: String): Boolean {
        return getCurrencyLimitEnabled(currency) && amount > getCurrencyLimit(currency)
    }

    fun getCheckBalanceLimit(currentBalance: BigDecimal, currency: String, amount: BigDecimal): Boolean {
        return getCurrencyLimitEnabled(currency) && (currentBalance.plus(amount) > getCurrencyLimit(currency))
    }

    fun getCurrencyLimitEnabled(currency: String): Boolean {
        return getCurrencyData(currency)?.balanceLimitCheck ?: false
    }

    fun isCurrencyDisplayCompactEnabled(currency: String): Boolean {
        return getCurrencyData(currency)?.compactDisplay ?: false
    }

    fun getCurrencyNameExist(currency: String): Boolean {
        return config.economy.currencies.containsKey(currency.lowercase())
    }

    fun getCurrenciesKeys(): Set<String> {
        return config.economy.currencies.keys
    }

    fun currencyModularNameConvert(currency: String, value: BigDecimal): String {
        return if (value.compareTo(BigDecimal.ONE) == 0) {
            getCurrencySingular(currency)
        } else {
            getCurrencyPlural(currency)
        }
    }

    fun compacted(amount: BigDecimal): String = amount.compactFormat(
        config.formatting.currencyPattern,
        config.formatting.compactedPattern,
        config.formatting.currencyLocale
    )

    fun formatted(amount: BigDecimal): String = amount.moneyFormat(
        config.formatting.currencyPattern,
        config.formatting.currencyLocale
    )

    fun fullFormatting(amount: BigDecimal, currency: String): String {
        val value = if (isCurrencyDisplayCompactEnabled(currency)) {
            compacted(amount)
        } else {
            formatted(amount)
        }

        val format = getCurrencyFormat(currency)

        return liteEco.locale.plainTextTranslation(
            ModernText.miniModernText(format, Placeholder.parsed("money", value))
        )
    }
}