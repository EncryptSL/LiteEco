package com.github.encryptsl.lite.eco.common.hook.economy.craftconomy3

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.common.hook.HookListener
import com.github.encryptsl.lite.eco.utils.ClassUtil
import com.greatmancode.craftconomy3.Common
import com.greatmancode.craftconomy3.account.AccountManager

class CraftConomyHook(
    private val liteEco: LiteEco
) : HookListener(
    PLUGIN_NAME,
    "You can now export economy from plugin CraftConomy3 to LiteEco with /eco database import CraftConomy3 <your_currency>"
) {
    private var economyHandler: AccountManager? = null

    companion object {
        const val PLUGIN_NAME = "CraftConomy3"
        fun isCraftEconomyPresent(): Boolean
                = ClassUtil.isValidClasspath("com.greatmancode.craftconomy3.Common")
    }

    override fun canRegister(): Boolean {
        val plugin = liteEco.pluginManager.getPlugin(PLUGIN_NAME)
        return !registered && plugin != null && isCraftEconomyPresent()
    }

    override fun register() {
        if (isCraftEconomyPresent()) {
            registered = true
        }
    }

    override fun unregister() {}

    fun getBalance(name: String, currency: String = "Dollar"): Double {
        return if (isCraftEconomyPresent()) {
            economyHandler = Common.getInstance().accountManager
            economyHandler?.getAccount(name, false)?.getBalance(null, currency) ?: 0.00
        } else 0.00
    }
}