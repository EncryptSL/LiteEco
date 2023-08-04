package encryptsl.cekuj.net.commands

import cloud.commandframework.annotations.*
import cloud.commandframework.annotations.specifier.Range
import encryptsl.cekuj.net.LiteEco
import encryptsl.cekuj.net.api.Paginator
import encryptsl.cekuj.net.api.enums.CheckLevel
import encryptsl.cekuj.net.api.enums.LangKey
import encryptsl.cekuj.net.api.enums.MigrationKey
import encryptsl.cekuj.net.api.enums.PurgeKey
import encryptsl.cekuj.net.api.events.*
import encryptsl.cekuj.net.api.objects.ModernText
import encryptsl.cekuj.net.extensions.*
import encryptsl.cekuj.net.utils.MigrationData
import encryptsl.cekuj.net.utils.MigrationTool
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

@Suppress("UNUSED")
@CommandDescription("Provided plugin by LiteEco")
class MoneyCMD(private val liteEco: LiteEco) {

    private fun validateAmount(amountStr: String, commandSender: CommandSender, checkLevel: CheckLevel = CheckLevel.FULL): Double? {
        val amount = amountStr.toValidDecimal()
        return when {
            amount == null -> {
                commandSender.sendMessage(ModernText.miniModernText(liteEco.locale.getMessage("messages.error.format_amount")))
                null
            }
            checkLevel == CheckLevel.ONLY_NEGATIVE && amount.isNegative() || checkLevel == CheckLevel.FULL && (amount.isApproachingZero()) -> {
                commandSender.sendMessage(ModernText.miniModernText(liteEco.locale.getMessage("messages.error.negative_amount")))
                null
            }
            else -> amount
        }
    }

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
                                liteEco.api.formatting(liteEco.api.getBalance(commandSender))
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
                            liteEco.api.formatting(liteEco.api.getBalance(offlinePlayer))
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
                                liteEco.api.formatting(liteEco.api.getBalance(offlinePlayer))
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
        val balances = liteEco.api.getTopBalance().entries.sortedByDescending { e -> e.value }
            .filter { data -> Bukkit.getOfflinePlayer(UUID.fromString(data.key)).hasPlayedBefore() }
            .positionIndexed { index, mutableEntry -> LegacyComponentSerializer.legacyAmpersand().serialize(ModernText.miniModernText(
                liteEco.locale.getMessage("messages.balance.top_format"),
                TagResolver.resolver(
                    Placeholder.parsed("position", index.toString()),
                    Placeholder.parsed(
                        "player",
                        Bukkit.getOfflinePlayer(UUID.fromString(mutableEntry.key)).name.toString()
                    ),
                    Placeholder.parsed("money", liteEco.api.formatting(mutableEntry.value))
                )
            )) }

        if (balances.isEmpty()) return

