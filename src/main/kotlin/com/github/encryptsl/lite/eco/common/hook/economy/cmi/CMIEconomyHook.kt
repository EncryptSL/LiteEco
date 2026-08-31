package com.github.encryptsl.lite.eco.common.hook.economy.cmi

import com.Zrips.CMI.CMI
import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.common.hook.HookListener
import com.github.encryptsl.lite.eco.common.hook.economy.playerpoints.PlayerPointsHook
import com.github.encryptsl.lite.eco.utils.ClassUtil
import java.math.BigDecimal
import java.util.*

class CMIEconomyHook(
    private val liteEco: LiteEco
) : HookListener(
    "CMI",
    "Experimental: You can now export economy from plugin CMI to LiteEco with /eco database import CMI <into_currency>"
) {

    private val economyHandler: CMI?
        get() = (if (isCMIPresent()) {
            CMI.getInstance()
        } else null)

    companion object {
        const val PLUGIN_NAME = "CMI"
        fun isCMIPresent(): Boolean
                = ClassUtil.isValidClasspath("com.Zrips.CMI.CMI")
    }

    override fun canRegister(): Boolean {
        val plugin = liteEco.pluginManager.getPlugin(PLUGIN_NAME)
        return !registered && plugin != null && isCMIPresent()
    }

    override fun register() {
        registered = (economyHandler != null)
    }

    override fun unregister() {}

    fun getBalance(uuid: UUID): BigDecimal? {
        return try {
            economyHandler?.playerManager?.getUser(uuid)?.balance?.toBigDecimal() ?: BigDecimal.ZERO
        } catch (e: Exception) {
            liteEco.componentLogger.warn("Failed to get ${PlayerPointsHook.PLUGIN_NAME} balance for $uuid: ${e.message}")
            BigDecimal.ZERO
        }
    }

}