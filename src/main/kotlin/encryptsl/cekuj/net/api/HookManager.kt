package encryptsl.cekuj.net.api

import encryptsl.cekuj.net.LiteEco
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.ServicePriority

class HookManager(private val liteEco: LiteEco) {

    /**
     * Method for disable plugin if is detected unsupported plugin.
     * @param pluginName - String name of plugin is CaseSensitive.
     */
    fun blockPlugin(pluginName: String) {
        if (liteEco.pluginManger.isPluginEnabled(pluginName)) {
            liteEco.logger.severe("Please don't use $pluginName, because there can be conflict.")
            liteEco.pluginManger.disablePlugin(liteEco)
        }
    }

    /**
     * Method for check if plugin is enabled or disabled
     * @param pluginName - String name of plugin is CaseSensitive
     * @return Boolean
     */
    fun isPluginEnabled(pluginName: String): Boolean {
        return liteEco.pluginManger.isPluginEnabled(pluginName)
    }

    /**
     * Method of registering Placeholders if plugin PlaceholderAPI is enabled.
     */
    fun hookPAPI() {
        if (isPluginEnabled("PlaceholderAPI")) {
            liteEco.logger.info("PlaceholderAPI hook initialized")
            PlaceHolderExtensionProvider(liteEco).register()
        } else {
            liteEco.logger.info("PlaceholderAPI hook not found")
        }
    }

    /**
     * ServiceRegister is method for registering services.
     * @param pluginName - String name of plugin is CaseSensitive
     * @param service - Is represented like a interface of provider.
     * @param provider - Is class implemented service.
     * @param plugin - Is instance of LiteEco plugin
     * @param priority - Is priority of the registering.
     * @return Boolean
     */
    fun <T : Any> serviceRegister(pluginName: String, service: Class<T>, provider: T, plugin: Plugin, priority: ServicePriority): Boolean {
        return if (isPluginEnabled(pluginName)) {
            liteEco.server.servicesManager.register(service, provider, plugin, priority)
            liteEco.logger.info("Registered $pluginName like a service.")
            true
        } else {
            liteEco.logger.info("$pluginName not found. Please download $pluginName to use LiteEco")
            false
        }
    }
}