package com.github.encryptsl.lite.eco.utils

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.objects.ModernText

class Debugger(private val liteEco: LiteEco) {

    fun <T> debug(provider: Class<T>, message: String) {
        if (liteEco.config.getBoolean("plugin.vault-debug")) {
            liteEco.componentLogger.info(ModernText.miniModernText("<gold> ${provider.name} $message"))
        }
    }

}