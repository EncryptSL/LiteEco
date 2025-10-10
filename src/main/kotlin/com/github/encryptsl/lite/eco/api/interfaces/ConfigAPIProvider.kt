package com.github.encryptsl.lite.eco.api.interfaces

/**
 * Interface defining a provider or factory for creating instances of the Configuration API.
 *
 * This provider abstracts the process of initializing and loading configuration files,
 * ensuring the application gets a ready-to-use [com.github.encryptsl.lite.eco.api.ConfigAPI] instance.
 */
interface ConfigAPIProvider {

    /**
     * Creates and loads a configuration API instance for a specified file name.
     *
     * This method is a general utility for initializing configurations, regardless of the file extension.
     *
     * @param fileName The name of the configuration file to create/load (e.g., "messages.yml" or "settings.json").
     * @return An initialized instance of [com.github.encryptsl.lite.eco.api.ConfigAPI].
     */
    fun create(fileName: String): com.github.encryptsl.lite.eco.api.ConfigAPI

    /**
     * Creates and loads the main configuration file, specifically targeting "config.yml".
     *
     * This method serves as a dedicated entry point for the primary configuration.
     *
     * @param configName The name of the configuration file (e.g., "config.yml").
     * @return An initialized instance of [com.github.encryptsl.lite.eco.api.ConfigAPI].
     */
    fun createConfig(configName: String): com.github.encryptsl.lite.eco.api.ConfigAPI
}