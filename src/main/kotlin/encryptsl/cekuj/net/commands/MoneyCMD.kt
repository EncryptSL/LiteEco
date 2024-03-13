package encryptsl.cekuj.net.commands

import org.incendo.cloud.annotation.specifier.Range
import org.incendo.cloud.annotations.*
import encryptsl.cekuj.net.LiteEco
import encryptsl.cekuj.net.api.Paginator
import encryptsl.cekuj.net.api.events.PlayerEconomyPayEvent
import encryptsl.cekuj.net.api.objects.ModernText
import encryptsl.cekuj.net.extensions.positionIndexed
import encryptsl.cekuj.net.utils.Helper
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

@Suppress("UNUSED")
@CommandDescription("Provided plugin by LiteEco")
class MoneyCMD(private val liteEco: LiteEco) {
    private val helper: Helper = Helper(liteEco)

    @Command("money help")
    @Permission("lite.eco.help")
    fun onHelp(commandSender: CommandSender) {
        liteEco.locale.getList("messages.help")?.forEach { s ->
            commandSender.sendMessage(ModernText.miniModernText(s.toString()))
        }
    }

    @Command("bal|balance [player]")
    @Permission("lite.eco.balance")
    fun onBalanceProxy(commandSender: CommandSender, @Argument(value = "player", suggestions = "players") offlinePlayer: OfflinePlayer?) {
        onBalance(commandSender, offlinePlayer)
    }

    @Command("money bal [player]")
    @Permission("lite.eco.balance")
    fun onBalance(commandSender: CommandSender, @Argument(value = "player", suggestions = "players") offlinePlayer: OfflinePlayer?) {
        if (commandSender is Player) {
            if (offlinePlayer == null) {
                return commandSender.sendMessage(
                    ModernText.miniModernText(
                        liteEco.locale.getMessage("messages.balance.format"),
                        TagResolver.resolver(
                            Placeholder.parsed(
                                "money",
                                liteEco.api.fullFormatting(liteEco.api.getBalance(commandSender))
                            )
                        )
                    )
                )
            }
            commandSender.sendMessage(
                ModernText.miniModernText(
                    liteEco.locale.getMessage("messages.balance.format_target"),
                    TagResolver.resolver(
                        Placeholder.parsed("target", offlinePlayer.name.toString()),
                        Placeholder.parsed(
                            "money",
                            liteEco.api.fullFormatting(liteEco.api.getBalance(offlinePlayer))
                        )
                    )
                )
            )
        } else {
            if (offlinePlayer != null) {
                return commandSender.sendMessage(
                    ModernText.miniModernText(
                        liteEco.locale.getMessage("messages.balance.format_target"),
                        TagResolver.resolver(
                            Placeholder.parsed("target", offlinePlayer.name.toString()),
                            Placeholder.parsed(
                                "money",
                                liteEco.api.fullFormatting(liteEco.api.getBalance(offlinePlayer))
                            )
                        )
                    )
                )
            }
            liteEco.locale.getList("messages.help")?.forEach { s ->
                commandSender.sendMessage(ModernText.miniModernText(s.toString()))
            }
        }
    }

    @ProxiedBy("baltop")
    @Command("money top [page]")
    @Permission("lite.eco.top")
    fun onTopBalance(commandSender: CommandSender, @Argument(value = "page") @Range(min = "1", max="") page: Int?) {
        val p = page ?: 1


        val topPlayers = liteEco.api.getTopBalance().toList()
            .sortedByDescending { e -> e.second }.positionIndexed { index, pair ->
                liteEco.locale.getMessage("messages.balance.top_format")
                    .replace("<position>", index.toString())
                    .replace("<player>", Bukkit.getOfflinePlayer(UUID.fromString(pair.first)).name.toString())
                    .replace("<money>", liteEco.api.fullFormatting(pair.second))
            }
        if (topPlayers.isEmpty()) return

        val pagination = Paginator(topPlayers).apply {
            page(p)
        }

        if (p > pagination.maxPages) {
            return commandSender.sendMessage(
                ModernText.miniModernText(liteEco.locale.getMessage("messages.error.maximum_page"),
                    TagResolver.resolver(Placeholder.parsed("max_page", pagination.maxPages.toString())))
            )
        }

        commandSender.sendMessage(
            ModernText.miniModernText(
                liteEco.locale.getMessage("messages.balance.top_header"),
                TagResolver.resolver(
                    Placeholder.parsed("page", pagination.page().toString()), Placeholder.parsed("max_page", pagination.maxPages.toString())
                ))
                .appendNewline().append(ModernText.miniModernText(pagination.display()))
                .appendNewline().append(ModernText.miniModernText(liteEco.locale.getMessage("messages.balance.top_footer")))
        )
    }

    @ProxiedBy("pay")
    @Command("money pay <player> <amount>")
    @Permission("lite.eco.pay")
    fun onPayMoney(
        commandSender: CommandSender,
        @Argument(value = "player", suggestions = "players") offlinePlayer: OfflinePlayer,
        @Argument(value = "amount") @Range(min = "1.00", max = "") amountStr: String
    ) {
        if (commandSender is Player) {
            if (commandSender.name == offlinePlayer.name) {
                return commandSender.sendMessage(ModernText.miniModernText(liteEco.locale.getMessage("messages.error.self_pay")))
            }

            val amount = helper.validateAmount(amountStr, commandSender) ?: return
            liteEco.pluginManager.callEvent(PlayerEconomyPayEvent(commandSender, offlinePlayer, amount))
        } else {
            commandSender.sendMessage(ModernText.miniModernText("<red>Only a player can use this command."))
        }
    }
}