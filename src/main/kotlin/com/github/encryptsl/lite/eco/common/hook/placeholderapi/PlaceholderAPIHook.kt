package com.github.encryptsl.lite.eco.common.hook.placeholderapi

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.common.hook.HookListener

class PlaceholderAPIHook(
    private val liteEco: LiteEco
) : HookListener(
    PLUGIN_NAME,
    "You can now use placeholders from https://github.com/EncryptSL/LiteEco/wiki/Placeholders#papi-available-placeholders"
) {
    companion object {
        const val PLUGIN_NAME = "PlaceholderAPI"
    }

    override fun canRegister(): Boolean {
        return !registered && liteEco.pluginManager.getPlugin(PLUGIN_NAME) != null
    }

    override fun register() {
        EconomyPlaceholderAPI(liteEco, LiteEco.PAPI_VERSION).register()

        registered = true
    }

    override fun unregister() {
        EconomyPlaceholderAPI(liteEco, LiteEco.PAPI_VERSION).unregister()
    }
}