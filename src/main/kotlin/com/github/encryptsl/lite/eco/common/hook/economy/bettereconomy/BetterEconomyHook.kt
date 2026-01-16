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
    private val economyHandler: EconomyHolder?
        get() = (if (isBetterEconomyPresent()) {
            val instance = liteEco.pluginManager.getPlugin(PLUGIN_NAME) as? BetterEconomy
            instance?.get(EconomyHolder::class.java)
        } else null)

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
        registered = (economyHandler != null)
    }

    override fun unregister() {}

    fun getBalance(uuid: UUID): Double {
        return try {
            economyHandler?.get(uuid) ?: 0.0
        } catch (e: Exception) {
            liteEco.componentLogger.warn("Failed to get $PLUGIN_NAME balance for $uuid: ${e.message}")
            0.0
        }
    }
}