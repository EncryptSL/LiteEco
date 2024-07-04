package com.github.encryptsl.lite.eco.common.hook.treasury.impl

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.common.hook.treasury.wrapper.TreasuryPlayerAccount
import com.github.encryptsl.lite.eco.utils.PlayerUtils
import me.lokka30.treasury.api.economy.account.PlayerAccount
import me.lokka30.treasury.api.economy.account.accessor.PlayerAccountAccessor
import java.util.concurrent.CompletableFuture

class TreasuryPlayerAccessor(private val liteEco: LiteEco) : PlayerAccountAccessor() {

    override fun getOrCreate(context: PlayerAccountCreateContext): CompletableFuture<PlayerAccount?> {
        val uuid = context.uniqueId
        val offlinePlayer = PlayerUtils.getOfflinePlayer(uuid)

        if (!offlinePlayer.hasPlayedBefore()) {
            return CompletableFuture.failedFuture(IllegalArgumentException("Invalid player UUID"))
        }

        liteEco.api.createAccount(offlinePlayer, liteEco.currencyImpl.defaultCurrency(), liteEco.currencyImpl.defaultStartBalance())

        return CompletableFuture.completedFuture(TreasuryPlayerAccount(context.uniqueId))
    }
}