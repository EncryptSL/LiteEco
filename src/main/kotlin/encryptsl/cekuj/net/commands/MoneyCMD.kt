package encryptsl.cekuj.net.commands

import cloud.commandframework.annotations.*
import cloud.commandframework.annotations.specifier.Range
import encryptsl.cekuj.net.LiteEco
import encryptsl.cekuj.net.api.Paginator
import encryptsl.cekuj.net.api.enums.MigrationKey
import encryptsl.cekuj.net.api.enums.PurgeKey
import encryptsl.cekuj.net.api.enums.LangKey
import encryptsl.cekuj.net.api.events.*
import encryptsl.cekuj.net.api.objects.ModernText
import encryptsl.cekuj.net.extensions.isNegative
import encryptsl.cekuj.net.extensions.isZero
import encryptsl.cekuj.net.extensions.moneyFormat
import encryptsl.cekuj.net.extensions.positionIndexed
import encryptsl.cekuj.net.utils.MigrationData
import encryptsl.cekuj.net.utils.MigrationTool
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

    @CommandMethod("money help")
    @CommandPermission("lite.eco.help")
    fun onHelp(commandSender: CommandSender) {
        liteEco.translationConfig.getList("messages.help")?.forEach { s ->
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
            .filter { data -> Bukkit.getOfflinePlayer(UUID.fromString(data.key)).hasPlayedBefore() }
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

    @ProxiedBy("pay")
    @CommandMethod("money pay <player> <amount>")
    @CommandPermission("lite.eco.pay")
    fun onPayMoney(
        commandSender: CommandSender,
        @Argument(value = "player", suggestions = "players") offlinePlayer: OfflinePlayer,
        @Argument(value = "amount") @Range(min = "1.00", max = "") amount: Double
    ) {
        if (commandSender is Player) {
            if (commandSender.name == offlinePlayer.name) {
                commandSender.sendMessage(ModernText.miniModernText(liteEco.translationConfig.getMessage("messages.self_pay_error")))
                return
            }

            if (amount.isNegative() || amount.isZero() || amount.moneyFormat() == "0.00") {
                commandSender.sendMessage(ModernText.miniModernText(liteEco.translationConfig.getMessage("messages.negative_amount_error")))
                return
            }

            liteEco.server.scheduler.runTask(liteEco) { ->
                liteEco.pluginManger.callEvent(PlayerEconomyPayEvent(commandSender, offlinePlayer, amount))
            }
        } else {
            commandSender.sendMessage(ModernText.miniModernText("<red>Only a player can use this command."))
        }
    }

    @CommandMethod("eco help")
    @CommandPermission("lite.eco.admin.help")
    fun adminHelp(commandSender: CommandSender) {
        liteEco.translationConfig.getList("messages.admin-help")?.forEach { s ->
            commandSender.sendMessage(ModernText.miniModernText(s.toString()))
        }
    }

    @CommandMethod("eco add <player> <amount>")
    @CommandPermission("lite.eco.admin.add")
    fun onAddMoney(
        commandSender: CommandSender,
        @Argument(value = "player", suggestions = "players") offlinePlayer: OfflinePlayer,
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

    @CommandMethod("eco gadd <amount>")
    @CommandPermission("lite.eco.admin.gadd")
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

    @CommandMethod("eco set <player> <amount>")
    @CommandPermission("lite.eco.admin.set")
    fun onSetBalance(
        commandSender: CommandSender,
        @Argument(value = "player", suggestions = "players") offlinePlayer: OfflinePlayer,
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

    @CommandMethod("eco gset <amount>")
    @CommandPermission("lite.eco.admin.gset")
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

    @CommandMethod("eco remove <player> <amount>")
    @CommandPermission("lite.eco.admin.remove")
    fun onRemoveMoney(
        commandSender: CommandSender,
        @Argument(value = "player", suggestions = "players") offlinePlayer: OfflinePlayer,
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

    @CommandMethod("eco gremove <amount>")
    @CommandPermission("lite.eco.admin.gremove")
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

    @CommandMethod("eco lang <isoKey>")
    @CommandPermission("lite.eco.admin.lang")
    fun onLangSwitch(
        commandSender: CommandSender,
        @Argument(value = "isoKey", suggestions = "langKeys") langKey: LangKey
    ) {
        when (langKey) {
            LangKey.CS_CZ -> {
                liteEco.translationConfig.setTranslationFile(LangKey.CS_CZ)
                commandSender.sendMessage(
                    ModernText.miniModernText(
                        liteEco.translationConfig.getMessage("messages.translation_switch"),
                        TagResolver.resolver(Placeholder.parsed("locale", langKey.name))
                    )
                )
            }

            LangKey.EN_US -> {
                liteEco.translationConfig.setTranslationFile(LangKey.EN_US)
                commandSender.sendMessage(
                    ModernText.miniModernText(
                        liteEco.translationConfig.getMessage("messages.translation_switch"),
                        TagResolver.resolver(Placeholder.parsed("locale", langKey.name))
                    )
                )
            }

            LangKey.ES_ES -> {
                liteEco.translationConfig.setTranslationFile(LangKey.ES_ES)
                commandSender.sendMessage(
                    ModernText.miniModernText(
                        liteEco.translationConfig.getMessage("messages.translation_switch"),
                        TagResolver.resolver(Placeholder.parsed("locale", langKey.name))
                    )
                )
            }

            LangKey.JA_JP -> {
                liteEco.translationConfig.setTranslationFile(LangKey.JA_JP)
                commandSender.sendMessage(
                    ModernText.miniModernText(
                        liteEco.translationConfig.getMessage("messages.translation_switch"),
                        TagResolver.resolver(Placeholder.parsed("locale", langKey.name))
                    )
                )
            }
        }
    }

    @CommandMethod("eco purge <argument>")
    @CommandPermission("lite.eco.admin.purge")
    fun onPurge(commandSender: CommandSender, @Argument(value = "argument", suggestions = "purgeKeys") purgeKey: PurgeKey)
    {
        when (purgeKey) {
            PurgeKey.ACCOUNTS -> {
                liteEco.preparedStatements.purgeAccounts()
                commandSender.sendMessage(ModernText.miniModernText(liteEco.translationConfig.getMessage("messages.purge_accounts")))
            }
            PurgeKey.DEFAULT_ACCOUNTS -> {
                liteEco.preparedStatements.purgeDefaultAccounts(liteEco.config.getDouble("plugin.economy.default_money"))
                commandSender.sendMessage(ModernText.miniModernText(liteEco.translationConfig.getMessage("messages.purge_default_accounts")))
            }
            else -> {
                commandSender.sendMessage(ModernText.miniModernText(liteEco.translationConfig.getMessage("messages.purge_argument_error")))
            }
        }
    }

    @CommandMethod("eco migration <argument>")
    @CommandPermission("lite.eco.admin.migration")
    fun onMigration(commandSender: CommandSender, @Argument(value = "argument", suggestions = "migrationKeys") migrationKey: MigrationKey) {
        val migrationTool = MigrationTool(liteEco)
        val output = liteEco.api.getTopBalance().toList().positionIndexed { index, k -> MigrationData(index, k.first, k.second) }
        when(migrationKey) {
            MigrationKey.CSV -> {
                migrationTool.migrateToCSV(output, "economy_migration")
                commandSender.sendMessage(ModernText.miniModernText(
                    liteEco.translationConfig.getMessage("messages.migration_success"),
                    TagResolver.resolver(
                        Placeholder.parsed("type", migrationKey.name)
                    )
                ))
            }
            MigrationKey.SQL -> {
                migrationTool.migrateToSQL(output, "economy_migration")
                commandSender.sendMessage(ModernText.miniModernText(
                    liteEco.translationConfig.getMessage("messages.migration_success"),
                    TagResolver.resolver(
                        Placeholder.parsed("type", migrationKey.name)
                    )
                ))
            }
        }
    }

    @CommandMethod("eco reload")
    @CommandPermission("lite.eco.admin.reload")
    fun onReload(commandSender: CommandSender) {
        liteEco.reloadConfig()
        commandSender.sendMessage(ModernText.miniModernText(liteEco.translationConfig.getMessage("messages.config_reload")))
        liteEco.logger.info("Config.yml was reloaded !")
        liteEco.saveConfig()
        liteEco.translationConfig.reloadTranslationConfig()
    }
}