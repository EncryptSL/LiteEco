package com.github.encryptsl.lite.eco.commands

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.enums.ExportKeys
import com.github.encryptsl.lite.eco.api.enums.PurgeKey
import com.github.encryptsl.lite.eco.api.objects.ModernText
import com.github.encryptsl.lite.eco.commands.parsers.AmountValidatorParser
import com.github.encryptsl.lite.eco.commands.parsers.CurrencyParser
import com.github.encryptsl.lite.eco.commands.parsers.ImportEconomyParser
import com.github.encryptsl.lite.eco.commands.parsers.LangParser
import com.github.encryptsl.lite.eco.common.manager.ExportManager
import com.github.encryptsl.lite.eco.common.manager.ImportManager
import com.github.encryptsl.lite.eco.common.manager.MonologManager
import com.github.encryptsl.lite.eco.common.manager.PurgeManager
import com.github.encryptsl.lite.eco.common.manager.economy.admin.EconomyGlobalDepositHandler
import com.github.encryptsl.lite.eco.common.manager.economy.admin.EconomyGlobalSetHandler
import com.github.encryptsl.lite.eco.common.manager.economy.admin.EconomyGlobalWithdrawHandler
import com.github.encryptsl.lite.eco.common.manager.economy.admin.EconomyMoneyDepositHandler
import com.github.encryptsl.lite.eco.common.manager.economy.admin.EconomyMoneySetHandler
import com.github.encryptsl.lite.eco.common.manager.economy.admin.EconomyMoneyWithdrawHandler
import com.github.encryptsl.lite.eco.common.manager.importer.ImportEconomy
import com.github.encryptsl.lite.eco.utils.Helper
import kotlinx.coroutines.launch
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.incendo.cloud.bukkit.data.MultiplePlayerSelector
import org.incendo.cloud.bukkit.parser.OfflinePlayerParser
import org.incendo.cloud.bukkit.parser.selector.MultiplePlayerSelectorParser
import org.incendo.cloud.component.DefaultValue
import org.incendo.cloud.description.Description
import org.incendo.cloud.kotlin.extension.buildAndRegister
import org.incendo.cloud.paper.PaperCommandManager
import org.incendo.cloud.paper.util.sender.Source
import org.incendo.cloud.parser.standard.BooleanParser
import org.incendo.cloud.parser.standard.IntegerParser
import org.incendo.cloud.parser.standard.StringParser
import java.math.BigDecimal

