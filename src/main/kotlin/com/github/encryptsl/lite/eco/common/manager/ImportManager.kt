package com.github.encryptsl.lite.eco.common.manager

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.common.manager.importer.ImportEconomy
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.command.CommandSender

class ImportManager(
    private val liteEco: LiteEco,
    private val importEconomy: ImportEconomy
) {

    suspend fun importEconomy(
        sender: CommandSender,
        economyName: String,
        targetCurrency: String,
        fromCurrency: String?,
    ) {
        val (converted, balances) = try {
            importEconomy.import(economyName, targetCurrency, fromCurrency)
        } catch (e : Exception) {
            liteEco.componentLogger.error("Failed to import Economy: $economyName {}", e.message)
            sender.sendMessage(liteEco.locale.translation("messages.error.import_failed"))
            return
        }

        if (converted == 0) {
            sender.sendMessage(liteEco.locale.translation("messages.error.import_failed"))
            return
        }

        sender.sendMessage(
            liteEco.locale.translation(
                "messages.admin.import_success",
                TagResolver.resolver(
                    Placeholder.parsed("economy", economyName),
                    Placeholder.parsed("converted", converted.toString()),
                    Placeholder.parsed("balances", balances.toPlainString())
                )
            )
        )
    }

}