package com.github.encryptsl.lite.eco.commands

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.enums.CheckLevel
import com.github.encryptsl.lite.eco.api.enums.ExportKeys
import com.github.encryptsl.lite.eco.api.enums.PurgeKey
import com.github.encryptsl.lite.eco.api.events.admin.*
import com.github.encryptsl.lite.eco.api.objects.ModernText
import com.github.encryptsl.lite.eco.commands.parsers.ConvertEconomyParser
import com.github.encryptsl.lite.eco.commands.parsers.CurrencyParser
import com.github.encryptsl.lite.eco.commands.parsers.LangParser
import com.github.encryptsl.lite.eco.common.config.Locales
import com.github.encryptsl.lite.eco.common.manager.ExportManager
import com.github.encryptsl.lite.eco.common.manager.ImportManager
import com.github.encryptsl.lite.eco.common.manager.MonologManager
import com.github.encryptsl.lite.eco.common.manager.PurgeManager
import com.github.encryptsl.lite.eco.utils.Helper
import com.github.encryptsl.lite.eco.utils.ImportEconomy
import com.github.encryptsl.lite.eco.utils.ImportEconomy.Economies
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.incendo.cloud.bukkit.parser.OfflinePlayerParser
import org.incendo.cloud.component.DefaultValue
import org.incendo.cloud.description.Description
import org.incendo.cloud.kotlin.extension.buildAndRegister
import org.incendo.cloud.paper.PaperCommandManager
import org.incendo.cloud.paper.util.sender.Source
import org.incendo.cloud.parser.standard.BooleanParser
import org.incendo.cloud.parser.standard.IntegerParser
import org.incendo.cloud.parser.standard.StringParser

class EcoCMD(
    private val commandManager: PaperCommandManager<Source>,
    private val liteEco: LiteEco
) {

    companion object {
        private const val DESCRIPTION = "Provided plugin by LiteEco"
    }

    private val helper: Helper = Helper(liteEco)
    private val importEconomy: ImportEconomy = ImportEconomy(liteEco)
    private val exportManager: ExportManager = ExportManager(liteEco)
    private val monologManager: MonologManager = MonologManager(liteEco, helper)
    private val importManager: ImportManager = ImportManager(liteEco, importEconomy)
    private val purgeManager: PurgeManager = PurgeManager(liteEco)


    @OptIn(DelicateCoroutinesApi::class)
    fun adminCommands() {
        commandManager.buildAndRegister("eco", Description.description(DESCRIPTION)) {
            commandManager.command(commandBuilder.literal("help").permission("lite.eco.admin.help").handler { ctx ->
                val sender: CommandSender = ctx.sender().source()
                liteEco.locale.getList("messages.admin-help")?.forEach { s -> sender.sendMessage(ModernText.miniModernText(s.toString())) }
            })
            commandManager.command(commandBuilder.literal("add")
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
                .optional(
                    "silent",
                    BooleanParser.booleanParser(false),
                    DefaultValue.constant(false)
                )
                .handler { ctx ->
                    val sender: CommandSender = ctx.sender().source()
                    val target: OfflinePlayer = ctx.get("target")
                    val amountStr: Integer = ctx.get("amount")
                    val currency: String = ctx.get("currency")
                    val silent: Boolean = ctx.get("silent")

                    val amount = helper.validateAmount(amountStr.toString(), sender) ?: return@handler
                    liteEco.pluginManager.callEvent(EconomyMoneyDepositEvent(sender, target, currency, amount, silent))
                })
            commandManager.command(commandBuilder.literal("global")
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
                })

            commandManager.command(commandBuilder.literal("set")
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
                })
            commandManager.command(commandBuilder.literal("global")
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
                })
            commandManager.command(commandBuilder.literal("withdraw")
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
                .optional(
                    "silent",
                    BooleanParser.booleanParser(false),
                    DefaultValue.constant(false)
                )
                .handler { ctx ->
                    val sender: CommandSender = ctx.sender().source()
                    val target: OfflinePlayer = ctx.get("target")
                    val amountStr: Integer = ctx.get("amount")
                    val currency: String = ctx.get("currency")
                    val silent: Boolean = ctx.get("silent")

                    val amount = helper.validateAmount(amountStr.toString(), sender) ?: return@handler
                    liteEco.pluginManager.callEvent(EconomyMoneyWithdrawEvent(sender, target, currency, amount, silent))
                })
            commandManager.command(commandBuilder.literal("global")
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
                })
            commandManager.command(commandBuilder.literal("create")
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

                    liteEco.pluginScope.launch {
                        val message = if (liteEco.suspendApiWrapper.createAccount(target, currency, amountStr.toBigDecimal())) {
                            "messages.admin.create_account"
                        } else {
                            "messages.error.account_now_exist"
                        }
                        sender.sendMessage(liteEco.locale.translation(message, Placeholder.parsed("account", target.name.toString())))
                    }
                })
            commandManager.command(commandBuilder.literal("delete")
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

                    liteEco.pluginScope.launch {
                        val message = if (liteEco.suspendApiWrapper.deleteAccount(target.uniqueId, currency)) {
                            "messages.admin.delete_account"
                        } else {
                            "messages.error.account_not_exist"
                        }
                        sender.sendMessage(liteEco.locale.translation(message, Placeholder.parsed("account", target.name.toString())))
                    }
                })
            commandManager.command(commandBuilder.literal("monolog")
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
                })
            val subCommandDatabase = commandBuilder.literal("database")
            commandManager.command(subCommandDatabase.literal("purge")
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
                })
            commandManager.command(subCommandDatabase.literal("export")
                .commandDescription(Description.description(DESCRIPTION))
                .permission("lite.eco.admin.export")
                .required(commandManager
                    .componentBuilder(ExportKeys::class.java, "argument")
                    .suggestionProvider(commandManager.parserRegistry().getSuggestionProvider("exportKeys").get())
                )
                .optional(commandManager
                    .componentBuilder(String::class.java, "currency")
                    .parser(CurrencyParser())
                    .defaultValue(DefaultValue.parsed("dollars"))
                )
                .handler { ctx ->
                    val sender: CommandSender = ctx.sender().source()
                    val exportKey: ExportKeys = ctx.get("argument")
                    val currency: String = ctx.get("currency")
                    exportManager.export(sender, exportKey, currency)
                })
            commandManager.command(subCommandDatabase.literal("import")
                .commandDescription(Description.description(DESCRIPTION))
                .permission("lite.eco.admin.import")
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
                })
            val configSubCommand = commandBuilder.literal("config", "c")

            commandManager.command(configSubCommand.literal("lang")
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
                })
            commandManager.command(configSubCommand.literal("reload")
                .permission("lite.eco.admin.reload").handler { ctx ->
                    liteEco.reloadConfig()
                    ctx.sender().source().sendMessage(liteEco.locale.translation("messages.admin.config_reload"))
                    liteEco.componentLogger.info(ModernText.miniModernText("Config.yml was reloaded [!]"))
                    liteEco.saveConfig()
                    liteEco.locale.loadCurrentTranslation()
                })
        }
    }

}