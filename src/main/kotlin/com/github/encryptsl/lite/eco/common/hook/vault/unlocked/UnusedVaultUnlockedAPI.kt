@file:Suppress("DEPRECATION")
package com.github.encryptsl.lite.eco.common.hook.vault.unlocked

import net.milkbowl.vault2.economy.AccountPermission
import net.milkbowl.vault2.economy.Economy
import java.math.BigDecimal
import java.util.*

abstract class UnusedVaultUnlockedAPI : Economy {

    @Deprecated("Deprecated in Java", ReplaceWith("createAccount(accountID, name)"))
    override fun createAccount(accountID: UUID, name: String): Boolean {
        return createAccount(accountID, name)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("createAccount(accountID, name)"))
    override fun createAccount(accountID: UUID, name: String, worldName: String): Boolean {
        return createAccount(accountID, name)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("format(pluginName, amount)"))
    override fun format(amount: BigDecimal): String {
        return format("", amount)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("format(pluginName, amount, currency)"))
    override fun format(amount: BigDecimal, currency: String): String {
        return format("", amount, currency)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("balance(pluginName, accountID)"))
    override fun getBalance(pluginName: String, accountID: UUID): BigDecimal {
        return balance(pluginName, accountID)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("balance(pluginName, accountID)"))
    override fun getBalance(pluginName: String, accountID: UUID, world: String): BigDecimal {
        return balance(pluginName, accountID)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("balance(pluginName, accountID, currency)"))
    override fun getBalance(pluginName: String, accountID: UUID, world: String, currency: String): BigDecimal {
        return balance(pluginName, accountID, currency)
    }

    override fun hasSharedAccountSupport(): Boolean = false

    override fun createSharedAccount(pluginName: String, accountID: UUID, name: String, owner: UUID): Boolean = false

    override fun isAccountOwner(pluginName: String, accountID: UUID, uuid: UUID): Boolean = false

    override fun setOwner(pluginName: String, accountID: UUID, uuid: UUID): Boolean = false

    override fun isAccountMember(pluginName: String, accountID: UUID, uuid: UUID): Boolean = false

    override fun addAccountMember(pluginName: String, accountID: UUID, uuid: UUID): Boolean = false

    override fun addAccountMember(pluginName: String, accountID: UUID, uuid: UUID, vararg initialPermissions: AccountPermission): Boolean = false

    override fun removeAccountMember(pluginName: String, accountID: UUID, uuid: UUID): Boolean = false

    override fun hasAccountPermission(pluginName: String, accountID: UUID, uuid: UUID, permission: AccountPermission): Boolean = false

    override fun updateAccountPermission(
        pluginName: String,
        accountID: UUID,
        uuid: UUID,
        permission: AccountPermission,
        value: Boolean
    ): Boolean = false
}