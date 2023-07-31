package encryptsl.cekuj.net.api.interfaces

import encryptsl.cekuj.net.api.ConfigAPI

interface ConfigAPIProvider {
    fun create(fileName: String): ConfigAPI

    /**
     * Create config.yml
     * @param fileName - File name example config.yml
     * @param version - Version of revision of config need changed.
     * @return ConfigLoaderAPI
     */
    fun createConfig(fileName: String, version: String): ConfigAPI
}