package com.github.encryptsl.lite.eco.common.hook.treasury

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.common.hook.treasury.impl.TreasuryNonPlayerAccessor
import com.github.encryptsl.lite.eco.common.hook.treasury.impl.TreasuryPlayerAccessor
import com.github.encryptsl.lite.eco.common.hook.treasury.wrapper.TreasuryCurrency
import com.github.encryptsl.lite.eco.common.hook.treasury.wrapper.TreasuryNonPlayerAccount
import com.github.encryptsl.lite.eco.utils.PlayerUtils
import me.lokka30.treasury.api.common.NamespacedKey
import me.lokka30.treasury.api.common.misc.TriState
import me.lokka30.treasury.api.economy.EconomyProvider
import me.lokka30.treasury.api.economy.account.AccountData
import me.lokka30.treasury.api.economy.account.accessor.AccountAccessor
import me.lokka30.treasury.api.economy.account.accessor.NonPlayerAccountAccessor
import me.lokka30.treasury.api.economy.account.accessor.PlayerAccountAccessor
import me.lokka30.treasury.api.economy.currency.Currency
import java.lang.UnsupportedOperationException
import java.util.*
import java.util.concurrent.CompletableFuture

class TreasuryEconomyAPI(private val liteEco: LiteEco) : EconomyProvider, AccountAccessor {

    private val playerAccessor: TreasuryPlayerAccessor = TreasuryPlayerAccessor(liteEco)
    private val nonPlayerAccessor: TreasuryNonPlayerAccessor = TreasuryNonPlayerAccessor()
    private val currency: TreasuryCurrency = TreasuryCurrency(liteEco)

    override fun player(): PlayerAccountAccessor {
        return playerAccessor
    }

    override fun nonPlayer(): NonPlayerAccountAccessor {
        return nonPlayerAccessor
    }

    override fun accountAccessor(): AccountAccessor {
        return this
    }

    override fun hasAccount(accountData: AccountData): CompletableFuture<Boolean> {
        if (!accountData.isPlayerAccount) {
            return CompletableFuture.failedFuture(UnsupportedOperationException(TreasuryNonPlayerAccount.UNSUPPORTED_NON_PLAYER))
        }

        return LiteEco.instance.api.hasAccount(PlayerUtils.getOfflinePlayer(accountData.playerIdentifier.get()))
    }

    override fun retrievePlayerAccountIds(): CompletableFuture<MutableCollection<UUID>> {
        return LiteEco.instance.databaseEcoModel.getPlayersIds(liteEco.currencyImpl.defaultCurrency())
    }

    override fun retrieveNonPlayerAccountIds(): CompletableFuture<MutableCollection<NamespacedKey>> {
        return CompletableFuture.failedFuture(UnsupportedOperationException(TreasuryNonPlayerAccount.UNSUPPORTED_NON_PLAYER))
    }

    override fun getPrimaryCurrency(): Currency {
        return currency
    }

    override fun findCurrency(identifier: String): Optional<Currency> {
        return Optional.ofNullable(currency)
    }

    override fun getCurrencies(): MutableSet<Currency> {
        return mutableSetOf(currency)
    }

    override fun registerCurrency(currency: Currency): CompletableFuture<TriState> {
        return CompletableFuture.completedFuture(null)
    }

    override fun unregisterCurrency(currency: Currency): CompletableFuture<TriState> {
        return CompletableFuture.completedFuture(null)
    }
}