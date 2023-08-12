package encryptsl.cekuj.net.commands

import cloud.commandframework.annotations.*
import cloud.commandframework.annotations.specifier.Range
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

    @CommandMethod("money help")
    @CommandPermission("lite.eco.help")
    fun onHelp(commandSender: CommandSender) {
        liteEco.locale.getList("messages.help")?.forEach { s ->
            commandSender.sendMessage(ModernText.miniModernText(s.toString()))
        }
    }

    @CommandMethod("bal|balance [player]")
    @CommandPermission("lite.eco.balance")
    fun onBalanceProxy(commandSender: CommandSender, @Argument(value = "player", suggestions = "players") offlinePlayer: OfflinePlayer?) {
        onBalance(commandSender, offlinePlayer)
    }

    @CommandMethod("money bal [player]")
    @CommandPermission("lite.eco.balance")
    fun onBalance(commandSender: CommandSender, @Argument(value = "player", suggestions = "players") offlinePlayer: OfflinePlayer?) {
        if (commandSender is Player) {
            if (offlinePlayer == null) {
                commandSender.sendMessage(
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
                return
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
                return
            }
            liteEco.locale.getList("messages.help")?.forEach { s ->
                commandSender.sendMessage(ModernText.miniModernText(s.toString()))
            }
        }
    }

    @ProxiedBy("baltop")
    @CommandMethod("money top [page]")
    @CommandPermission("lite.eco.top")
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
            commandSender.sendMessage(
                ModernText.miniModernText(liteEco.locale.getMessage("messages.error.maximum_page"),
                    TagResolver.resolver(Placeholder.parsed("max_page", pagination.maxPages.toString())))
            )
            return
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
    @CommandMethod("money pay <player> <amount>")
    @CommandPermission("lite.eco.pay")
    fun onPayMoney(
        commandSender: CommandSender,
        @Argument(value = "player", suggestions = "players") offlinePlayer: OfflinePlayer,
        @Argument(value = "amount") @Range(min = "1.00", max = "") amountStr: String
    ) {
        if (commandSender is Player) {
            if (commandSender.name == offlinePlayer.name) {
                commandSender.sendMessage(ModernText.miniModernText(liteEco.locale.getMessage("messages.error.self_pay")))
                return
            }

            val amount = helper.validateAmount(amountStr, commandSender) ?: return

            liteEco.server.scheduler.runTask(liteEco) { ->
                liteEco.pluginManager.callEvent(PlayerEconomyPayEvent(commandSender, offlinePlayer, amount))
            }
        } else {
            commandSender.sendMessage(ModernText.miniModernText("<red>Only a player can use this command."))
        }
    }
}