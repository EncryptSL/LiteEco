package com.github.encryptsl.lite.eco.commands

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.ComponentPaginator
import com.github.encryptsl.lite.eco.api.enums.CheckLevel
import com.github.encryptsl.lite.eco.api.enums.PurgeKey
import com.github.encryptsl.lite.eco.api.events.admin.EconomyGlobalDepositEvent
import com.github.encryptsl.lite.eco.api.events.admin.EconomyGlobalSetEvent
import com.github.encryptsl.lite.eco.api.events.admin.EconomyGlobalWithdrawEvent
import com.github.encryptsl.lite.eco.api.events.admin.EconomyMoneyDepositEvent
import com.github.encryptsl.lite.eco.api.events.admin.EconomyMoneySetEvent
import com.github.encryptsl.lite.eco.api.events.admin.EconomyMoneyWithdrawEvent
import com.github.encryptsl.lite.eco.api.migrator.entity.PlayerBalances
import com.github.encryptsl.lite.eco.api.objects.ModernText
import com.github.encryptsl.lite.eco.commands.parsers.ConvertEconomyParser
import com.github.encryptsl.lite.eco.commands.parsers.CurrencyParser
import com.github.encryptsl.lite.eco.commands.parsers.LangParser
import com.github.encryptsl.lite.eco.common.config.Locales
import com.github.encryptsl.lite.eco.common.extensions.positionIndexed
import com.github.encryptsl.lite.eco.common.manager.ImportManager
import com.github.encryptsl.lite.eco.common.manager.MonologManager
import com.github.encryptsl.lite.eco.common.manager.PurgeManager
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
import org.incendo.cloud.bukkit.parser.OfflinePlayerParser
import org.incendo.cloud.component.DefaultValue
import org.incendo.cloud.description.Description
import org.incendo.cloud.kotlin.extension.buildAndRegister
import org.incendo.cloud.paper.PaperCommandManager
import org.incendo.cloud.paper.util.sender.Source
import org.incendo.cloud.parser.standard.IntegerParser
import org.incendo.cloud.parser.standard.StringParser
import kotlin.system.measureTimeMillis

