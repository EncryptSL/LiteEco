package com.github.encryptsl.lite.eco.common.hook.economy.scruffyboy13

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.common.hook.HookListener
import com.github.encryptsl.lite.eco.utils.ClassUtil
import me.scruffyboy13.Economy.EconomyMain
import me.scruffyboy13.Economy.eco.Economy
import java.util.*

class ScruffyboyEconomyHook(
    private val liteEco: LiteEco
) : HookListener(
    PLUGIN_NAME,
    "You can now export economy from plugin Economy to LiteEco with /eco database import ScruffyboyEconomy <your_currency>"
) {
    private val economyHandler: Economy?
        get() = if (isScruffyBoyEconomyPresent()) EconomyMain.getEco() else null

    companion object {
        const val PLUGIN_NAME = "Economy"
        fun isScruffyBoyEconomyPresent(): Boolean
                = ClassUtil.isValidClasspath("me.scruffyboy13.Economy.EconomyMain")
    }

    override fun canRegister(): Boolean {
        val plugin = liteEco.pluginManager.getPlugin(PLUGIN_NAME)
        return !registered && plugin != null && isScruffyBoyEconomyPresent()
    }

    override fun register() {
        registered = (economyHandler != null)
    }

    override fun unregister() {}

    fun getBalance(uuid: UUID): Double {
        return if (isScruffyBoyEconomyPresent()) {
            economyHandler?.getBalance(uuid)?.balance ?: 0.00
        } else 0.00
    }

}