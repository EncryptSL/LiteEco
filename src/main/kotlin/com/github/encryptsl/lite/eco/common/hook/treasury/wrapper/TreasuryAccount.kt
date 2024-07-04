package com.github.encryptsl.lite.eco.common.hook.treasury.wrapper

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.common.extensions.isApproachingZero
import com.github.encryptsl.lite.eco.utils.PlayerUtils
import me.lokka30.treasury.api.economy.account.Account
import me.lokka30.treasury.api.economy.currency.Currency
import me.lokka30.treasury.api.economy.transaction.EconomyTransaction
import me.lokka30.treasury.api.economy.transaction.EconomyTransactionType
import org.bukkit.Bukkit
import java.math.BigDecimal
import java.time.temporal.Temporal
import java.util.Optional
import java.util.UUID
import java.util.concurrent.CompletableFuture

abstract class TreasuryAccount(private val uuid: UUID) : Account {

    companion object {
        const val PLAYER_NOT_FOUND = "Player don't have account."
        const val AMOUNT_IS_NEGATIVE_OR_ZERO = "Amount is zero or negative."
        const val AMOUNT_REACH_LIMIT_OF_BALANCE = "Limit of max balance is reached."
    }

    override fun getName(): Optional<String> {
        return Optional.ofNullable(PlayerUtils.getOfflinePlayer(uuid).name)
    }

    override fun retrieveBalance(currency: Currency): CompletableFuture<BigDecimal> {
        if (!currency.identifier.equals(TreasuryCurrency.CURRENCY_IDENTIFIER, true)) {
            return CompletableFuture.failedFuture(UnsupportedOperationException("LiteEco Currency not found !"))
        } else {
            return CompletableFuture.completedFuture(LiteEco.instance.api.getBalance(PlayerUtils.getOfflinePlayer(uuid)))
        }
    }

    override fun doTransaction(economyTransaction: EconomyTransaction): CompletableFuture<BigDecimal> {
        return CompletableFuture.supplyAsync {
            if (!economyTransaction.currencyId.equals(TreasuryCurrency.CURRENCY_IDENTIFIER, true)) {
                throw UnsupportedOperationException("LiteEco Currency not found !")
            }

            val money = economyTransaction.amount
            val player = PlayerUtils.getOfflinePlayer(uuid)
            val isApproachingZero = money.isApproachingZero()
            val playerHasAccount = LiteEco.instance.api.hasAccount(player).join()

            when (economyTransaction.type) {
                EconomyTransactionType.DEPOSIT -> {
                    if (!playerHasAccount) {
                        throw IllegalArgumentException(PLAYER_NOT_FOUND)
                    }
                    if (isApproachingZero) {
                        throw IllegalArgumentException(AMOUNT_IS_NEGATIVE_OR_ZERO)
                    }
                    if(LiteEco.instance.api.getCheckBalanceLimit(player, LiteEco.instance.currencyImpl.defaultCurrency(), money)) {
                        throw IllegalArgumentException(AMOUNT_REACH_LIMIT_OF_BALANCE)
                    }

                    LiteEco.instance.api.depositMoney(player, LiteEco.instance.currencyImpl.defaultCurrency(), money)
                }
                EconomyTransactionType.WITHDRAWAL -> {
                    if (!playerHasAccount) {
                        throw IllegalArgumentException(PLAYER_NOT_FOUND)
                    }
                    if (isApproachingZero) {
                        throw IllegalArgumentException(AMOUNT_IS_NEGATIVE_OR_ZERO)
                    }

                    LiteEco.instance.api.withDrawMoney(player, LiteEco.instance.currencyImpl.defaultCurrency(), money)
                }
                EconomyTransactionType.SET -> {
                    if (!playerHasAccount) {
                        throw IllegalArgumentException(PLAYER_NOT_FOUND)
                    }
                    LiteEco.instance.api.setMoney(player, LiteEco.instance.currencyImpl.defaultCurrency(), money)
                }
            }
            return@supplyAsync LiteEco.instance.api.getBalance(player)
        }
    }

    override fun deleteAccount(): CompletableFuture<Boolean> {
        if (!LiteEco.instance.api.hasAccount(PlayerUtils.getOfflinePlayer(uuid)).join())
            return CompletableFuture.completedFuture(false)

        return CompletableFuture.completedFuture(LiteEco.instance.api.deleteAccount(Bukkit.getOfflinePlayer(uuid)))
    }

    override fun retrieveHeldCurrencies(): CompletableFuture<MutableCollection<String>> {
        return CompletableFuture.failedFuture(UnsupportedOperationException("LiteEco don't use more currencies."))
    }

    override fun retrieveTransactionHistory(
        transactionCount: Int,
        from: Temporal,
        to: Temporal
    ): CompletableFuture<MutableCollection<EconomyTransaction>> {
        return CompletableFuture.failedFuture(UnsupportedOperationException("LiteEco use own transaction history..."))
    }
}