package com.github.encryptsl.lite.eco.api.economy

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.objects.ModernText
import com.github.encryptsl.lite.eco.common.extensions.compactFormat
import com.github.encryptsl.lite.eco.common.extensions.moneyFormat
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import java.math.BigDecimal

class Currency(private val liteEco: LiteEco) {

    private val config get() = liteEco.baseConfig

    private fun getCurrencyData(currency: String) = config.economy.currencies[currency]

    fun defaultCurrency(): String = getCurrenciesKeys().first()

    fun defaultStartBalance(): BigDecimal =
        getCurrencyData(defaultCurrency())?.startingBalance?.toBigDecimal() ?: BigDecimal.valueOf(30.0)

    fun getCurrencyPlural(currency: String): String {
        return getCurrencyData(currency)?.currencyPluralName ?: "dollars"
    }

    fun getCurrencySingular(currency: String): String {
        return getCurrencyData(currency)?.currencySingularName ?: "dollar"
    }

    private fun getCurrencyFormat(currency: String): String {
        return getCurrencyData(currency)?.currencyFormat ?: "$ <money>"
    }

    fun getCurrencyStartBalance(currency: String): BigDecimal {
        return getCurrencyData(currency)?.startingBalance?.toBigDecimal() ?: BigDecimal.valueOf(30.0)
    }

    fun getCurrencyLimit(currency: String): BigDecimal {
        return getCurrencyData(currency)?.balanceLimit?.toBigDecimal() ?: BigDecimal.ZERO
    }

    fun getCheckBalanceLimit(amount: BigDecimal, currency: String = "dollars"): Boolean {
        return (amount > getCurrencyLimit(currency)) && getCurrencyLimitEnabled(currency)
    }

    fun getCheckBalanceLimit(currentBalance: BigDecimal, currency: String = "dollars", amount: BigDecimal): Boolean {
        return ((currentBalance.plus(amount) > getCurrencyLimit(currency)) && getCurrencyLimitEnabled(currency))
    }

    fun getCurrencyLimitEnabled(currency: String): Boolean {
        return getCurrencyData(currency)?.balanceLimitCheck ?: false
    }

    fun isCurrencyDisplayCompactEnabled(currency: String): Boolean {
        return getCurrencyData(currency)?.compactDisplay ?: false
    }

    fun getCurrencyNameExist(currency: String): Boolean {
        return config.economy.currencies.containsKey(currency)
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

    fun fullFormatting(amount: BigDecimal, currency: String = "dollars"): String {
        val value = when(isCurrencyDisplayCompactEnabled(currency)) {
            true -> compacted(amount)
            false -> formatted(amount)
        }

        val format = getCurrencyFormat(currency)

        return liteEco.locale.plainTextTranslation(
            ModernText.miniModernText(format, Placeholder.parsed("money", value))
        )
    }
}