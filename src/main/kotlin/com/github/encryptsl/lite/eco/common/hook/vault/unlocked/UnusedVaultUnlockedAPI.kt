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

    override fun createSharedAccount(p0: String?, p1: UUID?, p2: String?, p3: UUID?): Boolean {
        return EconomyResponse(BigDecimal.ZERO, BigDecimal.ZERO, EconomyResponse.ResponseType.NOT_IMPLEMENTED, SHARED_ACCOUNTS_NOT_SUPPORTED_MESSAGE).transactionSuccess()
    }

    override fun hasSharedAccountSupport(): Boolean {
        return EconomyResponse(BigDecimal.ZERO, BigDecimal.ZERO, EconomyResponse.ResponseType.NOT_IMPLEMENTED, SHARED_ACCOUNTS_NOT_SUPPORTED_MESSAGE).transactionSuccess()
    }

    override fun isAccountOwner(p0: String?, p1: UUID?, p2: UUID?): Boolean {
        return EconomyResponse(BigDecimal.ZERO, BigDecimal.ZERO, EconomyResponse.ResponseType.NOT_IMPLEMENTED, SHARED_ACCOUNTS_NOT_SUPPORTED_MESSAGE).transactionSuccess()
    }

    override fun setOwner(p0: String?, p1: UUID?, p2: UUID?): Boolean {
        return EconomyResponse(BigDecimal.ZERO, BigDecimal.ZERO, EconomyResponse.ResponseType.NOT_IMPLEMENTED, SHARED_ACCOUNTS_NOT_SUPPORTED_MESSAGE).transactionSuccess()
    }

    override fun isAccountMember(p0: String?, p1: UUID?, p2: UUID?): Boolean {
        return EconomyResponse(BigDecimal.ZERO, BigDecimal.ZERO, EconomyResponse.ResponseType.NOT_IMPLEMENTED, SHARED_ACCOUNTS_NOT_SUPPORTED_MESSAGE).transactionSuccess()
    }

    override fun addAccountMember(p0: String?, p1: UUID?, p2: UUID?): Boolean {
        return EconomyResponse(BigDecimal.ZERO, BigDecimal.ZERO, EconomyResponse.ResponseType.NOT_IMPLEMENTED, SHARED_ACCOUNTS_NOT_SUPPORTED_MESSAGE).transactionSuccess()
    }

    override fun addAccountMember(p0: String?, p1: UUID?, p2: UUID?, vararg p3: AccountPermission?): Boolean {
        return EconomyResponse(BigDecimal.ZERO, BigDecimal.ZERO, EconomyResponse.ResponseType.NOT_IMPLEMENTED, SHARED_ACCOUNTS_NOT_SUPPORTED_MESSAGE).transactionSuccess()
    }

    override fun removeAccountMember(p0: String?, p1: UUID?, p2: UUID?): Boolean {
        return EconomyResponse(BigDecimal.ZERO, BigDecimal.ZERO, EconomyResponse.ResponseType.NOT_IMPLEMENTED, SHARED_ACCOUNTS_NOT_SUPPORTED_MESSAGE).transactionSuccess()
    }


    override fun hasAccountPermission(p0: String?, p1: UUID?, p2: UUID?, p3: AccountPermission?): Boolean {
        return EconomyResponse(BigDecimal.ZERO, BigDecimal.ZERO, EconomyResponse.ResponseType.NOT_IMPLEMENTED, PERMISSIONS_NOT_SUPPORTED_MESSAGE).transactionSuccess()
    }

    override fun updateAccountPermission(
        p0: String?,
        p1: UUID?,
        p2: UUID?,
        p3: AccountPermission?,
        p4: Boolean
    ): Boolean {
        return EconomyResponse(BigDecimal.ZERO, BigDecimal.ZERO, EconomyResponse.ResponseType.NOT_IMPLEMENTED, PERMISSIONS_NOT_SUPPORTED_MESSAGE).transactionSuccess()
    }

}