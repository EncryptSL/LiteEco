package com.github.encryptsl.lite.eco.common.hook.treasury.wrapper

import me.lokka30.treasury.api.common.NamespacedKey
import me.lokka30.treasury.api.common.misc.TriState
import me.lokka30.treasury.api.economy.account.AccountPermission
import me.lokka30.treasury.api.economy.account.NonPlayerAccount
import java.lang.UnsupportedOperationException
import java.util.UUID
import java.util.concurrent.CompletableFuture

class TreasuryNonPlayerAccount(val uuid: UUID) : TreasuryAccount(uuid), NonPlayerAccount {

    companion object {
        const val UNSUPPORTED_NON_PLAYER = "LiteEco not support non player accounts."
        const val UNSUPPORTED_FUNCTION = "LiteEco not support this function."
    }

    override fun identifier(): NamespacedKey {
        TODO("Not yet implemented")
    }

    override fun setName(name: String?): CompletableFuture<Boolean?> {
        return CompletableFuture.failedFuture(IllegalStateException("LiteEco not support changing names of accounts through Treasury."))
    }

    override fun retrieveMemberIds(): CompletableFuture<Collection<UUID>> {
        return CompletableFuture.failedFuture(IllegalStateException(UNSUPPORTED_FUNCTION))
    }

    override fun isMember(player: UUID): CompletableFuture<Boolean?> {
        return CompletableFuture.failedFuture(UnsupportedOperationException(UNSUPPORTED_FUNCTION))
    }

    override fun setPermissions(
        player: UUID,
        permissionsMap: Map<AccountPermission?, TriState?>
    ): CompletableFuture<Boolean?> {
        return CompletableFuture.failedFuture(IllegalStateException(UNSUPPORTED_FUNCTION))
    }

    override fun retrievePermissions(player: UUID): CompletableFuture<Map<AccountPermission?, TriState?>?> {
        return CompletableFuture.failedFuture(IllegalStateException(UNSUPPORTED_FUNCTION))
    }

    override fun retrievePermissionsMap(): CompletableFuture<Map<UUID?, Map<AccountPermission?, TriState?>?>?> {
        return CompletableFuture.failedFuture(IllegalStateException(UNSUPPORTED_FUNCTION))
    }

    override fun hasPermissions(
        player: UUID,
        vararg permissions: AccountPermission
    ): CompletableFuture<TriState?> {
        return CompletableFuture.failedFuture(IllegalStateException(UNSUPPORTED_FUNCTION))
    }
}