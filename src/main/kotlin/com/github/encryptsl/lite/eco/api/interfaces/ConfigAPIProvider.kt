package com.github.encryptsl.lite.eco.api.interfaces

interface ConfigAPIProvider {
    fun create(fileName: String): com.github.encryptsl.lite.eco.api.ConfigAPI

    /**
     * Create config.yml
     * @param configName - Config name example config.yml
     * @param version - Version of revision of config need changed.
     * @return ConfigLoaderAPI
     */
    fun createConfig(configName: String, version: String): com.github.encryptsl.lite.eco.api.ConfigAPI
}