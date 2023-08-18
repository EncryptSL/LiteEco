package encryptsl.cekuj.net.hook

import encryptsl.cekuj.net.LiteEco
import encryptsl.cekuj.net.hook.treasury.TreasureCurrency
import encryptsl.cekuj.net.hook.treasury.TreasuryEconomyAPI
import encryptsl.cekuj.net.hook.vault.AdaptiveEconomyVaultAPI
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
            liteEco.logger.info("###################################")
            liteEco.logger.info("#       PlaceholderAPI Found      #")
            liteEco.logger.info("#   Please download from ecloud   #")
            liteEco.logger.info("#  /papi ecloud download LiteEco  #")
            liteEco.logger.info("###################################")
        } else {
            liteEco.logger.info("###################################")
            liteEco.logger.info("#     PlaceholderAPI not Found    #")
            liteEco.logger.info("###################################")
        }
    }

    fun hookVault() {
        if (isPluginInstalled("Vault")) {
            liteEco.server.servicesManager.register(Economy::class.java, AdaptiveEconomyVaultAPI(liteEco), liteEco, ServicePriority.Highest)
            liteEco.logger.info("###################################")
            liteEco.logger.info("# Vault registered like a service #")
            liteEco.logger.info("###################################")
        } else {
            liteEco.logger.info("###################################")
            liteEco.logger.info("#         Vault not Found         #")
            liteEco.logger.info("#   For better experience please  #")
            liteEco.logger.info("#    download vault or treasury   #")
            liteEco.logger.info("###################################")
        }
    }

    fun hookTreasury() {
        if (isPluginInstalled("Treasury")) {
            ServiceRegistry.INSTANCE.registerService(EconomyProvider::class.java, TreasuryEconomyAPI(liteEco, TreasureCurrency(liteEco)), "LiteEco", me.lokka30.treasury.api.common.service.ServicePriority.HIGH)
            liteEco.logger.info("######################################")
            liteEco.logger.info("# Treasury registered like a service #")
            liteEco.logger.info("######################################")
        } else {
            liteEco.logger.info("###################################")
            liteEco.logger.info("#       Treasury not Found        #")
            liteEco.logger.info("#   For better experience please  #")
            liteEco.logger.info("#    download treasury or vault   #")
            liteEco.logger.info("###################################")
        }
    }
}