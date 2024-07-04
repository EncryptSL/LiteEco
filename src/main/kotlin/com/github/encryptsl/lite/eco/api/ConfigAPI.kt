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
            liteEco.saveResource(fileName, false)
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
        } else {
            val fileVersion = liteEco.config.getString("version")
            if (fileVersion.isNullOrEmpty() || fileVersion != version) {
                copyOldConfig(file, configName)
                liteEco.config.options().parseComments(true)
                liteEco.config.options().copyDefaults(true)
                liteEco.saveConfig()
            } else {
                liteEco.logger.info("Configuration config.yml is the latest [!]")
            }
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

    private fun copyOldConfig(file: File, configName: String) {
        Files.copy(file.toPath(), File(liteEco.dataFolder, "old_$configName").toPath(), StandardCopyOption.REPLACE_EXISTING)
        liteEco.logger.info("Configuration config.yml was outdated and copied into old_config. [!]")
    }
}