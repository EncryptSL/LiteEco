package com.github.encryptsl.lite.eco.common.hook.economy.simpleeconomy

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.common.hook.HookListener
import com.github.encryptsl.lite.eco.utils.ClassUtil
import it.alzy.simpleeconomy.plugin.SimpleEconomy
import it.alzy.simpleeconomy.plugin.storage.Storage

class SimpleEconomyHook(private val liteEco: LiteEco) : HookListener(
    PLUGIN_NAME,
    "You can now export economy from plugin SimpleEconomy to LiteEco with /eco database import SimpleEconomy <into_currency>"
) {

    private val economyHandler: Storage?
        get() = if (isSimpleEconomyPresent()) SimpleEconomy.getInstance().storage else null

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
        registered = (economyHandler != null)
    }

    override fun unregister() {}

    fun getAccounts(): MutableMap<String, Double> {
        return if (isSimpleEconomyPresent()) {
            economyHandler?.allBalances ?: mutableMapOf()
        } else mutableMapOf()
    }
}