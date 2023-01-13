package encryptsl.cekuj.net.commands

import cloud.commandframework.annotations.*
import cloud.commandframework.annotations.specifier.Range
import encryptsl.cekuj.net.LiteEco
import encryptsl.cekuj.net.api.Paginator
import encryptsl.cekuj.net.api.enums.TranslationKey
import encryptsl.cekuj.net.api.events.*
import encryptsl.cekuj.net.api.objects.ModernText
import encryptsl.cekuj.net.extensions.isNegative
import encryptsl.cekuj.net.extensions.isZero
import encryptsl.cekuj.net.extensions.moneyFormat
import encryptsl.cekuj.net.extensions.positionIndexed
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

@Suppress("UNUSED")
@CommandDescription("Provided plugin by LiteEco")
class MoneyCMD(private val liteEco: LiteEco) {

    @CommandMethod("money|bal|balance [player]")
    @CommandPermission("lite.eco.money")
    fun onBalance(commandSender: CommandSender, @Argument(value = "player", suggestions = "players") offlinePlayer: OfflinePlayer?) {
        if (commandSender is Player) {
            if (offlinePlayer == null) {
                commandSender.sendMessage(
                    ModernText.miniModernText(
                        liteEco.translationConfig.getMessage("messages.balance_format"),
                        TagResolver.resolver(
                            Placeholder.parsed(
                                "money",
                                liteEco.api.formatting(liteEco.api.getBalance(commandSender))
                            )
                        )
                    )
                )
                return
            }
            commandSender.sendMessage(
                ModernText.miniModernText(
                    liteEco.translationConfig.getMessage("messages.balance_format_target"),
                    TagResolver.resolver(
                        Placeholder.parsed("target", offlinePlayer.name.toString()),
                        Placeholder.parsed(
                            "money",
                            liteEco.api.formatting(liteEco.api.getBalance(offlinePlayer))
                        )
                    )
                )
            )
        } else {
            if (offlinePlayer != null) {
                commandSender.sendMessage(
                    ModernText.miniModernText(
                        liteEco.translationConfig.getMessage("messages.balance_format_target"),
                        TagResolver.resolver(
                            Placeholder.parsed("target", offlinePlayer.name.toString()),
                            Placeholder.parsed(
                                "money",
                                liteEco.api.formatting(liteEco.api.getBalance(offlinePlayer))
                            )
                        )
                    )
                )
                return
            }
            liteEco.translationConfig.getList("messages.help")?.forEach { s ->
                commandSender.sendMessage(ModernText.miniModernText(s.toString()))
            }
        }
    }

    @ProxiedBy("baltop")
    @CommandMethod("money top [page]")
    @CommandPermission("lite.eco.top")
    fun onTopBalance(commandSender: CommandSender, @Argument(value = "page") @Range(min = "1", max="") page: Int?) {
        val p = page ?: 1
        val balances = liteEco.api.getTopBalance().entries.sortedByDescending { e -> e.value }
            .positionIndexed { index, mutableEntry -> LegacyComponentSerializer.legacyAmpersand().serialize(ModernText.miniModernText(
                liteEco.translationConfig.getMessage("messages.balance_top_format"),
                TagResolver.resolver(
                    Placeholder.parsed("position", index.toString()),
                    Placeholder.parsed(
                        "player",
                        Bukkit.getOfflinePlayer(UUID.fromString(mutableEntry.key)).name.toString()
                    ),
                    Placeholder.parsed("money", liteEco.api.formatting(mutableEntry.value))
                )
            )) }

        val pagination = Paginator(balances).apply { page(p) }

        if (p > pagination.maxPages) {
            commandSender.sendMessage(
                ModernText.miniModernText(liteEco.translationConfig.getMessage("messages.balance_top_page_error"),
                    TagResolver.resolver(Placeholder.parsed("maxpage", pagination.maxPages.toString())))
            )
            return
        }

        commandSender.sendMessage(ModernText.miniModernText(liteEco.translationConfig.getMessage("messages.balance_top_line_first"),
            TagResolver.resolver(Placeholder.parsed("page", pagination.page().toString()), Placeholder.parsed("maxpage", pagination.maxPages.toString()))))

        commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', pagination.display()))

        commandSender.sendMessage(ModernText.miniModernText(liteEco.translationConfig.getMessage("messages.balance_top_line_second")))
    }

    @CommandMethod("money|bal|balance help")
    @CommandPermission("lite.eco.help")
    fun onHelp(commandSender: CommandSender) {
        liteEco.translationConfig.getList("messages.help")?.forEach { s ->
            commandSender.sendMessage(ModernText.miniModernText(s.toString()))
        }
    }

    @CommandMethod("money|bal|balance pay <player> <amount>")
    @CommandPermission("lite.eco.pay")
    fun onPayMoney(
        player: Player,
        @Argument(value = "player", suggestions = "offlinePlayers") offlinePlayer: OfflinePlayer,
        @Argument(value = "amount") @Range(min = "1.00", max = "") amount: Double
    ) {
        if (player.name == offlinePlayer.name) {
            player.sendMessage(ModernText.miniModernText(liteEco.translationConfig.getMessage("messages.self_pay_error")))
            return
        }

        if (amount.isNegative() || amount.isZero() || amount.moneyFormat() == "0.00") {
            player.sendMessage(ModernText.miniModernText(liteEco.translationConfig.getMessage("messages.negative_amount_error")))
            return
        }

        liteEco.server.scheduler.runTask(liteEco) { ->
            liteEco.pluginManger.callEvent(PlayerEconomyPayEvent(player, offlinePlayer, amount))
        }
    }