class EcoCMD(
    private val commandManager: PaperCommandManager<Source>,
    private val liteEco: LiteEco
) {

    companion object {
        private const val DESCRIPTION = "Provided plugin by LiteEco"
    }

    private val helper: Helper = Helper(liteEco)
    private val convertEconomy: ConvertEconomy = ConvertEconomy(liteEco)
    private val monologManager: MonologManager = MonologManager(liteEco, helper)
    private val importManager: ImportManager = ImportManager(liteEco, convertEconomy)
    private val purgeManager: PurgeManager = PurgeManager(liteEco)


    fun adminCommands() {
        commandManager.buildAndRegister("eco", Description.description(DESCRIPTION)) {
            commandBuilder.literal("help").permission("lite.eco.admin.help").handler { ctx ->
                val sender: CommandSender = ctx.sender().source()
                liteEco.locale.getList("messages.admin-help")?.forEach { s -> sender.sendMessage(ModernText.miniModernText(s.toString())) }
            }
            commandBuilder.literal("add")
                .commandDescription(Description.description(DESCRIPTION))
                .permission("lite.eco.admin.add")
                .required(
                    "target",
                    OfflinePlayerParser.offlinePlayerParser(),
                    commandManager.parserRegistry().getSuggestionProvider("players").get()
                )
                .required(
                    "amount",
                    IntegerParser.integerParser(1)
                )
                .optional(commandManager
                    .componentBuilder(String::class.java, "currency")
                    .parser(CurrencyParser())
                    .defaultValue(DefaultValue.parsed("dollars"))
                )
                .flag(commandManager
                    .flagBuilder("silent")
                    .withAliases("s")
                    .build()
                )
                .handler { ctx ->
                    val sender: CommandSender = ctx.sender().source()
                    val target: OfflinePlayer = ctx.get("target")
                    val amountStr: Integer = ctx.get("amount")
                    val currency: String = ctx.get("currency")
                    val silent: Boolean = ctx.flags().hasFlag("silent")

                    val amount = helper.validateAmount(amountStr.toString(), sender) ?: return@handler
                    liteEco.pluginManager.callEvent(EconomyMoneyDepositEvent(sender, target, currency, amount, silent))
                }
            commandBuilder.literal("global")
                .literal("add")
                .commandDescription(Description.description(DESCRIPTION))
                .permission("lite.eco.admin.global.add")
                .required(
                    "amount",
                    IntegerParser.integerParser(1)
                )
                .optional(commandManager
                    .componentBuilder(String::class.java, "currency")
                    .parser(CurrencyParser())
                    .defaultValue(DefaultValue.parsed("dollars"))
                ).handler { ctx ->
                    val sender: CommandSender = ctx.sender().source()
                    val amountStr: Integer = ctx.get("amount")
                    val currency: String = ctx.get("currency")

                    val amount = helper.validateAmount(amountStr.toString(), sender) ?: return@handler
                    liteEco.pluginManager.callEvent(EconomyGlobalDepositEvent(sender, currency, amount))
                }

            commandBuilder.literal("set")
                .commandDescription(Description.description(DESCRIPTION))
                .permission("lite.eco.admin.set")
                .required(
                    "target",
                    OfflinePlayerParser.offlinePlayerParser(),
                    commandManager.parserRegistry().getSuggestionProvider("players").get()
                )
                .required(
                    "amount",
                    IntegerParser.integerParser(1)
                )
                .optional(commandManager
                    .componentBuilder(String::class.java, "currency")
                    .parser(CurrencyParser())
                    .defaultValue(DefaultValue.parsed("dollars"))
                ).handler { ctx ->
                    val sender: CommandSender = ctx.sender().source()
                    val target: OfflinePlayer = ctx.get("target")
                    val amountStr: Integer = ctx.get("amount")
                    val currency: String = ctx.get("currency")

                    val amount = helper.validateAmount(amountStr.toString(), sender, CheckLevel.ONLY_NEGATIVE) ?: return@handler
                    liteEco.pluginManager.callEvent(EconomyMoneySetEvent(sender, target, currency, amount))
                }
            commandBuilder.literal("global")
                .literal("set")
                .commandDescription(Description.description(DESCRIPTION))
                .permission("lite.eco.admin.global.set")
                .required(
                    "amount",
                    IntegerParser.integerParser(1)
                )
                .optional(commandManager
                    .componentBuilder(String::class.java, "currency")
                    .parser(CurrencyParser())
                    .defaultValue(DefaultValue.parsed("dollars"))
                ).handler { ctx ->
                    val sender: CommandSender = ctx.sender().source()
                    val amountStr: Integer = ctx.get("amount")
                    val currency: String = ctx.get("currency")

                    val amount = helper.validateAmount(amountStr.toString(), sender, CheckLevel.ONLY_NEGATIVE) ?: return@handler
                    liteEco.pluginManager.callEvent(EconomyGlobalSetEvent(sender, currency, amount))
                }
            commandBuilder.literal("withdraw")
                .commandDescription(Description.description(DESCRIPTION))
                .permission("lite.eco.admin.withdraw")
                .required(
                    "target",
                    OfflinePlayerParser.offlinePlayerParser(),
                    commandManager.parserRegistry().getSuggestionProvider("players").get()
                )
                .required(
                    "amount",
                    IntegerParser.integerParser(1)
                )
                .optional(commandManager
                    .componentBuilder(String::class.java, "currency")
                    .parser(CurrencyParser())
                    .defaultValue(DefaultValue.parsed("dollars"))
                )
                .flag(commandManager
                    .flagBuilder("silent")
                    .withAliases("s")
                    .build()
                )
                .handler { ctx ->
                    val sender: CommandSender = ctx.sender().source()
                    val target: OfflinePlayer = ctx.get("target")
                    val amountStr: Integer = ctx.get("amount")
                    val currency: String = ctx.get("currency")
                    val silent: Boolean = ctx.flags().hasFlag("silent")

                    val amount = helper.validateAmount(amountStr.toString(), sender) ?: return@handler
                    liteEco.pluginManager.callEvent(EconomyMoneyWithdrawEvent(sender, target, currency, amount, silent))
                }
            commandBuilder.literal("global")
                .literal("withdraw")
                .commandDescription(Description.description(DESCRIPTION))
                .permission("lite.eco.admin.global.withdraw")
                .required(
                    "amount",
                    IntegerParser.integerParser(1)
                )
                .optional(commandManager
                    .componentBuilder(String::class.java, "currency")
                    .parser(CurrencyParser())
                    .defaultValue(DefaultValue.parsed("dollars"))
                ).handler { ctx ->
                    val sender: CommandSender = ctx.sender().source()
                    val amountStr: Integer = ctx.get("amount")
                    val currency: String = ctx.get("currency")

                    val amount = helper.validateAmount(amountStr.toString(), sender, CheckLevel.ONLY_NEGATIVE) ?: return@handler
                    liteEco.pluginManager.callEvent(EconomyGlobalWithdrawEvent(sender, currency, amount))
                }
            commandBuilder.literal("create")
                .commandDescription(Description.description(DESCRIPTION))
                .permission("lite.eco.admin.create")
                .required(
                    "target",
                    OfflinePlayerParser.offlinePlayerParser(),
                    commandManager.parserRegistry().getSuggestionProvider("players").get()
                )
                .required(
                    "amount",
                    IntegerParser.integerParser(1)
                )
                .optional(commandManager
                    .componentBuilder(String::class.java, "currency")
                    .parser(CurrencyParser())
                    .defaultValue(DefaultValue.parsed("dollars"))
                )
                .handler { ctx ->
                    val sender: CommandSender = ctx.sender().source()
                    val target: OfflinePlayer = ctx.get("target")
                    val amountStr: Int = ctx.get("amount")
                    val currency: String = ctx.get("currency")

                    val message = if (liteEco.api.createAccount(target, currency, amountStr.toBigDecimal())) {
                        "messages.admin.create_account"
                    } else {
                        "messages.error.account_now_exist"
                    }
                    sender.sendMessage(liteEco.locale.translation(message, Placeholder.parsed("account", target.name.toString())))
                }
            commandBuilder.literal("delete")
                .commandDescription(Description.description(DESCRIPTION))
                .permission("lite.eco.admin.delete")
                .required(
                    "target",
                    OfflinePlayerParser.offlinePlayerParser(),
                    commandManager.parserRegistry().getSuggestionProvider("players").get()
                )
                .optional(commandManager
                    .componentBuilder(String::class.java, "currency")
                    .parser(CurrencyParser())
                    .defaultValue(DefaultValue.parsed("dollars"))
                )
                .handler { ctx ->
                    val sender: CommandSender = ctx.sender().source()
                    val target: OfflinePlayer = ctx.get("target")
                    val currency: String = ctx.get("currency")

                    val message = if (liteEco.api.deleteAccount(target.uniqueId, currency)) {
                        "messages.admin.delete_account"
                    } else {
                        "messages.error.account_not_exist"
                    }
                    sender.sendMessage(liteEco.locale.translation(message, Placeholder.parsed("account", target.name.toString())))
                }
            commandBuilder.literal("monolog")
                .commandDescription(Description.description(DESCRIPTION))
                .permission("lite.eco.admin.monolog")
                .optional("page", IntegerParser.integerParser(1), DefaultValue.constant(1))
                .optional(
                    "target",
                    StringParser.stringParser(),
                    commandManager.parserRegistry().getSuggestionProvider("players").get()
                ).handler { ctx ->
                    val sender: CommandSender = ctx.sender().source()
                    val page: Int = ctx.get("page")
                    val target: String = ctx.getOrDefault("target", "all")
                    monologManager.displayMonolog(sender, target, page)
                }
            commandBuilder.literal("purge")
                .commandDescription(Description.description(DESCRIPTION))
                .permission("lite.eco.admin.purge")
                .required(commandManager
                    .componentBuilder(PurgeKey::class.java, "argument")
                    .suggestionProvider(commandManager.parserRegistry().getSuggestionProvider("purgeKeys").get())
                )
                .optional(commandManager
                    .componentBuilder(String::class.java, "currency")
                    .parser(CurrencyParser())
                    .defaultValue(DefaultValue.parsed("dollars"))
                )
                .handler { ctx ->
                    val sender: CommandSender = ctx.sender().source()
                    val purgeKey: PurgeKey = ctx.get("argument")
                    val currency: String = ctx.get("currency")
                    purgeManager.purge(sender, purgeKey, currency)
                }
            commandBuilder.literal("migration")
                .commandDescription(Description.description(DESCRIPTION))
                .permission("lite.eco.admin.migration")
                .required(commandManager
                    .componentBuilder(MigrationKey::class.java, "argument")
                    .suggestionProvider(commandManager.parserRegistry().getSuggestionProvider("migrationKeys").get())
                )
                .optional(commandManager
                    .componentBuilder(String::class.java, "currency")
                    .parser(CurrencyParser())
                    .defaultValue(DefaultValue.parsed("dollars"))
                )
                .handler { ctx ->
                    val sender: CommandSender = ctx.sender().source()
                    val migrationKey: MigrationKey = ctx.get("argument")
                    val currency: String = ctx.get("currency")

                    try {
                        runBlocking {
                            val timer = measureTimeMillis {
                                val migrationTool = MigrationTool(liteEco)

                                // This is better method to not crash server because data is exported from database and from game cache if is player connected and cached.
                                val output = liteEco.api.getUUIDNameMap(currency.lowercase()).toList().positionIndexed { index, pair ->
                                    PlayerBalances.PlayerBalance(index, pair.first, pair.second, liteEco.api.getBalance(pair.first))
                                }

                                val result = when(migrationKey) {
                                    MigrationKey.CSV -> migrationTool.getCSVFileExporter("economy_migration", currency.lowercase()).exportToCSVFile(output)
                                    MigrationKey.SQL -> migrationTool.getSQLFileExporter("economy_migration", currency.lowercase()).exportToSQLFile(output)
                                    MigrationKey.LEGACY_TABLE -> migrationTool.getLegacyTableExporter(currency.lowercase()).exportToLiteEcoDollarsTable()
                                    MigrationKey.SQL_LITE_FILE -> migrationTool.getSQLFileExporter("economy_migration_sql_lite", currency.lowercase()).exportToSQLFileLite(output)
                                }

                                val messageKey = if (result) {
                                    "messages.admin.migration_success"
                                } else {
                                    "messages.error.migration_failed"
                                }

                                sender.sendMessage(liteEco.locale.translation(messageKey,
                                    TagResolver.resolver(
                                        Placeholder.parsed("type", migrationKey.name),
                                        Placeholder.parsed("currency", currency)
                                    )
                                ))
                            }
                            liteEco.componentLogger.info(ModernText.miniModernText("Exporting of ${migrationKey.name} elapsed $timer ms"))
                        }
                    } catch (e : Exception) {
                        liteEco.componentLogger.error(ModernText.miniModernText(e.message ?: e.localizedMessage))
                    }
                }
            commandBuilder.literal("convert")
                .commandDescription(Description.description(DESCRIPTION))
                .permission("lite.eco.admin.convert")
                .required(
                    commandManager
                        .componentBuilder(Economies::class.java, "economy")
                        .parser(ConvertEconomyParser())
                )
                .optional(
                    commandManager
                        .componentBuilder(String::class.java, "currency")
                        .parser(CurrencyParser())
                        .defaultValue(DefaultValue.parsed("dollars"))
                ).handler { ctx ->
                    val sender: CommandSender = ctx.sender().source()
                    val economy: Economies = ctx.get("economy")
                    val currency: String = ctx.get("currency")
                    importManager.importEconomy(sender, economy, currency)
                }
            val configSubCommand = commandBuilder.literal("config")
            configSubCommand.literal("lang")
                .permission("lite.eco.admin.lang")
                .required(
                    commandManager
                        .componentBuilder(Locales.LangKey::class.java, "isoKey")
                        .parser(LangParser())
                ).handler { ctx ->
                    val sender: CommandSender = ctx.sender().source()
                    val lang: Locales.LangKey = ctx.get("isoKey")

                    liteEco.locale.setLocale(lang)
                    sender.sendMessage(liteEco.locale
                        .translation("messages.admin.translation_switch", Placeholder.parsed("locale", lang.name))
                    )
                }
            configSubCommand.literal("reload")
                .permission("lite.eco.admin.reload").handler { ctx ->
                    liteEco.reloadConfig()
                    ctx.sender().source().sendMessage(liteEco.locale.translation("messages.admin.config_reload"))
                    liteEco.componentLogger.info(ModernText.miniModernText("Config.yml was reloaded [!]"))
                    liteEco.saveConfig()
                    liteEco.locale.loadCurrentTranslation()
                }
        }
    }

}