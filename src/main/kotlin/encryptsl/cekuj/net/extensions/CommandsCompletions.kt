package encryptsl.cekuj.net.extensions

import co.aikar.commands.CommandCompletions
import encryptsl.cekuj.net.api.enums.TranslationKey
import org.bukkit.Bukkit

fun CommandCompletions<*>.registerOfflinePlayers() {
    this.registerAsyncCompletion("offlinePlayers") {
        return@registerAsyncCompletion Bukkit.getOfflinePlayers().map { offlinePlayer -> offlinePlayer.name }.toList()
    }
}

fun CommandCompletions<*>.registerTranslationKeys() {
    this.registerAsyncCompletion("translationKeys") {
        return@registerAsyncCompletion TranslationKey.values().map { key -> key.name }.toList()
    }
}