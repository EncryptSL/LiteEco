package com.github.encryptsl.lite.eco.commands

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.enums.CheckLevel
import com.github.encryptsl.lite.eco.api.enums.PurgeKey
import com.github.encryptsl.lite.eco.api.events.admin.*
import com.github.encryptsl.lite.eco.api.objects.ModernText
import com.github.encryptsl.lite.eco.common.config.Locales
import com.github.encryptsl.lite.eco.common.extensions.convertInstant
import com.github.encryptsl.lite.eco.common.extensions.getRandomString
import com.github.encryptsl.lite.eco.common.extensions.positionIndexed
import com.github.encryptsl.lite.eco.utils.ConvertEconomy
import com.github.encryptsl.lite.eco.utils.ConvertEconomy.Economies
import com.github.encryptsl.lite.eco.utils.Helper
import com.github.encryptsl.lite.eco.utils.MigrationTool
import com.github.encryptsl.lite.eco.utils.MigrationTool.MigrationKey
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.util.ChatPaginator
import org.incendo.cloud.annotation.specifier.Range
import org.incendo.cloud.annotations.*
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.system.measureTimeMillis

@Suppress("UNUSED")
@CommandDescription("Provided plugin by LiteEco")
class EcoCMD(private val liteEco: LiteEco) {

    private val helper: Helper = Helper(liteEco)
    private val convertEconomy: ConvertEconomy = ConvertEconomy(liteEco)

    @Command("eco help")
    @Permission("lite.eco.admin.help")
    fun adminHelp(commandSender: CommandSender) {
        liteEco.locale.getList("messages.admin-help")?.forEach { s ->
            commandSender.sendMessage(ModernText.miniModernText(s.toString()))
        }
    }

    @Command("eco add <player> <amount>")
    @Permission("lite.eco.admin.add")
    fun onAddMoney(
        commandSender: CommandSender,
        @Argument(value = "player", suggestions = "players") offlinePlayer: OfflinePlayer,
        @Argument(value = "amount") @Range(min = "1.00", max = "") amountStr: String,
        @Flag(value = "silent", aliases = ["s"]) silent: Boolean
    ) {
        val amount = helper.validateAmount(amountStr, commandSender) ?: return
        liteEco.pluginManager.callEvent(EconomyMoneyDepositEvent(commandSender, offlinePlayer, amount, silent))
    }

    @Command("eco global add <amount>")
    @Permission("lite.eco.admin.gadd", "lite.eco.admin.global.add")
    fun onGlobalAddMoney(
        commandSender: CommandSender,
        @Argument("amount") @Range(min = "1.0", max = "") amountStr: String
    ) {
        val amount = helper.validateAmount(amountStr, commandSender) ?: return
        liteEco.pluginManager.callEvent(EconomyGlobalDepositEvent(commandSender, amount))
    }

    @Command("eco set <player> <amount>")
    @Permission("lite.eco.admin.set")
    fun onSetBalance(
        commandSender: CommandSender,
        @Argument(value = "player", suggestions = "players") offlinePlayer: OfflinePlayer,
        @Argument(value = "amount") amountStr: String
    ) {
        val amount = helper.validateAmount(amountStr, commandSender, CheckLevel.ONLY_NEGATIVE) ?: return
        liteEco.pluginManager.callEvent(EconomyMoneySetEvent(commandSender, offlinePlayer, amount))
    }

    @Command("eco global set <amount>")
    @Permission("lite.eco.admin.gset", "lite.eco.admin.global.withdraw")
    fun onGlobalSetMoney(
        commandSender: CommandSender,
        @Argument("amount") @Range(min = "1.0", max = "") amountStr: String
    ) {
        val amount = helper.validateAmount(amountStr, commandSender, CheckLevel.ONLY_NEGATIVE) ?: return
        liteEco.pluginManager.callEvent(EconomyGlobalSetEvent(commandSender, amount))
    }

