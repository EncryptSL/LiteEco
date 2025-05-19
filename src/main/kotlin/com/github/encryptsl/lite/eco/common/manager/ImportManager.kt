package com.github.encryptsl.lite.eco.common.manager

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.utils.ImportEconomy
import com.github.encryptsl.lite.eco.utils.ImportEconomy.Economies
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.command.CommandSender

class ImportManager(
    private val liteEco: LiteEco,
    private val importEconomy: ImportEconomy
) {

    fun importEconomy(sender: CommandSender, economy: Economies, currency: String) {
        when (economy) {
            Economies.EssentialsX -> {
                importEconomy.importEssentialsXEconomy(currency)
            }
            Economies.BetterEconomy -> {
                importEconomy.importBetterEconomy(currency)
            }
            Economies.ScruffyBoyEconomy -> {
                importEconomy.importScruffyBoyEconomy(currency)
            }
            Economies.CraftConomy3 -> {
                importEconomy.importCraftConomy3(currency)
            }
        }
        val (converted, balances) = importEconomy.getResult()

        if (converted == 0)
            return sender.sendMessage(liteEco.locale.translation("messages.error.import_failed"))

        sender.sendMessage(liteEco.locale.translation("messages.admin.import_success",
            TagResolver.resolver(
                Placeholder.parsed("economy", economy.name),
                Placeholder.parsed("converted", converted.toString()),
                Placeholder.parsed("balances", balances.toString())
            )
        ))
        importEconomy.convertRefresh()
    }

}