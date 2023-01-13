package encryptsl.cekuj.net.api.economy.treasury

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
        return liteEco.config.getString("plugin.economy.prefix").toString()
    }

    override fun getDecimal(): Char {
        val point: String = ".".trim()
        return if (point.isEmpty()) '.' else point[0]
    }

    override fun getDisplayNameSingular(): String {
        return liteEco.config.getString("plugin.economy.name").toString()
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
        var valueBuilder = StringBuilder()
        val currencyBuilder = StringBuilder()

        var hadDot = false
        for (c in formatted.toCharArray()) {
            if (Character.isWhitespace(c)) {
                continue
            }
            if (!Character.isDigit(c) && !isSeparator(c)) {
                currencyBuilder.append(c)
            } else if (Character.isDigit(c)) {
                valueBuilder.append(c)
            } else if (isSeparator(c)) {
                if (c == decimal) {
                    var nowChanged = false
                    if (!hadDot) {
                        hadDot = true
                        nowChanged = true
                    }
                    if (!nowChanged) {
                        valueBuilder = StringBuilder()
                        break
                    }
                }
                valueBuilder.append('.')
            }
        }

        if (currencyBuilder.isEmpty()) {
            subscription.fail(EconomyException(TreasuryFailureReasons.INVALID_CURRENCY))
            return
        }

        val currency = currencyBuilder.toString()
        if (!matches(currency)) {
            subscription.fail(EconomyException(TreasuryFailureReasons.INVALID_CURRENCY))
            return
        }

        if (valueBuilder.isEmpty()) {
            subscription.fail(EconomyException(TreasuryFailureReasons.INVALID_VALUE))
            return
        }

        try {
            val value = valueBuilder.toString().toDouble()
            if (value < 0) {
                subscription.fail(EconomyException(EconomyFailureReason.NEGATIVE_BALANCES_NOT_SUPPORTED))
                return
            }
            subscription.succeed(BigDecimal.valueOf(value))
        } catch (e: NumberFormatException) {
            subscription.fail(EconomyException(TreasuryFailureReasons.INVALID_VALUE, e))
        }
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
        val separator: String = ",".trim()
        return if (separator.isEmpty()) ',' else separator[0]
    }

    override fun getStartingBalance(playerID: UUID?): BigDecimal {
        return BigDecimal.valueOf(liteEco.config.getDouble("plugin.economy.default_money"))
    }

    override fun format(amount: BigDecimal, locale: Locale?): String {
        return liteEco.api.formatting(amount.toDouble())
    }

    override fun format(amount: BigDecimal, locale: Locale?, precision: Int): String {
        return format(amount, null)
    }


}