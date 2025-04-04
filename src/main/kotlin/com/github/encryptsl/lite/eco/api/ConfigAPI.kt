package com.github.encryptsl.lite.eco.api

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.interfaces.ConfigAPIProvider
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

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
            saveResource(fileName, false)
        } else {
            liteEco.logger.info("Resource $fileName exists [!]")
        }
        return this
    }

    override fun createConfig(configName: String, version: String): ConfigAPI {
        val file = File(liteEco.dataFolder, configName)
        if (!file.exists()) {
            saveResource(configName, false)
            liteEco.logger.info("Configuration $configName was successfully created !")
        }
        return this
    }

    private fun saveResource(configName: String, replace: Boolean = true) {
        try {
            liteEco.saveResource(configName, replace)
        } catch (e : Exception) {
            liteEco.logger.severe(e.message ?: e.localizedMessage)
        }
    }
}