package com.github.encryptsl.lite.eco.common.hook.theosiseconomy

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.common.hook.HookListener
import com.github.encryptsl.lite.eco.utils.ClassUtil
import me.Short.TheosisEconomy.PlayerAccount
import me.Short.TheosisEconomy.TheosisEconomy
import java.math.BigDecimal
import java.util.UUID

class TheosisEconomyHook(private val liteEco: LiteEco) : HookListener(
    PLUGIN_NAME,
    "You can now export economy from plugin TheosisEconomy to LiteEco with /eco database import TheosisEconomy <your_currency>"
) {
    private var economyHandler = mutableMapOf<UUID, PlayerAccount>()

    companion object {
        const val PLUGIN_NAME = "TheosisEconomy"
        fun isTheosisEconomy(): Boolean
                = ClassUtil.isValidClasspath("me.Short.TheosisEconomy.TheosisEconomy")
    }

    override fun canRegister(): Boolean {
        return !registered && liteEco.pluginManager.isPluginEnabled(PLUGIN_NAME)
    }

    override fun register() {
        if (isTheosisEconomy()) {
            economyHandler = TheosisEconomy().playerAccounts
        }
        registered = true
    }

    override fun unregister() {}

    fun getBalance(uuid: UUID) : BigDecimal {
        if (!isTheosisEconomy())
            throw Exception("Plugin ${PLUGIN_NAME} missing !")

        return economyHandler.get(uuid)?.balance ?: BigDecimal.ZERO
    }
}