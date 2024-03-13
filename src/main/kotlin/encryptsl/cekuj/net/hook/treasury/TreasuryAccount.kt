package encryptsl.cekuj.net.hook.treasury

import encryptsl.cekuj.net.LiteEco
import me.lokka30.treasury.api.economy.account.PlayerAccount
import me.lokka30.treasury.api.economy.currency.Currency
import me.lokka30.treasury.api.economy.transaction.EconomyTransaction
import org.bukkit.Bukkit
import java.math.BigDecimal
import java.time.temporal.Temporal
import java.util.*
import java.util.concurrent.CompletableFuture


class TreasuryAccount(private val liteEco: LiteEco, private val uuid: UUID) : PlayerAccount {
    override fun getName(): Optional<String> {
        return Optional.ofNullable(Bukkit.getOfflinePlayer(uuid).name)
    }

    override fun retrieveBalance(currency: Currency): CompletableFuture<BigDecimal> {
        TODO("Not yet implemented")
    }

    override fun doTransaction(economyTransaction: EconomyTransaction): CompletableFuture<BigDecimal> {
        return CompletableFuture()
    }

    override fun deleteAccount(): CompletableFuture<Boolean> {
        TODO("Not yet implemented")
    }

    override fun retrieveHeldCurrencies(): CompletableFuture<MutableCollection<String>> {
        TODO("Not yet implemented")
    }

    override fun retrieveTransactionHistory(
        transactionCount: Int,
        from: Temporal,
        to: Temporal
    ): CompletableFuture<MutableCollection<EconomyTransaction>> {
        TODO("Not yet implemented")
    }

    override fun identifier(): UUID {
        return uuid
    }
}