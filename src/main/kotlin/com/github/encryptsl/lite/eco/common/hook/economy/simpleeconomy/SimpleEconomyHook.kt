package com.github.encryptsl.lite.eco.common.hook.economy.simpleeconomy

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.common.hook.HookListener
import com.github.encryptsl.lite.eco.utils.ClassUtil
import it.alzy.simpleeconomy.plugin.SimpleEconomy
import it.alzy.simpleeconomy.plugin.storage.Storage

class SimpleEconomyHook(private val liteEco: LiteEco) : HookListener(
    PLUGIN_NAME,
    "You can now export economy from plugin SimpleEconomy to LiteEco with /eco database import SimpleEconomy <your_currency>"
) {

    private var economyHandler: Storage? = null

    companion object {
        const val PLUGIN_NAME = "SimpleEconomy"

        fun isSimpleEconomyPresent(): Boolean {
            return ClassUtil.isValidClasspath("it.alzy.simpleeconomy.plugin.SimpleEconomy")
        }
    }

    override fun canRegister(): Boolean {
        val plugin = liteEco.pluginManager.getPlugin(PLUGIN_NAME)
        return !registered && plugin != null && isSimpleEconomyPresent()
    }

    override fun register() {
        if (isSimpleEconomyPresent()) {
            registered = true
        }
    }

    override fun unregister() {}

    fun getAccounts(): MutableMap<String, Double> {
        return if (isSimpleEconomyPresent()) {
            economyHandler = SimpleEconomy.getInstance().storage
            economyHandler?.allBalances ?: mutableMapOf()
        } else mutableMapOf()
    }
}