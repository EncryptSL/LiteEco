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
    private val economyHandler: Map<UUID, PlayerAccount>?
        get() = if (isTheosisEconomyPresent()) {
            val instance = liteEco.pluginManager.getPlugin(PLUGIN_NAME) as? TheosisEconomy
            instance?.playerAccounts
        } else null

    companion object {
        const val PLUGIN_NAME = "TheosisEconomy"
        fun isTheosisEconomyPresent(): Boolean = ClassUtil.isValidClasspath("me.Short.TheosisEconomy.TheosisEconomy")
    }

    override fun canRegister(): Boolean {
        val plugin = liteEco.pluginManager.getPlugin(PLUGIN_NAME)
        return !registered && plugin != null && isTheosisEconomyPresent()
    }

    override fun register() {
        registered = (economyHandler != null)
    }

    override fun unregister() {}

    fun getBalance(uuid: UUID): BigDecimal {
        return economyHandler?.get(uuid)?.balance ?: BigDecimal.ZERO
    }
}