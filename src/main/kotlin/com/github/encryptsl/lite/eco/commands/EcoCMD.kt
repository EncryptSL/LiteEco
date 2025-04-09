package com.github.encryptsl.lite.eco.commands

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.ComponentPaginator
import com.github.encryptsl.lite.eco.api.enums.CheckLevel
import com.github.encryptsl.lite.eco.api.enums.PurgeKey
import com.github.encryptsl.lite.eco.api.events.admin.*
import com.github.encryptsl.lite.eco.api.migrator.entity.PlayerBalances
import com.github.encryptsl.lite.eco.api.objects.ModernText
import com.github.encryptsl.lite.eco.common.config.Locales
import com.github.encryptsl.lite.eco.common.extensions.getRandomString
import com.github.encryptsl.lite.eco.common.extensions.positionIndexed
import com.github.encryptsl.lite.eco.utils.ConvertEconomy
import com.github.encryptsl.lite.eco.utils.ConvertEconomy.Economies
import com.github.encryptsl.lite.eco.utils.Helper
import com.github.encryptsl.lite.eco.utils.MigrationTool
import com.github.encryptsl.lite.eco.utils.MigrationTool.MigrationKey
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.incendo.cloud.annotation.specifier.Range
import org.incendo.cloud.annotations.*
import java.math.BigDecimal
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

    @Command("eco add <player> <amount> [currency]")
    @Permission("lite.eco.admin.add")
    fun onAddMoney(
        commandSender: CommandSender,
        @Argument(value = "player", suggestions = "players") offlinePlayer: OfflinePlayer,
        @Argument(value = "amount") @Range(min = "1.00", max = "") amountStr: String,
        @Argument(value = "currency", suggestions = "currencies") @Default("dollars") currency: String,
        @Flag(value = "silent", aliases = ["s"]) silent: Boolean
    ) {
        val amount = helper.validateAmount(amountStr, commandSender) ?: return
        liteEco.pluginManager.callEvent(EconomyMoneyDepositEvent(commandSender, offlinePlayer, currency, amount, silent))
    }

    @Command("eco global add <amount> [currency]")
    @Permission("lite.eco.admin.gadd", "lite.eco.admin.global.add")
    fun onGlobalAddMoney(
        commandSender: CommandSender,
        @Argument("amount") @Range(min = "1.0", max = "") amountStr: String,
        @Argument(value = "currency", suggestions = "currencies") @Default("dollars") currency: String,
    ) {
        val amount = helper.validateAmount(amountStr, commandSender) ?: return
        liteEco.pluginManager.callEvent(EconomyGlobalDepositEvent(commandSender, currency, amount))
    }

    @Command("eco set <player> [amount] [currency]")
    @Permission("lite.eco.admin.set")
    fun onSetBalance(
        commandSender: CommandSender,
        @Argument(value = "player", suggestions = "players") offlinePlayer: OfflinePlayer,
        @Default("0.00") @Argument(value = "amount") amountStr: String,
        @Argument(value = "currency", suggestions = "currencies") @Default("dollars") currency: String,
    ) {
        val amount = helper.validateAmount(amountStr, commandSender, CheckLevel.ONLY_NEGATIVE) ?: return
        liteEco.pluginManager.callEvent(EconomyMoneySetEvent(commandSender, offlinePlayer, currency, amount))
    }

    @Command("eco global set <amount> [currency]")
    @Permission("lite.eco.admin.gset", "lite.eco.admin.global.withdraw")
    fun onGlobalSetMoney(
        commandSender: CommandSender,
        @Argument("amount") @Range(min = "1.0", max = "") amountStr: String,
        @Argument(value = "currency", suggestions = "currencies") @Default("dollars") currency: String,
    ) {
        val amount = helper.validateAmount(amountStr, commandSender, CheckLevel.ONLY_NEGATIVE) ?: return
        liteEco.pluginManager.callEvent(EconomyGlobalSetEvent(commandSender, currency, amount))
    }

    @Command("eco withdraw <player> <amount> [currency]")
    @Permission("lite.eco.admin.withdraw", "lite.eco.admin.remove")
    fun onWithdrawMoney(
        commandSender: CommandSender,
        @Argument(value = "player", suggestions = "players") offlinePlayer: OfflinePlayer,
        @Argument(value = "amount") @Range(min = "1.00", max = "") amountStr: String,
        @Argument(value = "currency", suggestions = "currencies") @Default("dollars") currency: String,
        @Flag(value = "silent", aliases = ["s"]) silent: Boolean
    ) {
        val amount = helper.validateAmount(amountStr, commandSender) ?: return
        liteEco.pluginManager.callEvent(EconomyMoneyWithdrawEvent(commandSender, offlinePlayer, currency, amount, silent))
    }

    @Command("eco global withdraw <amount> [currency]")
    @Permission("lite.eco.admin.gremove", "lite.eco.admin.global.withdraw")
    fun onGlobalWithdrawMoney(
        commandSender: CommandSender,
        @Argument("amount") @Range(min = "1.0", max = "") amountStr: String,
        @Argument("currency", suggestions = "currencies") @Default("dollars") currency: String
    ) {
        val amount = helper.validateAmount(amountStr, commandSender) ?: return

        liteEco.pluginManager.callEvent(
            EconomyGlobalWithdrawEvent(commandSender, currency, amount)
        )
    }

    @Command("eco create <player> [amount] [currency]")
    @Permission("lite.eco.admin.create")
    fun onCreateWalletAccount(
        commandSender: CommandSender,
        @Argument("player", suggestions = "players") offlinePlayer: OfflinePlayer,
        @Argument("amount") @Default("30.00") amount: Double,
        @Argument(value = "currency", suggestions = "currencies") @Default("dollars") currency: String
    ) {
        val message = if (liteEco.api.createAccount(offlinePlayer, currency, amount.toBigDecimal())) {
            "messages.admin.create_account"
        } else {
            "messages.error.account_now_exist"
        }
        commandSender.sendMessage(liteEco.locale.translation(message, Placeholder.parsed("account", offlinePlayer.name.toString())))
    }

    @Command("eco delete <player> [currency]")
    @Permission("lite.eco.admin.delete")
    fun onDeleteWalletAccount(
        commandSender: CommandSender,
        @Argument("player", suggestions = "players") offlinePlayer: OfflinePlayer,
        @Argument(value = "currency", suggestions = "currencies") @Default("dollars") currency: String
    ) {
        val message = if (liteEco.api.deleteAccount(offlinePlayer.uniqueId, currency)) {
            "messages.admin.delete_account"
        } else {
            "messages.error.account_not_exist"
        }
        commandSender.sendMessage(liteEco.locale.translation(message, Placeholder.parsed("account", offlinePlayer.name.toString())))
    }

    @Command("eco monolog [page] [player]")
    @Permission("lite.eco.admin.monolog")
    fun onLogView(commandSender: CommandSender, @Argument("page") @Default(value = "1") page: Int, @Argument("player") player: String?) {

        val log = helper.validateLog(player)
        val pagination = ComponentPaginator(log) { itemsPerPage = 10 }.apply { page(page) }

        if (pagination.isAboveMaxPage(page))
            return commandSender.sendMessage(liteEco.locale.translation("messages.error.maximum_page",
                Placeholder.parsed("max_page", pagination.maxPages.toString()))
            )

        for (content in pagination.display()) {
            commandSender.sendMessage(content)
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

    @Command("eco purge <argument> [currency]")
    @Permission("lite.eco.admin.purge")
    fun onPurge(
        commandSender: CommandSender,
        @Argument(value = "argument", suggestions = "purgeKeys") purgeKey: PurgeKey,
        @Argument(value = "currency", suggestions = "currencies") @Default("dollars") currency: String
    ) {
        @Suppress("REDUNDANT_ELSE_IN_WHEN")
        when (purgeKey) {
            PurgeKey.ACCOUNTS -> {
                liteEco.databaseEcoModel.purgeAccounts(currency)
                commandSender.sendMessage(liteEco.locale.translation("messages.admin.purge_accounts"))
            }
            PurgeKey.NULL_ACCOUNTS -> {
                liteEco.databaseEcoModel.purgeInvalidAccounts(currency)
                commandSender.sendMessage(liteEco.locale.translation("messages.admin.purge_null_accounts"))
            }
            PurgeKey.DEFAULT_ACCOUNTS -> {
                liteEco.databaseEcoModel.purgeDefaultAccounts(liteEco.currencyImpl.getCurrencyStartBalance(currency), currency)
                commandSender.sendMessage(liteEco.locale.translation("messages.admin.purge_default_accounts"))
            }
            PurgeKey.MONO_LOG -> {
                val monolog = liteEco.loggerModel.getLog()
                if (monolog.isEmpty()) {
                    return commandSender.sendMessage(liteEco.locale.translation("messages.error.purge_monolog_fail"))
                }
                liteEco.loggerModel.clearLogs()
                commandSender.sendMessage(liteEco.locale.translation("messages.admin.purge_monolog_success", Placeholder.parsed("deleted", monolog.size.toString())))
            }
            else -> {
                commandSender.sendMessage(liteEco.locale.translation("messages.error.purge_argument"))
            }
        }
    }

    @Command("eco migration <argument> [currency]")
    @Permission("lite.eco.admin.migration")
    fun onMigration(
        commandSender: CommandSender,
        @Argument(value = "argument", suggestions = "migrationKeys") migrationKey: MigrationKey,
        @Argument(value = "currency", suggestions = "currencies") @Default("dollars") currency: String
    ) {
        try {
            runBlocking {
                val timer = measureTimeMillis {
                    val migrationTool = MigrationTool(liteEco)

                    // This is better method to not crash server because data is exported from database and from game cache if is player connected and cached.
                    val output = liteEco.api.getUUIDNameMap(currency.lowercase()).toList().positionIndexed { index, pair ->
                        PlayerBalances.PlayerBalance(index, pair.first, pair.second, liteEco.api.getBalance(pair.first))
                    }

                    val result = when(migrationKey) {
                        MigrationKey.CSV -> migrationTool.getCSVFileExporter("economy_migration", currency.lowercase()).exportToCsvFile(output)
                        MigrationKey.SQL -> migrationTool.getSQLFileExporter("economy_migration", currency.lowercase()).exportToSQLFile(output)
                        MigrationKey.LEGACY_TABLE -> migrationTool.getLegacyTableExporter(currency.lowercase()).exportToLiteEcoDollarsTable()
                        MigrationKey.SQL_LITE_FILE -> migrationTool.getSQLFileExporter("economy_migration_sql_lite", currency.lowercase()).exportToSQLFileLite(output)
                    }

                    val messageKey = if (result) {
                        "messages.admin.migration_success"
                    } else {
                        "messages.error.migration_failed"
                    }

                    commandSender.sendMessage(liteEco.locale.translation(messageKey,
                        TagResolver.resolver(
                            Placeholder.parsed("type", migrationKey.name),
                            Placeholder.parsed("currency", currency)
                        )
                    ))
                }
                liteEco.logger.info("Exporting of ${migrationKey.name} elapsed $timer ms")
            }
        } catch (e : Exception) {
            liteEco.logger.severe(e.message ?: e.localizedMessage)
        }
    }

    @Command("eco convert <economy> [currency]")
    @Permission("lite.eco.admin.convert")
    fun onEconomyConvert(
        commandSender: CommandSender,
        @Argument("economy", suggestions = "economies") economy: Economies,
        @Argument(value = "currency", suggestions = "currencies") @Default("dollars") currency: String
    ) {
        try {
            when (economy) {
                Economies.EssentialsX -> {
                    convertEconomy.convertEssentialsXEconomy(currency)
                }

                Economies.BetterEconomy -> {
                    convertEconomy.convertBetterEconomy(currency)
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

    @Command("eco debug create accounts <amount> [currency]")
    @Permission("lite.eco.admin.debug.create.accounts")
    fun onDebugCreateAccounts(
        commandSender: CommandSender,
        @Argument("amount") @Range(min = "1", max = "100") amountStr: Int,
        @Argument(value = "currency", suggestions = "currencies") @Default("dollars") currency: String
    ) {
        val random = ThreadLocalRandom.current()

        val time = measureTimeMillis {
            for (i in 1 .. amountStr) {
                liteEco.databaseEcoModel.createPlayerAccount(getRandomString(10), UUID.randomUUID(), currency, BigDecimal.valueOf(random.nextDouble(1000.0, 500000.0)))
            }
        }

        commandSender.sendMessage("Into database was insterted $amountStr fake accounts in time $time ms")
    }

    @Command("eco debug database accounts [currency]")
    @Permission("lite.eco.admin.debug.database.accounts")
    fun onDebugDatabaseAccounts(
        commandSender: CommandSender,
        @Argument(value = "currency", suggestions = "currencies") @Default("dollars") currency: String
    ) {
        val data = liteEco.databaseEcoModel.getTopBalance(currency)

        commandSender.sendMessage("Accounts list ${data.entries.map { k -> k.key }}")
        commandSender.sendMessage("Accounts balances ${data.entries.sumOf { it.value.money }}")
        commandSender.sendMessage("Accounts in database ${data.size}")
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