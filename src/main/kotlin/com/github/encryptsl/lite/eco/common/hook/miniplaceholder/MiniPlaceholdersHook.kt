package com.github.encryptsl.lite.eco.common.hook.miniplaceholder

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.common.hook.HookListener

class MiniPlaceholdersHook(
    val liteEco: LiteEco
) : HookListener(
    PLUGIN_NAME,
    "You can now use placeholders from https://github.com/EncryptSL/LiteEco/wiki/Placeholders#mini-available-placeholders"
) {
    companion object {
        const val PLUGIN_NAME = "MiniPlaceholders"
    }

    override fun canRegister(): Boolean {
        return !registered && liteEco.pluginManager.getPlugin(PLUGIN_NAME) != null
    }

    override fun register() {
        EconomyMiniPlaceholder(liteEco).register()

        registered = true
    }

    override fun unregister() {}
}