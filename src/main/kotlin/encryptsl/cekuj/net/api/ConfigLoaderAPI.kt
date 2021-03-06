package encryptsl.cekuj.net.api

import encryptsl.cekuj.net.LiteEco
import encryptsl.cekuj.net.api.interfaces.ConfigLoaderProvider
import org.bukkit.Bukkit
import java.io.File

/**
 * @author EncryptSL(Patrik Kolařík)
 * This is part of Config API from ForgeCore by EncryptSL
 * This componentAPI is for Loading custom config.
 * Called static in mainMethod.
 */
class ConfigLoaderAPI(private val liteEco: LiteEco) : ConfigLoaderProvider {
    private var configName: String = ""
    override fun setConfigName(configName: String): ConfigLoaderAPI {
        this.configName = configName
        return this
    }

    override fun load() : ConfigLoaderAPI {
        val file = File(liteEco.dataFolder, this.configName)
        if (!file.exists()) {
            liteEco.saveResource(configName, false)
        } else {
            liteEco.logger.info("Configuration $configName exist !")
        }
        return this
    }
    override fun checkDependency(pluginName: String): ConfigLoaderAPI {
        if (Bukkit.getPluginManager().getPlugin(pluginName) == null) {
            liteEco.logger.info("Plugin $pluginName not found")
            Bukkit.getPluginManager().disablePlugin(liteEco)
        }
        return this
    }
}