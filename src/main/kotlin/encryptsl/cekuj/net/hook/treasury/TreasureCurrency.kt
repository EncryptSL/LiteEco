package encryptsl.cekuj.net.hook.treasury

import encryptsl.cekuj.net.LiteEco
import me.lokka30.treasury.api.economy.currency.Currency
import me.lokka30.treasury.api.economy.response.EconomyException
import me.lokka30.treasury.api.economy.response.EconomyFailureReason
import me.lokka30.treasury.api.economy.response.EconomySubscriber
import java.math.BigDecimal
import java.util.*
import java.util.regex.Pattern

class InvalidCurrencyException : Exception("Invalid currency inputted")

class TreasureCurrency(private val liteEco: LiteEco) : Currency {
    override fun getIdentifier(): String {
        return TreasuryEconomyAPI.currencyIdentifier
    }

    override fun getSymbol(): String {
        return liteEco.config.getString("economy.currency_prefix").toString()
    }

    override fun getDecimal(): Char {
        return '.'
    }

    override fun getDisplayNameSingular(): String {
        return liteEco.config.getString("economy.currency_name").toString()
    }

    override fun getDisplayNamePlural(): String {
        return displayNameSingular
    }

    override fun getPrecision(): Int {
        return 2
    }

    override fun isPrimary(): Boolean {
        return true
    }

    override fun to(currency: Currency, amount: BigDecimal, subscription: EconomySubscriber<BigDecimal>) {
        subscription.fail(EconomyException(EconomyFailureReason.FEATURE_NOT_SUPPORTED))
    }

    override fun parse(formatted: String, subscription: EconomySubscriber<BigDecimal>) {
        try {
            subscription.succeed(parseCurrencyValue(formatted))
        } catch (e: Exception) {
            val failureReason = when (e) {
                is NumberFormatException -> TreasuryFailureReasons.INVALID_VALUE
                is IllegalArgumentException -> EconomyFailureReason.NEGATIVE_BALANCES_NOT_SUPPORTED
                is InvalidCurrencyException -> TreasuryFailureReasons.INVALID_CURRENCY
                else -> throw e
            }
            subscription.fail(EconomyException(failureReason))
        }
    }

    private fun parseCurrencyValue(formatted: String): BigDecimal {
        val pattern = Pattern.compile("^([^\\d.,]+)?([\\d,.]+)([^\\d.,]+)?$")
        val matcher = pattern.matcher(formatted)

        if (!matcher.matches()) throw NumberFormatException()

        val currencySuffix = matcher.group(1)?.trim()
        val currencyValue = matcher.group(2).replace(",", "").toDoubleOrNull() ?: throw NumberFormatException()
        val currencyPrefix = matcher.group(3)?.trim()

        require(currencyValue >= 0) { "Negative balances not supported." }

        if ((currencyPrefix == null || !matchCurrency(currencyPrefix) || (currencySuffix == null || !matchCurrency(currencySuffix)))) {
            throw InvalidCurrencyException()
        }

        return BigDecimal.valueOf(currencyValue)
    }

    private fun matchCurrency(currency: String): Boolean {
        return if (currency.length == 1) {
            currency[0] == decimal
        } else {
            (currency.equals(symbol, ignoreCase = true)
                    || currency.equals(displayNameSingular, ignoreCase = true)
                    || currency.equals(displayNamePlural, ignoreCase = true))
        }
    }

    override fun getStartingBalance(playerID: UUID?): BigDecimal {
        return BigDecimal.valueOf(liteEco.config.getDouble("economy.starting_balance"))
    }

    override fun format(amount: BigDecimal, locale: Locale?): String {
        return liteEco.api.formatting(amount.toDouble())
    }

    override fun format(amount: BigDecimal, locale: Locale?, precision: Int): String {
        return format(amount, null)
    }
}