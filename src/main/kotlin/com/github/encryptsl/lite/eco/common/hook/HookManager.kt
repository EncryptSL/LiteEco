package com.github.encryptsl.lite.eco.common.hook

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.common.hook.bettereconomy.BetterEconomyHook
import com.github.encryptsl.lite.eco.common.hook.miniplaceholder.EconomyMiniPlaceholder
import com.github.encryptsl.lite.eco.common.hook.placeholderapi.EconomyPlaceholderAPI
import com.github.encryptsl.lite.eco.common.hook.treasury.TreasuryEconomyAPI
import com.github.encryptsl.lite.eco.common.hook.vault.AdaptiveEconomyVaultAPI
import com.github.encryptsl.lite.eco.common.hook.vault.unlocked.AdaptiveEconomyVaultUnlockedAPI
import me.lokka30.treasury.api.common.service.ServiceRegistry
import me.lokka30.treasury.api.economy.EconomyProvider
import net.milkbowl.vault.economy.Economy
import org.bukkit.plugin.ServicePriority

class HookManager(private val liteEco: LiteEco) {

    /**
     * Method for disable plugin if is detected unsupported plugin.
     * @param plugins
     */
    fun blockPlugin(vararg plugins: String) {
        for (plugin in plugins) {
            if (liteEco.pluginManager.isPluginEnabled(plugin)) {
                liteEco.logger.severe("Please don't use $plugin, because there can be conflict.")
                liteEco.pluginManager.disablePlugin(liteEco)
            }
        }
        liteEco.logger.info("Blocked plugins are now ${plugins.size}")
    }

    /**
     * Method for check if plugin is installed
     * @param pluginName - String name of plugin is CaseSensitive
     * @return Boolean
     */
    private fun isPluginInstalled(pluginName: String): Boolean {
        return liteEco.pluginManager.getPlugin(pluginName) != null
    }

    /**
     * Method of registering Placeholders if plugin PlaceholderAPI is enabled.
     */
    fun hookPAPI() {
        if (isPluginInstalled("PlaceholderAPI")) {
            EconomyPlaceholderAPI(liteEco, LiteEco.PAPI_VERSION).register()
            liteEco.logger.info("PlaceholderAPI found, placeholders are registered !")
        } else {
            liteEco.logger.warning("Warning plugin PlaceholderAPI not found !")
            liteEco.logger.warning("Keep in mind without PlaceholderAPI, you can't use LiteEco placeholders.")
        }
    }

    fun hookMiniPlaceholders() {
        if (isPluginInstalled("MiniPlaceholders")) {
            EconomyMiniPlaceholder(liteEco).register()
            liteEco.logger.info("MiniPlaceholders found, placeholders are registered !")
        } else {
            liteEco.logger.warning("Warning plugin MiniPlaceholders not found !")
            liteEco.logger.warning("Keep in mind without MiniPlaceholders, you can't use LiteEco placeholders.")
        }
    }

    fun hookVault() {
        if (isPluginInstalled("Vault")) {
            try {
                liteEco.server.servicesManager.register(Economy::class.java, AdaptiveEconomyVaultAPI(liteEco), liteEco, ServicePriority.Highest)
                liteEco.server.servicesManager.register(net.milkbowl.vault2.economy.Economy::class.java, AdaptiveEconomyVaultUnlockedAPI(liteEco), liteEco, ServicePriority.Highest)
                liteEco.logger.info("VaultUnlocked is registered, LiteEco now using v2 modern economy provider !")
            } catch (e : Exception) {
                liteEco.server.servicesManager.register(Economy::class.java, AdaptiveEconomyVaultAPI(liteEco), liteEco, ServicePriority.Highest)
                liteEco.logger.info("Vault is registered, LiteEco using old v1 implementation of vault api. !")
                liteEco.logger.info("For multi currencies support please download VaultUnlocked. !")
            }
        } else {
            liteEco.logger.warning("Warning plugin Vault or VaultUnlocked not found !")
            liteEco.logger.warning("For better experience please download Vault or VaultUnlocked.")
            liteEco.logger.warning("Keep in mind without Vault, LiteEco can't use API from VaultAPI.")
        }
    }

    fun hookBetterEconomy() {
        if (BetterEconomyHook().getBetterEconomy() || isPluginInstalled("BetterEconomy")) {
            liteEco.logger.info("Plugin BetterEconomy found.")
            liteEco.logger.info("You can now convert from BetterEconomy to LiteEco, with /eco convert BetterEconomy.")
        }
    }

    /**
     * This hook of treasury is heavy experimental.
     * Please keep in mind Treasury implementation in LiteEco is very limited.
     * LiteEco don't support banks accounts, and non player accounts.
     */
    fun hookTreasury() {
        if (isPluginInstalled("Treasury")) {
            ServiceRegistry.INSTANCE.registerService(
                EconomyProvider::class.java,
                TreasuryEconomyAPI(liteEco),
                "LiteEco",
                me.lokka30.treasury.api.common.service.ServicePriority.NORMAL
            )
            liteEco.logger.warning("TreasuryAPI support is very experimental..")
            liteEco.logger.info("LiteEco in version 1.4.6 and above supports TreasuryAPI.")
        } else {
            liteEco.logger.warning("Warning plugin Treasury not found !")
            liteEco.logger.warning("For better experience please download Treasury or Vault.")
            liteEco.logger.warning("Keep in mind without Treasury, LiteEco can't use API from Treasury.")
        }
    }
}