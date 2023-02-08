package encryptsl.cekuj.net.api

import encryptsl.cekuj.net.LiteEco
import encryptsl.cekuj.net.api.interfaces.ConfigLoaderProvider
import java.io.File

/**
 * @author EncryptSL(Patrik Kolařík)
 * This is part of Config API from ForgeCore by EncryptSL
 * This componentAPI is for Loading custom config.
 * Called static in mainMethod.
 */
class ConfigLoaderAPI(private val liteEco: LiteEco) : ConfigLoaderProvider {
    override fun create(configName: String) : ConfigLoaderAPI {
        val file = File(liteEco.dataFolder, configName)
        if (!file.exists()) {
            liteEco.saveResource(configName, false)
        } else {
            liteEco.logger.info("Configuration $configName exist !")
        }
        return this
    }

    override fun createConfig(configName: String, version: String): ConfigLoaderAPI {
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
                liteEco.logger.info("Configuration $configName is updated !")
            } else {
                liteEco.logger.info("Configuration $configName is latest !")
            }
        }

        return this
    }
}