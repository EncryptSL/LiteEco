package com.github.encryptsl.lite.eco.common.hook.vault.unlocked

import net.milkbowl.vault2.economy.AccountPermission
import net.milkbowl.vault2.economy.Economy
import net.milkbowl.vault2.economy.EconomyResponse
import java.math.BigDecimal
import java.util.*

abstract class UnusedVaultUnlockedAPI : Economy {
    companion object {
        private const val SHARED_ACCOUNTS_NOT_SUPPORTED_MESSAGE = "LiteEco does not support shared accounts !"
        private const val PERMISSIONS_NOT_SUPPORTED_MESSAGE = "LiteEco does not support permissions operation!"
    }

    @Deprecated("Deprecated in Java")
    override fun createAccount(accountID: UUID, name: String): Boolean {
        return false
    }

    @Deprecated("Deprecated in Java")
    override fun createAccount(accountID: UUID, name: String, worldName: String): Boolean {
        return false
    }

    @Deprecated("Deprecated in Java", ReplaceWith("format(pluginName, amount)"))
    override fun format(amount: BigDecimal): String {
        return format("", amount)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("format(pluginName, amount, currency)"))
    override fun format(amount: BigDecimal, currency: String): String {
        return format("pluginName", amount, currency)
    }

    override fun createSharedAccount(pluginName: String, accountID: UUID, name: String, owner: UUID): Boolean {
        return EconomyResponse(BigDecimal.ZERO, BigDecimal.ZERO, EconomyResponse.ResponseType.NOT_IMPLEMENTED, SHARED_ACCOUNTS_NOT_SUPPORTED_MESSAGE).transactionSuccess()
    }

    override fun hasSharedAccountSupport(): Boolean {
        return EconomyResponse(BigDecimal.ZERO, BigDecimal.ZERO, EconomyResponse.ResponseType.NOT_IMPLEMENTED, SHARED_ACCOUNTS_NOT_SUPPORTED_MESSAGE).transactionSuccess()
    }

    override fun isAccountOwner(pluginName: String, accountID: UUID, uuid: UUID): Boolean {
        return EconomyResponse(BigDecimal.ZERO, BigDecimal.ZERO, EconomyResponse.ResponseType.NOT_IMPLEMENTED, SHARED_ACCOUNTS_NOT_SUPPORTED_MESSAGE).transactionSuccess()
    }

    override fun setOwner(pluginName: String, accountID: UUID, uuid: UUID): Boolean {
        return EconomyResponse(BigDecimal.ZERO, BigDecimal.ZERO, EconomyResponse.ResponseType.NOT_IMPLEMENTED, SHARED_ACCOUNTS_NOT_SUPPORTED_MESSAGE).transactionSuccess()
    }

    override fun isAccountMember(pluginName: String, accountID: UUID, uuid: UUID): Boolean {
        return EconomyResponse(BigDecimal.ZERO, BigDecimal.ZERO, EconomyResponse.ResponseType.NOT_IMPLEMENTED, SHARED_ACCOUNTS_NOT_SUPPORTED_MESSAGE).transactionSuccess()
    }

    override fun addAccountMember(pluginName: String, accountID: UUID, uuid: UUID): Boolean {
        return EconomyResponse(BigDecimal.ZERO, BigDecimal.ZERO, EconomyResponse.ResponseType.NOT_IMPLEMENTED, SHARED_ACCOUNTS_NOT_SUPPORTED_MESSAGE).transactionSuccess()
    }

    override fun addAccountMember(pluginName: String, accountID: UUID, uuid: UUID, vararg initialPermissions: AccountPermission): Boolean {
        return EconomyResponse(BigDecimal.ZERO, BigDecimal.ZERO, EconomyResponse.ResponseType.NOT_IMPLEMENTED, SHARED_ACCOUNTS_NOT_SUPPORTED_MESSAGE).transactionSuccess()
    }

    override fun removeAccountMember(pluginName: String, accountID: UUID, uuid: UUID): Boolean {
        return EconomyResponse(BigDecimal.ZERO, BigDecimal.ZERO, EconomyResponse.ResponseType.NOT_IMPLEMENTED, SHARED_ACCOUNTS_NOT_SUPPORTED_MESSAGE).transactionSuccess()
    }

    override fun hasAccountPermission(pluginName: String, accountID: UUID, uuid: UUID, permission: AccountPermission): Boolean {
        return EconomyResponse(BigDecimal.ZERO, BigDecimal.ZERO, EconomyResponse.ResponseType.NOT_IMPLEMENTED, PERMISSIONS_NOT_SUPPORTED_MESSAGE).transactionSuccess()
    }

    override fun updateAccountPermission(
        pluginName: String,
        accountID: UUID,
        uuid: UUID,
        permission: AccountPermission,
        value: Boolean
    ): Boolean {
        return EconomyResponse(BigDecimal.ZERO, BigDecimal.ZERO, EconomyResponse.ResponseType.NOT_IMPLEMENTED, PERMISSIONS_NOT_SUPPORTED_MESSAGE).transactionSuccess()
    }

}