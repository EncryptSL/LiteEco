package encryptsl.cekuj.net.api.interfaces

import encryptsl.cekuj.net.api.ConfigLoaderAPI

interface ConfigLoaderProvider {
    fun create(configName: String): ConfigLoaderAPI
    fun checkDependency(pluginName: String): ConfigLoaderAPI
}