    @Command("eco withdraw <player> <amount>")
    @Permission("lite.eco.admin.withdraw", "lite.eco.admin.remove")
    fun onWithdrawMoney(
        commandSender: CommandSender,
        @Argument(value = "player", suggestions = "players") offlinePlayer: OfflinePlayer,
        @Argument(value = "amount") @Range(min = "1.00", max = "") amountStr: String,
        @Flag(value = "silent", aliases = ["s"]) silent: Boolean
    ) {
        val amount = helper.validateAmount(amountStr, commandSender) ?: return
        liteEco.pluginManager.callEvent(EconomyMoneyWithdrawEvent(commandSender, offlinePlayer, amount, silent))
    }

    @Command("eco global withdraw <amount>")
    @Permission("lite.eco.admin.gremove", "lite.eco.admin.global.withdraw")
    fun onGlobalWithdrawMoney(
        commandSender: CommandSender,
        @Argument("amount") @Range(min = "1.0", max = "") amountStr: String
    ) {
        val amount = helper.validateAmount(amountStr, commandSender) ?: return

        liteEco.pluginManager.callEvent(
            EconomyGlobalWithdrawEvent(commandSender, amount)
        )
    }

    @Command("eco delete <player>")
    @Permission("lite.eco.admin.delete")
    fun onDeleteWalletAccount(
        commandSender: CommandSender,
        @Argument("player", suggestions = "players") offlinePlayer: OfflinePlayer
    ) {
        val message = if (liteEco.api.deleteAccount(offlinePlayer)) {
            "messages.admin.delete_account"
        } else {
            "messages.error.account_not_exist"
        }
        commandSender.sendMessage(liteEco.locale.translation(message, Placeholder.parsed("player", offlinePlayer.name.toString())))
    }

    @Command("eco monolog [page] [player]")
    @Permission("lite.eco.admin.monolog")
    fun onLogView(commandSender: CommandSender, @Argument("page") @Default(value = "1") page: Int, @Argument("player") player: String?) {

        val log = helper.validateLog(player).map {
            liteEco.locale.getMessage("messages.admin.monolog_format")
                .replace("<level>", it.level)
                .replace("<timestamp>", convertInstant(it.timestamp))
                .replace("<log>", it.log)
        }
        if (log.isEmpty()) return
        val pagination = ChatPaginator.paginate(log.joinToString("\n"), page)
        val isPageAboveMaxPages = page > pagination.totalPages

        if (isPageAboveMaxPages)
            return commandSender.sendMessage(liteEco.locale.translation("messages.error.maximum_page",
                Placeholder.parsed("max_page", pagination.totalPages.toString()))
            )

        for (line in pagination.lines) {
            commandSender.sendMessage(ModernText.miniModernText(line))
        }
    }

    @Command("eco lang <isoKey>")
    @Permission("lite.eco.admin.lang")
    fun onLangSwitch(
        commandSender: CommandSender,
        @Argument(value = "isoKey", suggestions = "langKeys") isoKey: Locales.LangKey
    ) {
        try {
            val langKey = Locales.LangKey.valueOf(isoKey.name)
            liteEco.locale.setLocale(langKey)
            commandSender.sendMessage(
                liteEco.locale.translation("messages.admin.translation_switch", Placeholder.parsed("locale", langKey.name))
            )
        } catch (_: IllegalArgumentException) {
            commandSender.sendMessage(
                ModernText.miniModernText(
                    "That translation doesn't exist."
                )
            )
        }
    }

