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
    override fun create(fileName: String) : ConfigAPI {
        val file = File(liteEco.dataFolder, fileName)
        if (!file.exists()) {
            liteEco.saveResource(fileName, false)
        } else {
            liteEco.logger.info("Configuration $fileName exist !")
        }
        return this
    }

    override fun createConfig(configName: String, version: String): ConfigAPI {
        val file = File(liteEco.dataFolder, configName)
        if (!file.exists()) {
            liteEco.saveResource(configName, false)
            liteEco.logger.info("Configuration $configName was successfully created !")
        } else {
            val fileVersion = liteEco.config.getString("version")

            if (fileVersion.isNullOrEmpty() || fileVersion != version) {
                file.copyTo(File(liteEco.dataFolder, "old_config.yml"), true)
                liteEco.saveResource(configName, true)
                liteEco.config["version"] = version
                liteEco.saveConfig()
                liteEco.logger.info("Configuration config.yml was outdated [!]")
            } else {
                liteEco.logger.info("Configuration config.yml is the latest [!]")
            }
        }

        return this
    }
}