package encryptsl.cekuj.net.hook.treasury

import encryptsl.cekuj.net.LiteEco
import me.lokka30.treasury.api.economy.currency.Currency
import me.lokka30.treasury.api.economy.response.EconomyException
import me.lokka30.treasury.api.economy.response.EconomyFailureReason
import me.lokka30.treasury.api.economy.response.EconomySubscriber
import java.math.BigDecimal
import java.util.*

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
                else -> TreasuryFailureReasons.INVALID_CURRENCY
            }
            subscription.fail(EconomyException(failureReason))
        }
    }

    private fun parseCurrencyValue(formatted: String): BigDecimal {
        val valueBuilder = StringBuilder()
        var hadDot = false

        for (c in formatted.toCharArray()) {
            when {
                Character.isWhitespace(c) -> continue
                Character.isDigit(c) -> valueBuilder.append(c)
                isSeparator(c) -> {
                    if (c == decimal) {
                        if (!hadDot) hadDot = true
                        else throw NumberFormatException()
                    }
                    valueBuilder.append('.')
                }
            }
            if (Character.isWhitespace(c)) {
                continue
            }
        }

        if (valueBuilder.isEmpty()) {
            throw NumberFormatException()
        }

        val value = valueBuilder.toString().toDouble()
        if (value < 0) {
            throw IllegalArgumentException("Negative balances not supported.")
        }

        return BigDecimal.valueOf(value)
    }


    private fun matches(currency: String): Boolean {
        return if (currency.length == 1) {
            currency[0] == decimal
        } else {
            (currency.equals(symbol, ignoreCase = true)
                    || currency.equals(displayNameSingular, ignoreCase = true)
                    || currency.equals(displayNamePlural, ignoreCase = true))
        }
    }

    private fun isSeparator(c: Char): Boolean {
        return c == decimal || c == separator()
    }

    private fun separator(): Char {
        return ','
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