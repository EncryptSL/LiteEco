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
    "You can now export economy from plugin Craftconomy3 to LiteEco with /eco database import Craftconomy3 <your_currency>"
) {
    private val economyHandler: AccountManager?
        get() = if (isCraftEconomyPresent()) Common.getInstance().accountManager else null

    companion object {
        const val PLUGIN_NAME = "Craftconomy3"
        fun isCraftEconomyPresent(): Boolean
                = ClassUtil.isValidClasspath("com.greatmancode.craftconomy3.Common")
    }

    override fun canRegister(): Boolean {
        val plugin = liteEco.pluginManager.getPlugin(PLUGIN_NAME)
        return !registered && plugin != null && isCraftEconomyPresent()
    }

    override fun register() {
        registered = (economyHandler != null)
    }

    override fun unregister() {}

    // Still needs more testing, because for some reason balance can't be imported !
    fun getBalance(name: String, currency: String = "dollar"): Double {
        return try {
            economyHandler?.getAccount(name, false)?.getBalance(null, currency) ?: 0.0
        } catch (e: Exception) {
            liteEco.componentLogger.warn("Failed to get $PLUGIN_NAME balance for $name: ${e.message}")
            0.0
        }
    }
}