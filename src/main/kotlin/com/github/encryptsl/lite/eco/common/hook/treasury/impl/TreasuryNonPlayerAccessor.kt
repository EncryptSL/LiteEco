package com.github.encryptsl.lite.eco.common.hook.treasury.impl

import com.github.encryptsl.lite.eco.common.hook.treasury.wrapper.TreasuryNonPlayerAccount
import me.lokka30.treasury.api.economy.account.NonPlayerAccount
import me.lokka30.treasury.api.economy.account.accessor.NonPlayerAccountAccessor
import java.util.concurrent.CompletableFuture

class TreasuryNonPlayerAccessor : NonPlayerAccountAccessor() {

    override fun getOrCreate(context: NonPlayerAccountCreateContext): CompletableFuture<NonPlayerAccount?> {
        return CompletableFuture.failedFuture(UnsupportedOperationException(TreasuryNonPlayerAccount.UNSUPPORTED_NON_PLAYER))
    }
}