package encryptsl.cekuj.net.commands

import org.incendo.cloud.annotation.specifier.Range
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.CommandDescription
import org.incendo.cloud.annotations.Flag
import org.incendo.cloud.annotations.Permission
import encryptsl.cekuj.net.LiteEco
import encryptsl.cekuj.net.api.enums.*
import encryptsl.cekuj.net.api.events.admin.*
import encryptsl.cekuj.net.api.objects.ModernText
import encryptsl.cekuj.net.config.Locales
import encryptsl.cekuj.net.extensions.positionIndexed
import encryptsl.cekuj.net.utils.*
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
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

        liteEco.server.scheduler.runTask(liteEco) { ->
            liteEco.pluginManager.callEvent(EconomyMoneyDepositEvent(commandSender, offlinePlayer, amount, silent))
        }
    }

    @Command("eco gadd <amount>")
    @Permission("lite.eco.admin.gadd")
    fun onGlobalAddMoney(
        commandSender: CommandSender,
        @Argument("amount") @Range(min = "1.0", max = "") amountStr: String
    ) {
        val amount = helper.validateAmount(amountStr, commandSender) ?: return

        liteEco.server.scheduler.runTask(liteEco) { ->
            liteEco.pluginManager.callEvent(
                EconomyGlobalDepositEvent(commandSender, amount)
            )
        }
    }

    @Command("eco set <player> <amount>")
    @Permission("lite.eco.admin.set")
    fun onSetBalance(
        commandSender: CommandSender,
        @Argument(value = "player", suggestions = "players") offlinePlayer: OfflinePlayer,
        @Argument(value = "amount") amountStr: String
    ) {
        val amount = helper.validateAmount(amountStr, commandSender, CheckLevel.ONLY_NEGATIVE) ?: return

        liteEco.server.scheduler.runTask(liteEco) { ->
            liteEco.pluginManager.callEvent(
                EconomyMoneySetEvent(
                    commandSender,
                    offlinePlayer,
                    amount
                )
            )
        }
    }

    @Command("eco gset <amount>")
    @Permission("lite.eco.admin.gset")
    fun onGlobalSetMoney(
        commandSender: CommandSender,
        @Argument("amount") @Range(min = "1.0", max = "") amountStr: String
    ) {
        val amount = helper.validateAmount(amountStr, commandSender, CheckLevel.ONLY_NEGATIVE) ?: return

        liteEco.server.scheduler.runTask(liteEco) { ->
            liteEco.pluginManager.callEvent(
                EconomyGlobalSetEvent(commandSender, amount)
            )
        }
    }

    @Command("eco remove <player> <amount>")
    @Permission("lite.eco.admin.remove")
    fun onRemoveMoney(
        commandSender: CommandSender,
        @Argument(value = "player", suggestions = "players") offlinePlayer: OfflinePlayer,
        @Argument(value = "amount") @Range(min = "1.00", max = "") amountStr: String,
        @Flag(value = "silent", aliases = ["s"]) silent: Boolean
    ) {
        val amount = helper.validateAmount(amountStr, commandSender) ?: return

        liteEco.server.scheduler.runTask(liteEco) { ->
            liteEco.pluginManager.callEvent(
                EconomyMoneyWithdrawEvent(
                    commandSender,
                    offlinePlayer,
                    amount,
                    silent
                )
            )
        }
    }

    @Command("eco gremove <amount>")
    @Permission("lite.eco.admin.gremove")
    fun onGlobalRemoveMoney(
        commandSender: CommandSender,
        @Argument("amount") @Range(min = "1.0", max = "") amountStr: String
    ) {
        val amount = helper.validateAmount(amountStr, commandSender) ?: return

        liteEco.server.scheduler.runTask(liteEco) { ->
            liteEco.pluginManager.callEvent(
                EconomyGlobalWithdrawEvent(commandSender, amount)
            )
        }
    }

    @Command("eco lang <isoKey>")
    @Permission("lite.eco.admin.lang")
    fun onLangSwitch(
        commandSender: CommandSender,
        @Argument(value = "isoKey", suggestions = "langKeys") isoKey: String
    ) {
        try {
            val langKey = Locales.LangKey.valueOf(isoKey.uppercase())
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

    @Command("eco purge <argument>")
    @Permission("lite.eco.admin.purge")
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

    @Command("eco migration <argument>")
    @Permission("lite.eco.admin.migration")
    fun onMigration(commandSender: CommandSender, @Argument(value = "argument", suggestions = "migrationKeys") migrationKey: MigrationKey) {
        val migrationTool = MigrationTool(liteEco)
        val output = liteEco.api.getTopBalance().toList().positionIndexed { index, k -> MigrationData(index, Bukkit.getOfflinePlayer(k.first).name.toString(), k.first, k.second) }

        val result = when(migrationKey) {
            MigrationKey.CSV -> migrationTool.migrateToCSV(output, "economy_migration")
            MigrationKey.SQL -> migrationTool.migrateToSQL(output, "economy_migration")
        }

        val messageKey = if (result) {
            "messages.admin.migration_success"
        } else {
            "messages.error.migration_failed"
        }

        commandSender.sendMessage(ModernText.miniModernText(
            liteEco.locale.getMessage(messageKey),
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
            }
            val (converted, balances) = convertEconomy.getResult()
            commandSender.sendMessage(ModernText.miniModernText(
                liteEco.locale.getMessage("messages.admin.convert_success"),
                TagResolver.resolver(
                    Placeholder.parsed("economy", economy.name),
                    Placeholder.parsed("converted", converted.toString()),
                    Placeholder.parsed("balances", balances.toString())
                )
            ))
            convertEconomy.convertRefresh()
        } catch (e : Exception) {
            liteEco.logger.info(e.message ?: e.localizedMessage)
            commandSender.sendMessage(ModernText.miniModernText(liteEco.locale.getMessage("messages.error.convert_fail")))
        }
    }

    @Command("eco debug create accounts <amount>")
    @Permission("lite.eco.admin.debug.create.accounts")
    fun onDebugCreateAccounts(commandSender: CommandSender, @Argument("amount") @Range(min = "1", max = "100") amountStr: Int) {

        val randomName = StringGenerator()
        val random = ThreadLocalRandom.current()

        val time = measureTimeMillis {
            for (i in 1 .. amountStr) {
                liteEco.preparedStatements.createPlayerAccount(randomName.getRandomString(10), UUID.randomUUID(), random.nextDouble(1000.0, 500000.0))
            }
        }

        commandSender.sendMessage("Into database was insterted $amountStr fake accounts in time $time ms")
    }

    @Command("eco reload")
    @Permission("lite.eco.admin.reload")
    fun onReload(commandSender: CommandSender) {
        liteEco.reloadConfig()
        commandSender.sendMessage(ModernText.miniModernText(liteEco.locale.getMessage("messages.admin.config_reload")))
        liteEco.logger.info("Config.yml was reloaded [!]")
        liteEco.saveConfig()
        liteEco.locale.reloadTranslation()
    }
}