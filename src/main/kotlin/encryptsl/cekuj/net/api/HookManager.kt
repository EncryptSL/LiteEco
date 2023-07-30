package encryptsl.cekuj.net.api

import encryptsl.cekuj.net.LiteEco
import encryptsl.cekuj.net.api.economy.treasury.TreasureCurrency
import encryptsl.cekuj.net.api.economy.treasury.TreasuryEconomyAPI
import encryptsl.cekuj.net.api.economy.vault.AdaptiveEconomyVaultAPI
import me.lokka30.treasury.api.common.service.ServiceRegistry
import me.lokka30.treasury.api.economy.EconomyProvider
import net.milkbowl.vault.economy.Economy
import org.bukkit.plugin.ServicePriority

class HookManager(private val liteEco: LiteEco) {

    /**
     * Method for disable plugin if is detected unsupported plugin.
     * @param pluginName - String name of plugin is CaseSensitive.
     */
    fun blockPlugin(pluginName: String) {
        if (liteEco.pluginManager.isPluginEnabled(pluginName)) {
            liteEco.logger.severe("Please don't use $pluginName, because there can be conflict.")
            liteEco.pluginManager.disablePlugin(liteEco)
        }
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
            liteEco.logger.info("PlaceholderAPI hook initialized")
            PlaceHolderExtensionProvider(liteEco).register()
        } else {
            liteEco.logger.info("PlaceholderAPI hook not found")
        }
    }

    fun hookVault() {
        if (isPluginInstalled("Vault")) {
            liteEco.server.servicesManager.register(Economy::class.java, AdaptiveEconomyVaultAPI(liteEco), liteEco, ServicePriority.Highest)
            liteEco.logger.info("Registered Vault like a service.")
        } else {
            liteEco.logger.info("Vault not found, for better experience please download Vault or Treasury.")
        }
    }

    fun hookTreasury() {
        if (isPluginInstalled("Treasury")) {
            ServiceRegistry.INSTANCE.registerService(EconomyProvider::class.java, TreasuryEconomyAPI(liteEco, TreasureCurrency(liteEco)), "LiteEco", me.lokka30.treasury.api.common.service.ServicePriority.HIGH)
            liteEco.logger.info("Registered Treasury like a service.")
        } else {
            liteEco.logger.info("Treasury not found, for better experience please download Treasury or Vault.")
        }
    }
}