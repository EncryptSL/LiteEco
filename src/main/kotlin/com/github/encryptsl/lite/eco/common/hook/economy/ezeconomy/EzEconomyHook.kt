package com.github.encryptsl.lite.eco.common.hook.economy.ezeconomy

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.common.hook.HookListener
import com.github.encryptsl.lite.eco.utils.ClassUtil
import com.skyblockexp.ezeconomy.api.EzEconomyAPI
import com.skyblockexp.ezeconomy.core.EzEconomyPlugin
import java.util.*

class EzEconomyHook(
    private val liteEco: LiteEco
) : HookListener(
    PLUGIN_NAME,
    "You can now export economy from plugin EzEconomy to LiteEco with /eco database import EzEconomy <into_currency> [--from <currency>]"
) {

    private val economyHandler: EzEconomyAPI?
        get() = if (isEzEconomyPresent()) {
            val instance = liteEco.pluginManager.getPlugin(PLUGIN_NAME) as? EzEconomyPlugin
            val storage = instance?.storage
            EzEconomyAPI(storage)
        } else null

    companion object {
        const val PLUGIN_NAME = "EzEconomy"
        fun isEzEconomyPresent(): Boolean
                = ClassUtil.isValidClasspath("com.skyblockexp.ezeconomy.core.EzEconomyPlugin")
    }

    override fun canRegister(): Boolean {
        val plugin = liteEco.pluginManager.getPlugin(PLUGIN_NAME)
        return !registered && plugin != null && isEzEconomyPresent()
    }

    override fun register() {
        registered = (economyHandler != null)
    }

    override fun unregister() {}

    fun getBalance(uuid: UUID, currency: String?): Double {
        return try {
            economyHandler?.getBalance(uuid, currency ?: economyHandler?.defaultCurrency)?.balance ?: 0.0
        } catch (e: Exception) {
            liteEco.componentLogger.warn("Failed to get ${PLUGIN_NAME} balance for $uuid: ${e.message}")
            0.0
        }
    }
}