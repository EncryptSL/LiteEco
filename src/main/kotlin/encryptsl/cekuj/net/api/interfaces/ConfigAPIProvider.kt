package encryptsl.cekuj.net.api.interfaces

import encryptsl.cekuj.net.api.ConfigAPI

interface ConfigAPIProvider {
    fun create(fileName: String): ConfigAPI

    /**
     * Create config.yml
     * @param configName - Config name example config.yml
     * @param version - Version of revision of config need changed.
     * @return ConfigLoaderAPI
     */
    fun createConfig(configName: String, version: String): ConfigAPI
}