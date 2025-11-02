package com.github.encryptsl.lite.eco.common.hook.economy.theosiseconomy

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.common.hook.HookListener
import com.github.encryptsl.lite.eco.utils.ClassUtil
import me.Short.TheosisEconomy.PlayerAccount
import me.Short.TheosisEconomy.TheosisEconomy
import java.math.BigDecimal
import java.util.*

class TheosisEconomyHook(private val liteEco: LiteEco) : HookListener(
    PLUGIN_NAME,
    "You can now export economy from plugin TheosisEconomy to LiteEco with /eco database import TheosisEconomy <your_currency>"
) {
    private var economyHandler: Map<UUID, PlayerAccount>? = null

    companion object {
        const val PLUGIN_NAME = "TheosisEconomy"
        fun isTheosisEconomyPresent(): Boolean = ClassUtil.isValidClasspath("me.Short.TheosisEconomy.TheosisEconomy")
    }

    override fun canRegister(): Boolean {
        val plugin = liteEco.pluginManager.getPlugin(PLUGIN_NAME)
        return !registered && plugin != null && isTheosisEconomyPresent()
    }

    override fun register() {
        if (isTheosisEconomyPresent()) {
            val instance = liteEco.pluginManager.getPlugin(PLUGIN_NAME) as? TheosisEconomy
            economyHandler = instance?.playerAccounts
            registered = true
        }
    }

    override fun unregister() {}

    fun getBalance(uuid: UUID): BigDecimal {
        val handler = economyHandler ?: throw IllegalStateException("$PLUGIN_NAME handler is not initialized.")
        return handler[uuid]?.balance ?: BigDecimal.ZERO
    }
}