package com.github.encryptsl.lite.eco.common.manager

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.utils.ConvertEconomy
import com.github.encryptsl.lite.eco.utils.ConvertEconomy.Economies
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.command.CommandSender

class ImportManager(
    private val liteEco: LiteEco,
    private val convertEconomy: ConvertEconomy
) {

    fun importEconomy(sender: CommandSender, economy: Economies, currency: String) {
        when (economy) {
            Economies.EssentialsX -> {
                convertEconomy.convertEssentialsXEconomy(currency)
            }

            Economies.BetterEconomy -> {
                convertEconomy.convertBetterEconomy(currency)
            }
        }
        val (converted, balances) = convertEconomy.getResult()
        sender.sendMessage(liteEco.locale.translation("messages.admin.convert_success",
            TagResolver.resolver(
                Placeholder.parsed("economy", economy.name),
                Placeholder.parsed("converted", converted.toString()),
                Placeholder.parsed("balances", balances.toString())
            )
        ))
        convertEconomy.convertRefresh()
    }

}