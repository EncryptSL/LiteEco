package com.github.encryptsl.lite.eco.common.hook.scruffyboy13

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.common.hook.HookListener
import com.github.encryptsl.lite.eco.common.hook.bettereconomy.BetterEconomyHook.Companion.isBetterEconomy
import com.github.encryptsl.lite.eco.common.hook.placeholderapi.PlaceholderAPIHook
import com.github.encryptsl.lite.eco.utils.ClassUtil
import me.scruffyboy13.Economy.EconomyMain
import me.scruffyboy13.Economy.eco.Economy
import java.util.UUID

class ScruffyboyEconomyHook(
    private val liteEco: LiteEco
) : HookListener(
    PLUGIN_NAME,
    "You can now export economy from plugin Economy to LiteEco with /eco database import ScruffyboyEconomy <your_currency>"
) {

    private lateinit var economy: Economy

    companion object {
        const val PLUGIN_NAME = "Economy"
        fun isScruffyboyEconomy(): Boolean
                = ClassUtil.isValidClasspath("me.scruffyboy13.Economy.EconomyMain")
    }

    override fun canRegister(): Boolean {
        return !registered && liteEco.pluginManager.isPluginEnabled(PlaceholderAPIHook.PLUGIN_NAME)
    }

    override fun register() {
        if (isBetterEconomy()) {
            economy = EconomyMain.getEco()
        }
        registered = true
    }

    override fun unregister() {}

    fun getBalance(uuid: UUID): Double {
        if (!isScruffyboyEconomy())
            throw Exception("Plugin $PLUGIN_NAME missing !")
        return economy.getBalance(uuid).balance
    }


}