package encryptsl.cekuj.net.api

import encryptsl.cekuj.net.LiteEco
import encryptsl.cekuj.net.api.interfaces.ConfigAPIProvider
import java.io.File

/**
 * @author EncryptSL(Patrik Kolařík)
 * This is part of Config API from ForgeCore by EncryptSL
 * This componentAPI is for Loading custom config.
 * Called static in mainMethod.
 */
class ConfigAPI(private val liteEco: LiteEco) : ConfigAPIProvider {
    override fun create(configName: String) : ConfigAPI {
        val file = File(liteEco.dataFolder, configName)
        if (!file.exists()) {
            liteEco.saveResource(configName, false)
        } else {
            liteEco.getLogger().info("Configuration $configName exist !")
        }
        return this
    }

    override fun createConfig(configName: String, version: String): ConfigAPI {
        val file = File(liteEco.dataFolder, configName)
        if (!file.exists()) {
            liteEco.saveResource(configName, false)
            liteEco.logger.info("Configuration $configName was successfully created !")
        } else {
            val copyIfRequired = liteEco.config.getString("version").equals(version)
            val versionIsNull = liteEco.config.getString("version").isNullOrEmpty()
            if (!copyIfRequired || versionIsNull) {
                file.copyTo(File(liteEco.dataFolder, "old_config.yml"), true)
                liteEco.saveResource(configName, true)
                liteEco.config.set("version", version)
                liteEco.saveConfig()
                liteEco.getLogger().info("Configuration $configName is updated !")
            } else {
                liteEco.getLogger().info("Configuration $configName is latest !")
            }
        }

        return this
    }
}