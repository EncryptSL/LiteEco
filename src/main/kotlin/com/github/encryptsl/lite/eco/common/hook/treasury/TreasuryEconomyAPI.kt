package com.github.encryptsl.lite.eco.common.hook.treasury

import com.github.encryptsl.lite.eco.LiteEco
import me.lokka30.treasury.api.common.NamespacedKey
import me.lokka30.treasury.api.common.misc.TriState
import me.lokka30.treasury.api.economy.EconomyProvider
import me.lokka30.treasury.api.economy.account.AccountData
import me.lokka30.treasury.api.economy.account.accessor.AccountAccessor
import me.lokka30.treasury.api.economy.currency.Currency
import java.util.*
import java.util.concurrent.CompletableFuture

class TreasuryEconomyAPI(private val liteEco: LiteEco, private val currency: Currency) : EconomyProvider {
    override fun accountAccessor(): AccountAccessor {
        TODO("Not yet implemented")
    }

    override fun hasAccount(accountData: AccountData): CompletableFuture<Boolean> {
        TODO("Not yet implemented")
    }

    override fun retrievePlayerAccountIds(): CompletableFuture<MutableCollection<UUID>> {
        TODO("Not yet implemented")
    }

    override fun retrieveNonPlayerAccountIds(): CompletableFuture<MutableCollection<NamespacedKey>> {
        TODO("Not yet implemented")
    }

    override fun getPrimaryCurrency(): Currency {
        TODO("Not yet implemented")
    }

    override fun findCurrency(identifier: String): Optional<Currency> {
        TODO("Not yet implemented")
    }

    override fun getCurrencies(): MutableSet<Currency> {
        TODO("Not yet implemented")
    }

    override fun registerCurrency(currency: Currency): CompletableFuture<TriState> {
        TODO("Not yet implemented")
    }

    override fun unregisterCurrency(currency: Currency): CompletableFuture<TriState> {
        TODO("Not yet implemented")
    }

}