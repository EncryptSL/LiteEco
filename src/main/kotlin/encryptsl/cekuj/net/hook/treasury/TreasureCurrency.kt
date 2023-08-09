package encryptsl.cekuj.net.hook.treasury

import encryptsl.cekuj.net.LiteEco
import me.lokka30.treasury.api.economy.currency.Currency
import me.lokka30.treasury.api.economy.response.EconomyException
import me.lokka30.treasury.api.economy.response.EconomyFailureReason
import me.lokka30.treasury.api.economy.response.EconomySubscriber
import java.math.BigDecimal
import java.util.*
import java.util.regex.Pattern

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
        } catch (e: EconomyException) {
            subscription.fail(e)
        }
    }

    private fun parseCurrencyValue(formatted: String): BigDecimal {
        val pattern = Pattern.compile("^([^\\d.,]+)?([\\d,.]+)([^\\d.,]+)?$")
        val matcher = pattern.matcher(formatted)

        if (!matcher.matches()) throw EconomyException(TreasuryFailureReasons.INVALID_VALUE)

        val currencySuffix = matcher.group(1)?.trim()
        val currencyValue = matcher.group(2).replace(",", "").toDoubleOrNull()
        val currencyPrefix = matcher.group(3)?.trim()

        if (currencyValue == null) {
            throw EconomyException(TreasuryFailureReasons.INVALID_VALUE)
        }

        if (currencyValue >= 0) {
            throw EconomyException(EconomyFailureReason.NEGATIVE_BALANCES_NOT_SUPPORTED)
        }

        if (!matchCurrency(currencyPrefix) || !matchCurrency(currencySuffix)) {
            throw EconomyException(TreasuryFailureReasons.INVALID_CURRENCY)
        }

        return BigDecimal.valueOf(currencyValue)
    }

    private fun matchCurrency(currency: String?): Boolean {
        return when {
            currency == null -> false
            currency.length == 1 -> currency[0] == decimal
            currency.equals(symbol, ignoreCase = true) -> true
            currency.equals(displayNameSingular, ignoreCase = true) -> true
            currency.equals(displayNamePlural, ignoreCase = true) -> true
            else -> false
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