class EcoCMD(
    private val liteEco: LiteEco
) : InternalCmd {

    companion object {
        private const val DESCRIPTION = "Provided plugin by LiteEco"
    }

    private val helper: Helper = Helper(liteEco)
    private val importEconomy: ImportEconomy = ImportEconomy(liteEco)
    private val exportManager: ExportManager = ExportManager(liteEco)
    private val monologManager: MonologManager = MonologManager(liteEco, helper)
    private val importManager: ImportManager = ImportManager(liteEco, importEconomy)
    private val purgeManager: PurgeManager = PurgeManager(liteEco)

    private val globalEconomyDeposit by lazy { EconomyGlobalDepositHandler(liteEco) }
    private val globalEconomyWithdraw by lazy { EconomyGlobalWithdrawHandler(liteEco) }
    private val globalEconomySet by lazy { EconomyGlobalSetHandler(liteEco) }
    private val economyDeposit by lazy { EconomyMoneyDepositHandler(liteEco) }
    private val economySet by lazy { EconomyMoneySetHandler(liteEco) }
    private val economyWithdraw by lazy { EconomyMoneyWithdrawHandler(liteEco) }

    private val importEconomyParser by lazy { ImportEconomyParser(importEconomy) }

    override fun execute(commandManager: PaperCommandManager<Source>) {
        commandManager.buildAndRegister("eco", Description.description(DESCRIPTION)) {
            permission("lite.eco.admin.eco")
            commandManager.command(commandBuilder.literal("help").permission("lite.eco.admin.help").handler { ctx ->
                val sender: CommandSender = ctx.sender().source()
                liteEco.locale.translationList("messages.admin-help").forEach {
                    sender.sendMessage(it)
                }
            })
            commandManager.command(commandBuilder.literal("add")
                .commandDescription(Description.description(DESCRIPTION))
                .permission("lite.eco.admin.add")
                .required(
                    "target",
                    MultiplePlayerSelectorParser.multiplePlayerSelectorParser(),
                )
                .required(commandManager
                    .componentBuilder(BigDecimal::class.java, "amount")
                    .parser(AmountValidatorParser())
                    .defaultValue(DefaultValue.constant(BigDecimal.ONE))
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
                    val selector: MultiplePlayerSelector = ctx.get("target")
                    val amount: BigDecimal = ctx.get("amount")
                    val currency: String = ctx.get("currency")
                    val silent: Boolean = ctx.get("silent")

                    val players = selector.values()
                    val input = selector.inputString()
                    val isSelectorAndPlayersNotEmpty = input.startsWith("@") && players.isNotEmpty() && players.size > 1

                    if (isSelectorAndPlayersNotEmpty) {
                        if (!sender.hasPermission("lite.eco.admin.global.add")) return@handler
                        val offlinePlayers: MutableCollection<OfflinePlayer> = players.toMutableList()
                        globalEconomyDeposit.onAdminGlobalDepositMoney(sender, currency, amount, offlinePlayers)
                        return@handler
                    }

                    if (input.startsWith("@a") && players.isEmpty()) {
                        if (!sender.hasPermission("lite.eco.admin.global.add")) return@handler
                        val offlinePlayers: MutableCollection<OfflinePlayer> = Bukkit.getOfflinePlayers().toMutableList()
                        globalEconomyDeposit.onAdminGlobalDepositMoney(sender, currency, amount, offlinePlayers)
                        return@handler
                    }

                    if (!input.startsWith("@") || players.isEmpty()) {
                        val offlinePlayer = Bukkit.getOfflinePlayerIfCached(input)
                            ?: return@handler sender.sendMessage(liteEco.locale.translation("messages.error.account_not_exist",
                                Placeholder.parsed("account", input)))
                        economyDeposit.onAdminDepositMoney(sender, offlinePlayer, currency, amount, silent)
                        return@handler
                    }

                    economyDeposit.onAdminDepositMoney(sender, players.single(), currency, amount, silent)
                })

            commandManager.command(commandBuilder.literal("set")
                .commandDescription(Description.description(DESCRIPTION))
                .permission("lite.eco.admin.set")
                .required(
                    "target",
                    MultiplePlayerSelectorParser.multiplePlayerSelectorParser(),
                )
                .required(commandManager
                    .componentBuilder(BigDecimal::class.java, "amount")
                    .parser(AmountValidatorParser())
                    .defaultValue(DefaultValue.constant(BigDecimal.ONE))
                )
                .optional(commandManager
                    .componentBuilder(String::class.java, "currency")
                    .parser(CurrencyParser())
                    .defaultValue(DefaultValue.parsed("dollars"))
                ).handler { ctx ->
                    val sender: CommandSender = ctx.sender().source()
                    val selector: MultiplePlayerSelector = ctx.get("target")
                    val amount: BigDecimal = ctx.get("amount")
                    val currency: String = ctx.get("currency")

                    val players = selector.values()
                    val input = selector.inputString()
                    val isSelectorAndPlayersNotEmpty = input.startsWith("@") && players.isNotEmpty() && players.size > 1

                    if (isSelectorAndPlayersNotEmpty) {
                        if (!sender.hasPermission("lite.eco.admin.global.set")) return@handler
                        val offlinePlayers: MutableCollection<OfflinePlayer> = players.toMutableList()
                        globalEconomySet.onAdminGlobalSetMoney(sender, currency, amount, offlinePlayers)
                        return@handler
                    }

                    if (input.startsWith("@a") && players.isEmpty()) {
                        if (!sender.hasPermission("lite.eco.admin.global.set")) return@handler
                        val offlinePlayers: MutableCollection<OfflinePlayer> = Bukkit.getOfflinePlayers().toMutableList()
                        globalEconomySet.onAdminGlobalSetMoney(sender, currency, amount, offlinePlayers)
                        return@handler
                    }

                    if (!input.startsWith("@") || players.isEmpty()) {
                        val offlinePlayer = Bukkit.getOfflinePlayerIfCached(input)
                            ?: return@handler sender.sendMessage(liteEco.locale.translation("messages.error.account_not_exist",
                                Placeholder.parsed("account", input)))
                        economySet.onAdminSetMoney(sender, offlinePlayer, currency, amount)
                        return@handler
                    }

                    economySet.onAdminSetMoney(sender, players.single(), currency, amount)
                })
            commandManager.command(commandBuilder.literal("withdraw")
                .commandDescription(Description.description(DESCRIPTION))
                .permission("lite.eco.admin.withdraw")
                .required(
                    "target",
                    MultiplePlayerSelectorParser.multiplePlayerSelectorParser(),
                )
                .required(commandManager
                    .componentBuilder(BigDecimal::class.java, "amount")
                    .parser(AmountValidatorParser())
                    .defaultValue(DefaultValue.constant(BigDecimal.ONE))
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
                    val selector: MultiplePlayerSelector = ctx.get("target")
                    val amount: BigDecimal = ctx.get("amount")
                    val currency: String = ctx.get("currency")
                    val silent: Boolean = ctx.get("silent")

                    val players = selector.values()
                    val input = selector.inputString()
                    val isSelectorAndPlayersNotEmpty = input.startsWith("@") && players.isNotEmpty() && players.size > 1

                    if (isSelectorAndPlayersNotEmpty) {
                        if (!sender.hasPermission("lite.eco.admin.global.withdraw")) return@handler
                        val offlinePlayers: MutableCollection<OfflinePlayer> = players.toMutableList()
                        globalEconomyWithdraw.onAdminGlobalWithdrawMoney(sender, currency, amount, offlinePlayers)
                        return@handler
                    }

                    if (input.startsWith("@a") && players.isEmpty()) {
                        if (!sender.hasPermission("lite.eco.admin.global.withdraw")) return@handler
                        val offlinePlayers: MutableCollection<OfflinePlayer> = Bukkit.getOfflinePlayers().toMutableList()
                        globalEconomyWithdraw.onAdminGlobalWithdrawMoney(sender, currency, amount, offlinePlayers)
                        return@handler
                    }

                    if (!input.startsWith("@") || players.isEmpty()) {
                        val offlinePlayer = Bukkit.getOfflinePlayerIfCached(input)
                            ?: return@handler sender.sendMessage(liteEco.locale.translation("messages.error.account_not_exist",
                                Placeholder.parsed("account", input)))
                        economyWithdraw.onAdminWithdrawMoney(sender, offlinePlayer, currency, amount, silent)
                        return@handler
                    }

                    economyWithdraw.onAdminWithdrawMoney(sender, players.single(), currency, amount, silent)
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
                        val message = if (liteEco.api.createOrUpdateAccount(target.uniqueId, target.name.toString(), currency, amountStr.toBigDecimal())) {
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
                        val message = if (liteEco.api.deleteAccount(target.uniqueId, currency)) {
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
                        .componentBuilder(String::class.java, "economy")
                        .parser(importEconomyParser)
                )
                .optional(
                    commandManager
                        .componentBuilder(String::class.java, "currency")
                        .parser(CurrencyParser())
                        .defaultValue(DefaultValue.parsed("dollars"))
                ).handler { ctx ->
                    val sender: CommandSender = ctx.sender().source()
                    val economy: String = ctx.get("economy")
                    val currency: String = ctx.get("currency")
                    importManager.importEconomy(sender, economy, currency)
                })
            val configSubCommand = commandBuilder.literal("config", "c")

            commandManager.command(configSubCommand.literal("lang")
                .permission("lite.eco.admin.lang")
                .required(
                    commandManager
                        .componentBuilder(String::class.java, "isoKey")
                        .parser(LangParser(liteEco))
                ).handler { ctx ->
                    val sender: CommandSender = ctx.sender().source()
                    val lang: String = ctx.get("isoKey")

                    if (liteEco.locale.setLocale(lang)) {
                        sender.sendMessage(liteEco.locale
                            .translation("messages.admin.translation_switch", Placeholder.parsed("locale", lang))
                        )
                    }
                })
            commandManager.command(configSubCommand.literal("reload")
                .permission("lite.eco.admin.reload").handler { ctx ->
                    liteEco.reloadConfig()
                    liteEco.locale.reloadCurrentLocale()
                    ctx.sender().source().sendMessage(liteEco.locale.translation("messages.admin.config_reload"))
                    liteEco.componentLogger.info(ModernText.miniModernText("✅ config.yml & locale reloaded."))
                })
        }
    }

}