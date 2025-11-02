package com.github.encryptsl.lite.eco.common.hook.economy.simpleeconomy

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.common.hook.HookListener
import com.github.encryptsl.lite.eco.utils.ClassUtil
import it.alzy.simpleeconomy.plugin.SimpleEconomy
import it.alzy.simpleeconomy.plugin.storage.Storage
import java.util.*
import java.util.concurrent.CompletableFuture

class SimpleEconomyHook(private val liteEco: LiteEco) : HookListener(
    PLUGIN_NAME,
    "You can now export economy from plugin SimpleEconomy to LiteEco with /eco database import SimpleEconomy <your_currency>"
) {

    private lateinit var economyHandler: Storage

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

    fun getBalance(uuid: UUID): CompletableFuture<Double> {
        return if (isSimpleEconomyPresent()) {
            val instance = SimpleEconomy.getInstance()
            economyHandler = instance.storage
            economyHandler.getBalance(uuid)
        } else CompletableFuture.completedFuture(0.00)
    }

    fun getAccounts(): MutableMap<String, Double> {
        return if (isSimpleEconomyPresent()) {
            val instance = SimpleEconomy.getInstance()
            economyHandler = instance.storage
            economyHandler.allBalances
        } else mutableMapOf()
    }
}