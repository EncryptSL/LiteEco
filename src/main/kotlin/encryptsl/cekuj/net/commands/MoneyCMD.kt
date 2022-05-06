package encryptsl.cekuj.net.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import encryptsl.cekuj.net.LiteEco
import encryptsl.cekuj.net.api.enums.TransactionType
import encryptsl.cekuj.net.api.enums.TranslationKey
import encryptsl.cekuj.net.api.events.AdminEconomyTransactionEvent
import encryptsl.cekuj.net.api.events.ConsoleEconomyTransactionEvent
import encryptsl.cekuj.net.api.events.PlayerEconomyPayEvent
import encryptsl.cekuj.net.api.objects.ModernText
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@Suppress("UNUSED")
@CommandAlias("money")
@Description("Provided plugin by LiteEco")
class MoneyCMD(private val liteEco: LiteEco) : BaseCommand() {

    @Default
    @CommandPermission("lite.eco.money")
    fun onBalance(commandSender: CommandSender) {
        if (commandSender is Player) {
            commandSender.sendMessage(
                ModernText.miniModernText(
                    liteEco.translationConfig.getMessage("messages.balance_format"),
                    TagResolver.resolver(Placeholder.parsed("money", liteEco.econ?.getBalance(commandSender.player).toString()))
                )
            )
        } else {
            liteEco.translationConfig.getList("messages.help")?.forEach { s ->
                commandSender.sendMessage(ModernText.miniModernText(s.toString()))
            }
        }
    }

    @Subcommand("t|target")
    @CommandCompletion("@offlinePlayers")
    @CommandPermission("lite.eco.target")
    fun onTargetBalance(commandSender: CommandSender, @Values("@offlinePlayers") offlinePlayer: OfflinePlayer) {
        commandSender.sendMessage(
            ModernText.miniModernText(
                liteEco.translationConfig.getMessage("messages.balance_format_target"),
                TagResolver.resolver(Placeholder.parsed("target", offlinePlayer.name.toString()),
                    Placeholder.parsed("money", liteEco.econ?.getBalance(offlinePlayer).toString()))
            )
        )
    }

    @Subcommand("help")
    @CommandPermission("lite.eco.help")
    fun onHelp(commandSender: CommandSender) {
        liteEco.translationConfig.getList("messages.help")?.forEach { s ->
            commandSender.sendMessage(ModernText.miniModernText(s.toString()))
        }
    }

    @Subcommand("pay")
    @CommandPermission("lite.eco.pay")
    @CommandCompletion("@offlinePlayers")
    fun onPayMoney(player: Player, @Values("@offlinePlayers") offlinePlayer: OfflinePlayer, amount: Double) {
        if (player.name == offlinePlayer.name) {
            player.sendMessage(ModernText.miniModernText(liteEco.translationConfig.getMessage("messages.self_pay_error")))
            return
        }
        liteEco.pluginManger.callEvent(PlayerEconomyPayEvent(player, offlinePlayer,TransactionType.PAY, amount))
    }

    @Subcommand("add")
    @CommandPermission("lite.eco.add")
    @CommandCompletion("@offlinePlayers")
    fun onAddMoney(commandSender: CommandSender, @Values("@offlinePlayers") offlinePlayer: OfflinePlayer, amount: Double) {
        if (commandSender is Player) {
            liteEco.pluginManger.callEvent(AdminEconomyTransactionEvent(commandSender, offlinePlayer, TransactionType.ADD, amount))
        } else {
            liteEco.pluginManger.callEvent(ConsoleEconomyTransactionEvent(commandSender, offlinePlayer, TransactionType.ADD, amount))
        }
    }

    @Subcommand("remove")
    @CommandPermission("lite.eco.remove")
    @CommandCompletion("@offlinePlayers")
    fun onRemoveAccount(commandSender: CommandSender, @Values("@offlinePlayers") offlinePlayer: OfflinePlayer, amount: Double) {
        if (commandSender is Player) {
            liteEco.pluginManger.callEvent(AdminEconomyTransactionEvent(commandSender, offlinePlayer,TransactionType.WITHDRAW, amount))
        } else {
            liteEco.pluginManger.callEvent(ConsoleEconomyTransactionEvent(commandSender, offlinePlayer,TransactionType.WITHDRAW, amount))
        }
    }

    @Subcommand("set")
    @CommandPermission("lite.eco.set")
    @CommandCompletion("@offlinePlayers")
    fun onSetBalance(commandSender: CommandSender, @Values("@offlinePlayers") offlinePlayer: OfflinePlayer , amount: Double) {
        if (commandSender is Player) {
            liteEco.pluginManger.callEvent(AdminEconomyTransactionEvent(commandSender, offlinePlayer, TransactionType.SET, amount))
        } else {
            liteEco.pluginManger.callEvent(ConsoleEconomyTransactionEvent(commandSender, offlinePlayer, TransactionType.SET, amount))
        }
    }

    @Subcommand("lang")
    @CommandPermission("lite.eco.lang")
    @CommandCompletion("@translationKeys")
    fun onLangSwitch(commandSender: CommandSender, @Values("@translationKeys") translationKey: TranslationKey) {
        when (translationKey) {
            TranslationKey.CS_CZ -> {
                liteEco.translationConfig.setTranslationFile(TranslationKey.CS_CZ)
                commandSender.sendMessage(ModernText.miniModernText(liteEco.translationConfig.getMessage("messages.translation_switch"),
                    TagResolver.resolver(Placeholder.parsed("locale", translationKey.name))))
            }
            TranslationKey.EN_US -> {
                liteEco.translationConfig.setTranslationFile(TranslationKey.EN_US)
                commandSender.sendMessage(ModernText.miniModernText(liteEco.translationConfig.getMessage("messages.translation_switch"),
                    TagResolver.resolver(Placeholder.parsed("locale", translationKey.name))))
            }
        }
    }

    @Subcommand("reload")
    @CommandPermission("lite.eco.reload")
    fun onReload(commandSender: CommandSender) {
        liteEco.reloadConfig()
        commandSender.sendMessage(ModernText.miniModernText(liteEco.translationConfig.getMessage("messages.config_reload")))
        liteEco.logger.info("Config.yml was reloaded !")
        liteEco.saveConfig()
        liteEco.translationConfig.reloadTranslationConfig()
    }
}