package com.github.encryptsl.lite.eco.common.hook.craftconomy3

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.common.hook.HookListener
import com.github.encryptsl.lite.eco.common.hook.bettereconomy.BetterEconomyHook
import com.github.encryptsl.lite.eco.common.hook.bettereconomy.BetterEconomyHook.Companion.isBetterEconomy
import com.github.encryptsl.lite.eco.utils.ClassUtil
import com.greatmancode.craftconomy3.Common
import com.greatmancode.craftconomy3.account.AccountManager

class CraftConomyHook(
    private val liteEco: LiteEco
) : HookListener(
    PLUGIN_NAME,
    "You can now export economy from plugin CraftConomy3 to LiteEco with /eco database import CraftConomy3 <your_currency>"
) {
    private lateinit var economyHandler: AccountManager

    companion object {
        const val PLUGIN_NAME = "CraftConomy3"
        fun isCraftEconomy(): Boolean
                = ClassUtil.isValidClasspath("com.greatmancode.craftconomy3.Common")
    }

    override fun canRegister(): Boolean {
        return !registered && liteEco.pluginManager.isPluginEnabled(BetterEconomyHook.Companion.PLUGIN_NAME)
    }

    override fun register() {
        if (isCraftEconomy()) {
            economyHandler = Common.getInstance().accountManager
        }
        registered = true
    }

    override fun unregister() {}

    fun getBalance(name: String, currency: String = "Dollar"): Double {
        if (!isBetterEconomy())
            throw Exception("Plugin BetterEconomy missing !")
        return economyHandler.getAccount(name, false).getBalance(null, currency)
    }
}