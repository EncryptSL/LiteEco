package com.github.encryptsl.lite.eco.common.hook.treasury.wrapper

import com.github.encryptsl.lite.eco.LiteEco
import me.lokka30.treasury.api.economy.account.Account
import me.lokka30.treasury.api.economy.currency.Currency
import java.math.BigDecimal
import java.util.Locale
import java.util.concurrent.CompletableFuture

class TreasuryCurrency(private val liteEco: LiteEco) : Currency {

    companion object {
        const val CURRENCY_IDENTIFIER = "lite_eco_dollars"
    }

    override fun getIdentifier(): String {
        return CURRENCY_IDENTIFIER
    }

    override fun getSymbol(): String {
        return "$"
    }

    override fun getDecimal(locale: Locale?): Char {
        return ".".toCharArray()[0]
    }

    override fun getLocaleDecimalMap(): Map<Locale?, Char?> {
        return emptyMap()
    }

    override fun getDisplayName(value: BigDecimal, locale: Locale?): String {
        return format(value, locale)
    }

    override fun getPrecision(): Int {
       return 0
    }

    override fun isPrimary(): Boolean {
        return true
    }

    override fun getStartingBalance(account: Account): BigDecimal {
        return liteEco.config.getDouble("economy.starting_balance").toBigDecimal()
    }

    override fun getConversionRate(): BigDecimal {
        return BigDecimal.ONE
    }

    override fun parse(
        formattedAmount: String,
        locale: Locale?
    ): CompletableFuture<BigDecimal?> {
        return CompletableFuture.completedFuture<BigDecimal>(null)
    }

    override fun format(amount: BigDecimal, locale: Locale?): String {
        return liteEco.api.fullFormatting(amount)
    }

    override fun format(amount: BigDecimal, locale: Locale?, precision: Int): String {
        return format(amount, locale)
    }
}