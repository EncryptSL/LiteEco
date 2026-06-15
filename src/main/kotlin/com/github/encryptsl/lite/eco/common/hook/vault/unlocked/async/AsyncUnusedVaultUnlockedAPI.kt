package com.github.encryptsl.lite.eco.common.hook.vault.unlocked.async

import net.milkbowl.vault2.economy.AccountPermission
import net.milkbowl.vault2.economy.AsyncEconomy
import java.util.UUID
import java.util.concurrent.CompletableFuture

abstract class AsyncUnusedVaultUnlockedAPI : AsyncEconomy {

    companion object {
        private const val SHARED_ACCOUNTS_NOT_SUPPORTED_MESSAGE = "LiteEco does not support shared accounts!"

        private fun <T> unsupported(): CompletableFuture<T> {
            return CompletableFuture.failedFuture(UnsupportedOperationException(SHARED_ACCOUNTS_NOT_SUPPORTED_MESSAGE))
        }
    }

    override fun createSharedAccount(
        pluginName: String,
        accountID: UUID,
        name: String,
        owner: UUID
    ): CompletableFuture<Boolean?> = unsupported()

    override fun accountsWithOwnerOf(
        pluginName: String,
        accountID: UUID
    ): CompletableFuture<List<UUID?>?> = unsupported()

    override fun accountsWithMembershipTo(
        pluginName: String,
        accountID: UUID
    ): CompletableFuture<List<UUID?>?> = unsupported()

    override fun accountsWithAccessTo(
        pluginName: String,
        accountID: UUID,
        vararg permissions: AccountPermission
    ): CompletableFuture<List<UUID?>?> = unsupported()

    override fun isAccountOwner(
        pluginName: String,
        accountID: UUID,
        uuid: UUID
    ): CompletableFuture<Boolean?> = unsupported()

    override fun setOwner(
        pluginName: String,
        accountID: UUID,
        uuid: UUID
    ): CompletableFuture<Boolean?> = unsupported()

    override fun isAccountMember(
        pluginName: String,
        accountID: UUID,
        uuid: UUID
    ): CompletableFuture<Boolean?> = unsupported()

    override fun addAccountMember(
        pluginName: String,
        accountID: UUID,
        uuid: UUID
    ): CompletableFuture<Boolean?> = unsupported()

    override fun addAccountMember(
        pluginName: String,
        accountID: UUID,
        uuid: UUID,
        vararg initialPermissions: AccountPermission
    ): CompletableFuture<Boolean?> = unsupported()

    override fun removeAccountMember(
        pluginName: String,
        accountID: UUID,
        uuid: UUID
    ): CompletableFuture<Boolean?> = unsupported()

    override fun hasAccountPermission(
        pluginName: String,
        accountID: UUID,
        uuid: UUID,
        permission: AccountPermission
    ): CompletableFuture<Boolean?> = unsupported()

    override fun updateAccountPermission(
        pluginName: String,
        accountID: UUID,
        uuid: UUID,
        permission: AccountPermission,
        value: Boolean
    ): CompletableFuture<Boolean?> = unsupported()

}