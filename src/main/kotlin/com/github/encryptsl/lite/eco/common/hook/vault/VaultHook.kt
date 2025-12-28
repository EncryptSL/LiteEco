@file:Suppress("DEPRECATION")
package com.github.encryptsl.lite.eco.common.hook.vault

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.common.hook.HookListener
import com.github.encryptsl.lite.eco.common.hook.vault.legacy.LegacyEconomyVaultAPI
import com.github.encryptsl.lite.eco.common.hook.vault.unlocked.AdaptiveEconomyVaultUnlockedAPI
import com.github.encryptsl.lite.eco.utils.ClassUtil
import net.milkbowl.vault.economy.Economy
import org.bukkit.plugin.ServicePriority

@Suppress("DEPRECATION")
class VaultHook(
    val liteEco: LiteEco
) : HookListener(
    PLUGIN_NAME,
    "You can now use Vault API !"
) {

    companion object {
        const val PLUGIN_NAME = "Vault"

        fun isVaultUnlocked(): Boolean
            = ClassUtil.isValidClasspath("net.milkbowl.vault2.economy.Economy")
    }


    override fun canRegister(): Boolean {
        return !registered && liteEco.pluginManager.getPlugin(PLUGIN_NAME) != null
    }

    override fun register() {
        if (isVaultUnlocked()) {
            liteEco.server.servicesManager.register(net.milkbowl.vault2.economy.Economy::class.java, AdaptiveEconomyVaultUnlockedAPI(liteEco), liteEco, ServicePriority.Highest)
            liteEco.componentLogger.info("You can now use modern VaultUnlocked API.")
        }

        liteEco.server.servicesManager.register(Economy::class.java,
            LegacyEconomyVaultAPI(liteEco), liteEco, ServicePriority.Highest)
        registered = true
    }

    override fun unregister() {
        liteEco.server.servicesManager.unregister(LegacyEconomyVaultAPI::class.java)

        if (isVaultUnlocked()) {
            liteEco.server.servicesManager.unregister(AdaptiveEconomyVaultUnlockedAPI::class.java)
        }
    }
}