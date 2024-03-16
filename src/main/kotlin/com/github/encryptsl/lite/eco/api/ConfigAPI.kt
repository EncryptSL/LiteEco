package com.github.encryptsl.lite.eco.api

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.interfaces.ConfigAPIProvider
import java.io.File

/**
 * @author EncryptSL(Patrik Kolařík)
 * This is part of Config API from ForgeCore by EncryptSL
 * This componentAPI is for Loading custom config.
 * Called static in mainMethod.
 */
class ConfigAPI(private val liteEco: LiteEco) : ConfigAPIProvider {
    override fun create(fileName: String) : com.github.encryptsl.lite.eco.api.ConfigAPI {
        val file = File(liteEco.dataFolder, fileName)
        if (!file.exists()) {
            liteEco.saveResource(fileName, false)
        } else {
            liteEco.logger.info("Resource $fileName exists [!]")
        }
        return this
    }

    override fun createConfig(configName: String, version: String): com.github.encryptsl.lite.eco.api.ConfigAPI {
        val file = File(liteEco.dataFolder, configName)
        if (!file.exists()) {
            liteEco.saveResource(configName, false)
            liteEco.logger.info("Configuration $configName was successfully created !")
        } else {
            val fileVersion = liteEco.config.getString("version")

            if (fileVersion.isNullOrEmpty() || fileVersion != version) {
                file.copyTo(File(liteEco.dataFolder, "old_$configName"), true)
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