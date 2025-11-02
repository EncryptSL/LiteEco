package com.github.encryptsl.lite.eco.common.hook.economy.bettereconomy

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.common.hook.HookListener
import com.github.encryptsl.lite.eco.utils.ClassUtil
import me.hsgamer.bettereconomy.BetterEconomy
import me.hsgamer.bettereconomy.holder.EconomyHolder
import java.util.*

class BetterEconomyHook(
    private val liteEco: LiteEco
) : HookListener(
    PLUGIN_NAME,
    "You can now export economy from plugin BetterEconomy to LiteEco with /eco database import BetterEconomy <your_currency>"
) {
    private lateinit var economyHandler: EconomyHolder

    companion object {
        const val PLUGIN_NAME = "BetterEconomy"
        fun isBetterEconomyPresent(): Boolean
            = ClassUtil.isValidClasspath("me.hsgamer.bettereconomy.BetterEconomy")
    }

    override fun canRegister(): Boolean {
        val plugin = liteEco.pluginManager.getPlugin(PLUGIN_NAME)
        return !registered && plugin != null && isBetterEconomyPresent()
    }

    override fun register() {
        if (isBetterEconomyPresent()) {
            registered = true
        }
    }

    override fun unregister() {}

    fun getBalance(uuid: UUID): Double {
        return if (isBetterEconomyPresent()) {
            val plugin = liteEco.pluginManager.getPlugin(PLUGIN_NAME) as? BetterEconomy
            if (plugin == null) {
                liteEco.logger.warning("$PLUGIN_NAME plugin not found or not of expected type.")
                return 0.0
            }
            val handler = plugin.get(EconomyHolder::class.java)
            economyHandler = handler
            economyHandler.get(uuid)
        } else 0.00
    }
}