    @CommandMethod("money|bal|balance add <player> <amount>")
    @CommandPermission("lite.eco.add")
    fun onAddMoney(
        commandSender: CommandSender,
        @Argument(value = "player", suggestions = "offlinePlayers") offlinePlayer: OfflinePlayer,
        @Argument(value = "amount") @Range(min = "1.00", max = "") amount: Double
    ) {

        if (amount.isNegative() || amount.isZero() || amount.moneyFormat() == "0.00") {
            commandSender.sendMessage(ModernText.miniModernText(liteEco.translationConfig.getMessage("messages.negative_amount_error")))
            return
        }

        liteEco.server.scheduler.runTask(liteEco) { ->
            liteEco.pluginManger.callEvent(AdminEconomyMoneyDepositEvent(commandSender, offlinePlayer, amount))
        }
    }

    @CommandMethod("money|bal|balance gadd <amount>")
    @CommandPermission("lite.eco.gadd")
    fun onGlobalAddMoney(
        commandSender: CommandSender,
        @Argument("amount") @Range(min = "1.0", max = "") amount: Double
    ) {
        if (amount.isNegative() || amount.isZero() || amount.moneyFormat() == "0.00") {
            commandSender.sendMessage(ModernText.miniModernText(liteEco.translationConfig.getMessage("messages.negative_amount_error")))
            return
        }

        liteEco.server.scheduler.runTask(liteEco) { ->
            liteEco.pluginManger.callEvent(
                AdminEconomyGlobalDepositEvent(commandSender, amount)
            )
        }
    }

    @CommandMethod("money|bal|balance set <player> <amount>")
    @CommandPermission("lite.eco.set")
    fun onSetBalance(
        commandSender: CommandSender,
        @Argument(value = "player", suggestions = "offlinePlayers") offlinePlayer: OfflinePlayer,
        @Argument(value = "amount") amount: Double
    ) {
        if (amount.isNegative()) {
            commandSender.sendMessage(ModernText.miniModernText(liteEco.translationConfig.getMessage("messages.negative_amount_error")))
            return
        }
        liteEco.server.scheduler.runTask(liteEco) { ->
            liteEco.pluginManger.callEvent(
                AdminEconomyMoneySetEvent(
                    commandSender,
                    offlinePlayer,
                    amount
                )
            )
        }
    }

    @CommandMethod("money|bal|balance gset <amount>")
    @CommandPermission("lite.eco.gset")
    fun onGlobalSetMoney(
        commandSender: CommandSender,
        @Argument("amount") @Range(min = "1.0", max = "") amount: Double
    ) {
        if (amount.isNegative()) {
            commandSender.sendMessage(ModernText.miniModernText(liteEco.translationConfig.getMessage("messages.negative_amount_error")))
            return
        }

        liteEco.server.scheduler.runTask(liteEco) { ->
            liteEco.pluginManger.callEvent(
                AdminEconomyGlobalSetEvent(commandSender, amount)
            )
        }
    }

    @CommandMethod("money|bal|balance remove <player> <amount>")
    @CommandPermission("lite.eco.remove")
    fun onRemoveMoney(
        commandSender: CommandSender,
        @Argument(value = "player", suggestions = "offlinePlayers") offlinePlayer: OfflinePlayer,
        @Argument(value = "amount") @Range(min = "1.00", max = "") amount: Double
    ) {

        if (amount.isNegative() || amount.isZero() || amount.moneyFormat() == "0.00") {
            commandSender.sendMessage(ModernText.miniModernText(liteEco.translationConfig.getMessage("messages.negative_amount_error")))
            return
        }

        liteEco.server.scheduler.runTask(liteEco) { ->
            liteEco.pluginManger.callEvent(
                AdminEconomyMoneyWithdrawEvent(
                    commandSender,
                    offlinePlayer,
                    amount
                )
            )
        }
    }

    @CommandMethod("money|bal|balance gremove <amount>")
    @CommandPermission("lite.eco.gremove")
    fun onGlobalRemoveMoney(
        commandSender: CommandSender,
        @Argument("amount") @Range(min = "1.0", max = "") amount: Double
    ) {
        liteEco.server.scheduler.runTask(liteEco) { ->
            liteEco.pluginManger.callEvent(
                AdminEconomyGlobalWithdrawEvent(commandSender, amount)
            )
        }
    }

    @CommandMethod("money|bal|balance lang <isoKey>")
    @CommandPermission("lite.eco.lang")
    fun onLangSwitch(
        commandSender: CommandSender,
        @Argument(value = "isoKey", suggestions = "translationKeys") translationKey: TranslationKey
    ) {
        when (translationKey) {
            TranslationKey.CS_CZ -> {
                liteEco.translationConfig.setTranslationFile(TranslationKey.CS_CZ)
                commandSender.sendMessage(
                    ModernText.miniModernText(
                        liteEco.translationConfig.getMessage("messages.translation_switch"),
                        TagResolver.resolver(Placeholder.parsed("locale", translationKey.name))
                    )
                )
            }

            TranslationKey.EN_US -> {
                liteEco.translationConfig.setTranslationFile(TranslationKey.EN_US)
                commandSender.sendMessage(
                    ModernText.miniModernText(
                        liteEco.translationConfig.getMessage("messages.translation_switch"),
                        TagResolver.resolver(Placeholder.parsed("locale", translationKey.name))
                    )
                )
            }
        }
    }

    @CommandMethod("money|bal|balance reload")
    @CommandPermission("lite.eco.reload")
    fun onReload(commandSender: CommandSender) {
        liteEco.reloadConfig()
        commandSender.sendMessage(ModernText.miniModernText(liteEco.translationConfig.getMessage("messages.config_reload")))
        liteEco.logger.info("Config.yml was reloaded !")
        liteEco.saveConfig()
        liteEco.translationConfig.reloadTranslationConfig()
    }
}