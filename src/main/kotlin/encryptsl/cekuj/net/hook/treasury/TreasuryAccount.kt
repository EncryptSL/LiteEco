package encryptsl.cekuj.net.hook.treasury

import encryptsl.cekuj.net.LiteEco
import encryptsl.cekuj.net.extensions.isApproachingZero
import me.lokka30.treasury.api.economy.account.PlayerAccount
import me.lokka30.treasury.api.economy.currency.Currency
import me.lokka30.treasury.api.economy.response.EconomyException
import me.lokka30.treasury.api.economy.response.EconomyFailureReason
import me.lokka30.treasury.api.economy.response.EconomySubscriber
import me.lokka30.treasury.api.economy.transaction.EconomyTransaction
import me.lokka30.treasury.api.economy.transaction.EconomyTransactionInitiator
import me.lokka30.treasury.api.economy.transaction.EconomyTransactionType
import org.bukkit.Bukkit
import java.math.BigDecimal
import java.time.temporal.Temporal
import java.util.*


class TreasuryAccount(private val liteEco: LiteEco, private val uuid: UUID) : PlayerAccount {
    override fun getName(): Optional<String> {
        return Optional.ofNullable(Bukkit.getOfflinePlayer(uuid).name)
    }

    override fun retrieveBalance(currency: Currency, subscription: EconomySubscriber<BigDecimal>) {
        liteEco.server.scheduler.runTaskAsynchronously(liteEco, Runnable {
            if (!currency.equals(TreasuryEconomyAPI.currencyIdentifier)) {
                subscription.fail(EconomyException(EconomyFailureReason.CURRENCY_NOT_FOUND))
                return@Runnable
            }

            val amount: Double = liteEco.api.getBalance(Bukkit.getOfflinePlayer(uuid))
            subscription.succeed(BigDecimal.valueOf(amount))
        })
    }

    override fun setBalance(
        amount: BigDecimal,
        initiator: EconomyTransactionInitiator<*>,
        currency: Currency,
        subscription: EconomySubscriber<BigDecimal>
    ) {
        liteEco.server.scheduler.runTaskAsynchronously(liteEco, Runnable {
            val amountDouble: Double = amount.toDouble()
            if (!currency.equals(TreasuryEconomyAPI.currencyIdentifier)) {
                subscription.fail(EconomyException(EconomyFailureReason.CURRENCY_NOT_FOUND))
                return@Runnable
            }

            if (amountDouble.isApproachingZero()) {
                subscription.fail(EconomyException(EconomyFailureReason.NEGATIVE_AMOUNT_SPECIFIED))
                return@Runnable
            }

            liteEco.api.setMoney(Bukkit.getOfflinePlayer(uuid), amountDouble)
            subscription.succeed(BigDecimal.valueOf(amountDouble))
        })
    }

    override fun doTransaction(economyTransaction: EconomyTransaction, subscription: EconomySubscriber<BigDecimal>) {
        liteEco.server.scheduler.runTaskAsynchronously(liteEco, Runnable {
            if (!economyTransaction.equals(TreasuryEconomyAPI.currencyIdentifier)) {
                subscription.fail(EconomyException(EconomyFailureReason.CURRENCY_NOT_FOUND))
                return@Runnable
            }
            val type = economyTransaction.transactionType
            val amount = economyTransaction.transactionAmount
            val amountDouble = amount.toDouble()

            if (amountDouble.isApproachingZero()) {
                subscription.fail(EconomyException(EconomyFailureReason.NEGATIVE_AMOUNT_SPECIFIED))
                return@Runnable
            }

            if (type == EconomyTransactionType.DEPOSIT) {
                liteEco.api.depositMoney(Bukkit.getOfflinePlayer(uuid), amountDouble)
            } else if (type == EconomyTransactionType.WITHDRAWAL) {
                liteEco.api.withDrawMoney(Bukkit.getOfflinePlayer(uuid), amountDouble)
            }

            val balance: Double = liteEco.api.getBalance(Bukkit.getOfflinePlayer(uuid))
            subscription.succeed(BigDecimal.valueOf(balance))
        })
    }

    override fun deleteAccount(subscription: EconomySubscriber<Boolean>) {
        liteEco.server.scheduler.runTaskAsynchronously(liteEco, Runnable {
            val status: Boolean = liteEco.api.deleteAccount(Bukkit.getOfflinePlayer(uuid))
            subscription.succeed(status)
        })
    }

    override fun retrieveHeldCurrencies(subscription: EconomySubscriber<MutableCollection<String>>) {
        subscription.fail(EconomyException(EconomyFailureReason.FEATURE_NOT_SUPPORTED))
    }

    override fun retrieveTransactionHistory(
        transactionCount: Int,
        from: Temporal,
        to: Temporal,
        subscription: EconomySubscriber<MutableCollection<EconomyTransaction>>
    ) {
        subscription.fail(EconomyException(EconomyFailureReason.FEATURE_NOT_SUPPORTED))
    }

    override fun getUniqueId(): UUID {
        return uuid
    }
}