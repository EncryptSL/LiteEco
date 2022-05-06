package encryptsl.cekuj.net.api.objects

import co.aikar.commands.CommandCompletions
import encryptsl.cekuj.net.api.enums.TranslationKey
import org.bukkit.Bukkit

object CommandsCompletions {
    fun registerOfflinePlayers(commandCompletions: CommandCompletions<*>) {
        commandCompletions.registerAsyncCompletion("offlinePlayers") {
            return@registerAsyncCompletion Bukkit.getOfflinePlayers().map { offlinePlayer -> offlinePlayer.name }.toList()
        }
    }
    fun registerTranslationKeys(commandCompletions: CommandCompletions<*>) {
        commandCompletions.registerAsyncCompletion("translationKeys") {
            return@registerAsyncCompletion TranslationKey.values().map { key -> key.name }.toList()
        }
    }
}