        val pagination = Paginator(balances).apply { page(p) }

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
                .appendNewline().append(LegacyComponentSerializer.legacyAmpersand().deserialize(pagination.display()))
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
                commandSender.sendMessage(ModernText.miniModernText(liteEco.locale.getMessage("messages.self_pay_error")))
                return
            }

            val amount = validateAmount(amountStr, commandSender) ?: return

            liteEco.server.scheduler.runTask(liteEco) { ->
                liteEco.pluginManager.callEvent(PlayerEconomyPayEvent(commandSender, offlinePlayer, amount))
            }
        } else {
            commandSender.sendMessage(ModernText.miniModernText("<red>Only a player can use this command."))
        }
    }

    @CommandMethod("eco help")
    @CommandPermission("lite.eco.admin.help")
    fun adminHelp(commandSender: CommandSender) {
        liteEco.locale.getList("messages.admin-help")?.forEach { s ->
            commandSender.sendMessage(ModernText.miniModernText(s.toString()))
        }
    }

    @CommandMethod("eco add <player> <amount>")
    @CommandPermission("lite.eco.admin.add")
    fun onAddMoney(
        commandSender: CommandSender,
        @Argument(value = "player", suggestions = "players") offlinePlayer: OfflinePlayer,
        @Argument(value = "amount") @Range(min = "1.00", max = "") amountStr: String
    ) {
        val amount = validateAmount(amountStr, commandSender) ?: return

        liteEco.server.scheduler.runTask(liteEco) { ->
            liteEco.pluginManager.callEvent(AdminEconomyMoneyDepositEvent(commandSender, offlinePlayer, amount))
        }
    }

    @CommandMethod("eco gadd <amount>")
    @CommandPermission("lite.eco.admin.gadd")
    fun onGlobalAddMoney(
        commandSender: CommandSender,
        @Argument("amount") @Range(min = "1.0", max = "") amountStr: String
    ) {
        val amount = validateAmount(amountStr, commandSender) ?: return

        liteEco.server.scheduler.runTask(liteEco) { ->
            liteEco.pluginManager.callEvent(
                AdminEconomyGlobalDepositEvent(commandSender, amount)
            )
        }
    }

    @CommandMethod("eco set <player> <amount>")
    @CommandPermission("lite.eco.admin.set")
    fun onSetBalance(
        commandSender: CommandSender,
        @Argument(value = "player", suggestions = "players") offlinePlayer: OfflinePlayer,
        @Argument(value = "amount") amountStr: String
    ) {
        val amount = validateAmount(amountStr, commandSender, CheckLevel.ONLY_NEGATIVE) ?: return

        liteEco.server.scheduler.runTask(liteEco) { ->
            liteEco.pluginManager.callEvent(
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
        @Argument("amount") @Range(min = "1.0", max = "") amountStr: String
    ) {
        val amount = validateAmount(amountStr, commandSender, CheckLevel.ONLY_NEGATIVE) ?: return

        liteEco.server.scheduler.runTask(liteEco) { ->
            liteEco.pluginManager.callEvent(
                AdminEconomyGlobalSetEvent(commandSender, amount)
            )
        }
    }

    @CommandMethod("eco remove <player> <amount>")
    @CommandPermission("lite.eco.admin.remove")
    fun onRemoveMoney(
        commandSender: CommandSender,
        @Argument(value = "player", suggestions = "players") offlinePlayer: OfflinePlayer,
        @Argument(value = "amount") @Range(min = "1.00", max = "") amountStr: String
    ) {
        val amount = validateAmount(amountStr, commandSender) ?: return

        liteEco.server.scheduler.runTask(liteEco) { ->
            liteEco.pluginManager.callEvent(
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
        @Argument("amount") @Range(min = "1.0", max = "") amountStr: String
    ) {
        val amount = validateAmount(amountStr, commandSender) ?: return

        liteEco.server.scheduler.runTask(liteEco) { ->
            liteEco.pluginManager.callEvent(
                AdminEconomyGlobalWithdrawEvent(commandSender, amount)
            )
        }
    }

    @CommandMethod("eco lang <isoKey>")
    @CommandPermission("lite.eco.admin.lang")
    fun onLangSwitch(
        commandSender: CommandSender,
        @Argument(value = "isoKey", suggestions = "langKeys") isoKey: String
    ) {
        try {
            val langKey = LangKey.valueOf(isoKey.uppercase())
            liteEco.locale.setTranslationFile(langKey)
            commandSender.sendMessage(
                ModernText.miniModernText(
                    liteEco.locale.getMessage("messages.admin.translation_switch"),
                    TagResolver.resolver(Placeholder.parsed("locale", langKey.name))
                )
            )
        } catch (_: IllegalArgumentException) {
            commandSender.sendMessage(
                ModernText.miniModernText(
                    "That translation doesn't exist."
                )
            )
        }
    }

    @CommandMethod("eco purge <argument>")
    @CommandPermission("lite.eco.admin.purge")
    fun onPurge(commandSender: CommandSender, @Argument(value = "argument", suggestions = "purgeKeys") purgeKey: PurgeKey)
    {
        @Suppress("REDUNDANT_ELSE_IN_WHEN")
        when (purgeKey) {
            PurgeKey.ACCOUNTS -> {
                liteEco.preparedStatements.purgeAccounts()
                commandSender.sendMessage(ModernText.miniModernText(liteEco.locale.getMessage("messages.admin.purge_accounts")))
            }
            PurgeKey.NULL_ACCOUNTS -> {
                liteEco.preparedStatements.purgeInvalidAccounts()
                commandSender.sendMessage(ModernText.miniModernText(liteEco.locale.getMessage("messages.admin.purge_null_accounts")))
            }
            PurgeKey.DEFAULT_ACCOUNTS -> {
                liteEco.preparedStatements.purgeDefaultAccounts(liteEco.config.getDouble("economy.starting_balance"))
                commandSender.sendMessage(ModernText.miniModernText(liteEco.locale.getMessage("messages.admin.purge_default_accounts")))
            }
            else -> {
                commandSender.sendMessage(ModernText.miniModernText(liteEco.locale.getMessage("messages.error.purge_argument")))
            }
        }
    }

    @CommandMethod("eco migration <argument>")
    @CommandPermission("lite.eco.admin.migration")
    fun onMigration(commandSender: CommandSender, @Argument(value = "argument", suggestions = "migrationKeys") migrationKey: MigrationKey) {
        val migrationTool = MigrationTool(liteEco)
        val output = liteEco.api.getTopBalance().toList().positionIndexed { index, k -> MigrationData(index, k.first, k.second) }
        when(migrationKey) {
            MigrationKey.CSV -> migrationTool.migrateToCSV(output, "economy_migration")
            MigrationKey.SQL -> migrationTool.migrateToSQL(output, "economy_migration")
        }
        commandSender.sendMessage(ModernText.miniModernText(
            liteEco.locale.getMessage("messages.admin.migration_success"),
            TagResolver.resolver(
                Placeholder.parsed("type", migrationKey.name)
            )
        ))
    }

    @CommandMethod("eco reload")
    @CommandPermission("lite.eco.admin.reload")
    fun onReload(commandSender: CommandSender) {
        liteEco.reloadConfig()
        commandSender.sendMessage(ModernText.miniModernText(liteEco.locale.getMessage("messages.admin.config_reload")))
        liteEco.logger.info("Config.yml was reloaded [!]")
        liteEco.saveConfig()
        liteEco.locale.reloadTranslation()
    }
}