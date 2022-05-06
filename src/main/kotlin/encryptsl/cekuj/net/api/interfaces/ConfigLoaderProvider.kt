package encryptsl.cekuj.net.api.interfaces

import encryptsl.cekuj.net.api.ConfigLoaderAPI

interface ConfigLoaderProvider {
    fun setConfigName(configName: String): ConfigLoaderAPI
    fun load(): ConfigLoaderAPI
    fun loadFromPlugin()
    fun checkDependency(pluginName: String): ConfigLoaderAPI
}