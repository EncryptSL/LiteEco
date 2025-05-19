package com.github.encryptsl.lite.eco.common.hook.bettereconomy

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.common.hook.HookListener
import com.github.encryptsl.lite.eco.common.hook.placeholderapi.PlaceholderAPIHook
import com.github.encryptsl.lite.eco.utils.ClassUtil
import me.hsgamer.bettereconomy.BetterEconomy
import me.hsgamer.bettereconomy.api.EconomyHandler
import me.hsgamer.bettereconomy.provider.EconomyHandlerProvider
import java.util.*

class BetterEconomyHook(
    private val liteEco: LiteEco
) : HookListener(
    PLUGIN_NAME,
    "You can now export economy from plugin BetterEconomy to LiteEco with /eco database import BetterEconomy <your_currency>"
) {
    private lateinit var economyHandler: EconomyHandler

    companion object {
        const val PLUGIN_NAME = "BetterEconomy"
        fun isBetterEconomy(): Boolean
            = ClassUtil.isValidClasspath("me.hsgamer.bettereconomy.BetterEconomy")
    }

    override fun canRegister(): Boolean {
        return !registered && liteEco.pluginManager.isPluginEnabled(PLUGIN_NAME)
    }

    override fun register() {
        if (isBetterEconomy()) {
            economyHandler = BetterEconomy().get(EconomyHandlerProvider::class.java).economyHandler
        }
        registered = true
    }

    override fun unregister() {}

    fun getBalance(uuid: UUID): Double {
        if (!isBetterEconomy())
            throw Exception("Plugin BetterEconomy missing !")
        return economyHandler.get(uuid)
    }
}