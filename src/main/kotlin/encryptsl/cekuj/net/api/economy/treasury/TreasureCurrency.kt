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
        TODO("Not yet implemented")
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