    @Command("eco purge <argument>")
    @Permission("lite.eco.admin.purge")
    fun onPurge(commandSender: CommandSender, @Argument(value = "argument", suggestions = "purgeKeys") purgeKey: PurgeKey)
    {
        @Suppress("REDUNDANT_ELSE_IN_WHEN")
        when (purgeKey) {
            PurgeKey.ACCOUNTS -> {
                liteEco.databaseEcoModel.purgeAccounts()
                commandSender.sendMessage(liteEco.locale.translation("messages.admin.purge_accounts"))
            }
            PurgeKey.NULL_ACCOUNTS -> {
                liteEco.databaseEcoModel.purgeInvalidAccounts()
                commandSender.sendMessage(liteEco.locale.translation("messages.admin.purge_null_accounts"))
            }
            PurgeKey.DEFAULT_ACCOUNTS -> {
                liteEco.databaseEcoModel.purgeDefaultAccounts(liteEco.config.getDouble("economy.starting_balance"))
                commandSender.sendMessage(liteEco.locale.translation("messages.admin.purge_default_accounts"))
            }
            PurgeKey.MONO_LOG -> {
                liteEco.loggerModel.getLog().thenApply { el ->
                    if (el.isEmpty()) {
                        throw Exception("You can't remove monolog because is empty.")
                    }
                    return@thenApply el
                }.thenApply { el ->
                    liteEco.loggerModel.clearLogs()
                    commandSender.sendMessage(liteEco.locale.translation("messages.admin.purge_monolog_success", Placeholder.parsed("deleted", el.size.toString())))
                }.exceptionally { el ->
                    commandSender.sendMessage(liteEco.locale.translation("messages.error.purge_monolog_fail"))
                    liteEco.logger.severe(el.message ?: el.localizedMessage)
                }
            }
            else -> {
                commandSender.sendMessage(liteEco.locale.translation("messages.error.purge_argument"))
            }
        }
    }

    @Command("eco migration <argument>")
    @Permission("lite.eco.admin.migration")
    fun onMigration(commandSender: CommandSender, @Argument(value = "argument", suggestions = "migrationKeys") migrationKey: MigrationKey) {
        val migrationTool = MigrationTool(liteEco)
        val output = liteEco.api.getTopBalance().toList().positionIndexed { index, k -> MigrationTool.MigrationData(index, Bukkit.getOfflinePlayer(k.first).name.toString(), k.first, k.second) }

        val result = when(migrationKey) {
            MigrationKey.CSV -> migrationTool.migrateToCSV(output, "economy_migration")
            MigrationKey.SQL -> migrationTool.migrateToSQL(output, "economy_migration")
        }

        val messageKey = if (result) {
            "messages.admin.migration_success"
        } else {
            "messages.error.migration_failed"
        }

        commandSender.sendMessage(
            liteEco.locale.translation(messageKey,
            TagResolver.resolver(
                Placeholder.parsed("type", migrationKey.name)
            )
        ))
    }

    @Command("eco convert <economy>")
    @Permission("lite.eco.admin.convert")
    fun onEconomyConvert(commandSender: CommandSender, @Argument("economy", suggestions = "economies") economy: Economies) {
        try {
            when (economy) {
                Economies.EssentialsX -> {
                    convertEconomy.convertEssentialsXEconomy()
                }

                Economies.BetterEconomy -> {
                    convertEconomy.convertBetterEconomy()
                }
            }
            val (converted, balances) = convertEconomy.getResult()
            commandSender.sendMessage(
                liteEco.locale.translation("messages.admin.convert_success",
                TagResolver.resolver(
                    Placeholder.parsed("economy", economy.name),
                    Placeholder.parsed("converted", converted.toString()),
                    Placeholder.parsed("balances", balances.toString())
                )
            ))
            convertEconomy.convertRefresh()
        } catch (e : Exception) {
            liteEco.logger.info(e.message ?: e.localizedMessage)
            commandSender.sendMessage(liteEco.locale.translation("messages.error.convert_fail"))
        }
    }

    @Command("eco debug create accounts <amount>")
    @Permission("lite.eco.admin.debug.create.accounts")
    fun onDebugCreateAccounts(commandSender: CommandSender, @Argument("amount") @Range(min = "1", max = "100") amountStr: Int) {
        val random = ThreadLocalRandom.current()

        val time = measureTimeMillis {
            for (i in 1 .. amountStr) {
                liteEco.databaseEcoModel.createPlayerAccount(getRandomString(10), UUID.randomUUID(), random.nextDouble(1000.0, 500000.0))
            }
        }

        commandSender.sendMessage("Into database was insterted $amountStr fake accounts in time $time ms")
    }

    @Command("eco reload")
    @Permission("lite.eco.admin.reload")
    fun onReload(commandSender: CommandSender) {
        liteEco.reloadConfig()
        commandSender.sendMessage(liteEco.locale.translation("messages.admin.config_reload"))
        liteEco.logger.info("Config.yml was reloaded [!]")
        liteEco.saveConfig()
        liteEco.locale.loadCurrentTranslation()
    }
}