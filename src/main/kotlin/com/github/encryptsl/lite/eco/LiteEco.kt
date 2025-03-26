package com.github.encryptsl.lite.eco

import com.github.encryptsl.lite.eco.api.ConfigAPI
import com.github.encryptsl.lite.eco.api.UpdateNotifier
import com.github.encryptsl.lite.eco.api.economy.Currency
import com.github.encryptsl.lite.eco.api.economy.ModernLiteEcoEconomyImpl
import com.github.encryptsl.lite.eco.api.enums.PurgeKey
import com.github.encryptsl.lite.eco.api.objects.ModernText
import com.github.encryptsl.lite.eco.commands.EcoCMD
import com.github.encryptsl.lite.eco.commands.MoneyCMD
import com.github.encryptsl.lite.eco.common.config.Locales
import com.github.encryptsl.lite.eco.common.database.DatabaseConnector
import com.github.encryptsl.lite.eco.common.database.models.DatabaseEcoModel
import com.github.encryptsl.lite.eco.common.database.models.DatabaseMonologModel
import com.github.encryptsl.lite.eco.common.hook.HookManager
import com.github.encryptsl.lite.eco.listeners.*
import com.github.encryptsl.lite.eco.listeners.admin.*
import com.github.encryptsl.lite.eco.utils.ConvertEconomy.Economies
import com.github.encryptsl.lite.eco.utils.MigrationTool
import com.tchristofferson.configupdater.ConfigUpdater
import org.bstats.bukkit.Metrics
import org.bstats.charts.SingleLineChart
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.plugin.PluginManager
import org.bukkit.plugin.java.JavaPlugin
import org.incendo.cloud.SenderMapper
import org.incendo.cloud.annotations.AnnotationParser
import org.incendo.cloud.bukkit.CloudBukkitCapabilities
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.minecraft.extras.MinecraftExceptionHandler
import org.incendo.cloud.paper.LegacyPaperCommandManager
import org.incendo.cloud.suggestion.Suggestion
import java.io.File
import java.util.concurrent.CompletableFuture
import kotlin.system.measureTimeMillis

class LiteEco : JavaPlugin() {
    companion object {
        const val CONFIG_VERSION = "1.2.6"
        const val LANG_VERSION = "2.0.3"
        const val PAPI_VERSION = "2.0.5"

        lateinit var instance: LiteEco
            private set
    }

    val pluginManager: PluginManager = server.pluginManager

    private var countTransactions: LinkedHashMap<String, Int> = LinkedHashMap()

    val api: ModernLiteEcoEconomyImpl by lazy { ModernLiteEcoEconomyImpl() }
    val locale: Locales by lazy { Locales(this, LANG_VERSION) }
    val databaseEcoModel: DatabaseEcoModel by lazy { DatabaseEcoModel() }
    val loggerModel: DatabaseMonologModel by lazy { DatabaseMonologModel(this) }
    val currencyImpl: Currency by lazy { Currency(this) }
    val databaseConnector: DatabaseConnector by lazy { DatabaseConnector(this) }

    private val configAPI: ConfigAPI by lazy { ConfigAPI(this) }
    private val hookManager: HookManager by lazy { HookManager(this) }
    private val configFile = File(dataFolder, "config.yml")

    override fun onLoad() {
        instance = this

        configAPI
            .create("database.db")
            .createConfig("config.yml", CONFIG_VERSION)
        locale
            .loadCurrentTranslation()
        databaseConnector.load()
    }

    override fun onEnable() {
        val timeTaken = measureTimeMillis {
            blockPlugins()
            hookRegistration()
            setupMetrics()
            checkUpdates()
            registerCommands()
            registerListeners()
        }
        try {
            ConfigUpdater.update(this, "config.yml", configFile, "plugin", "economy")
            logger.info("Config was updated on current version !")
        } catch (e : Exception) {
            logger.severe(e.message ?: e.localizedMessage)
        }
        logger.info("Plugin enabled in time $timeTaken ms")
    }

    override fun onDisable() {
        api.syncAccounts()
        logger.info("Plugin is disabled")
    }

    fun increaseTransactions(value: Int) {
        countTransactions["transactions"] = countTransactions.getOrDefault("transactions", 0) + value
    }

    private fun blockPlugins() {
        hookManager.blockPlugin("Towny", "Treasury")
    }

    private fun hookRegistration() {
        hookManager.hookPAPI()
        hookManager.hookMiniPlaceholders()
        hookManager.hookVault()
        hookManager.hookBetterEconomy()
        hookManager.hookTreasury() // Experimental support for this api.
    }

    private fun setupMetrics() {
        if (config.getBoolean("plugin.metrics", true)) {
            val metrics = Metrics(this, 15144)
            metrics.addCustomChart(SingleLineChart("transactions") {
                countTransactions["transactions"]
            })
        }
    }

    @Suppress("UnstableApiUsage")
    private fun checkUpdates() {
        val updateNotifier = UpdateNotifier(this,"101934", pluginMeta.version)
        updateNotifier.makeUpdateCheck()
    }

    private fun registerListeners() {
        val listeners = arrayOf(
            AccountManageListener(this),
            PlayerEconomyPayListener(this),
            EconomyGlobalDepositListener(this),
            EconomyGlobalSetListener(this),
            EconomyGlobalWithdrawListener(this),
            EconomyMoneyDepositListener(this),
            EconomyMoneyWithdrawListener(this),
            EconomyMoneySetListener(this),
            PlayerLoginListener(this),
            PlayerJoinListener(this),
            PlayerQuitListener(this)
        )
        val timeTaken = measureTimeMillis {
            for (listener in listeners) {
                pluginManager.registerEvents(listener, this)
                logger.info("Bukkit Listener ${listener.javaClass.simpleName} registered () -> ok")
            }
        }
        logger.info("Listeners registered (${listeners.size}) in time $timeTaken ms -> ok")
    }

    private fun registerCommands() {
        logger.info("Registering commands with Cloud Command Framework !")

        val commandManager = createCommandManager()

        registerMinecraftExceptionHandler(commandManager)
        registerSuggestionProviders(commandManager)

        val annotationParser = createAnnotationParser(commandManager)
        annotationParser.parse(MoneyCMD(this))
        annotationParser.parse(EcoCMD(this))
    }

    private fun createCommandManager(): LegacyPaperCommandManager<CommandSender> {
        val commandManager = LegacyPaperCommandManager(
            this,
            ExecutionCoordinator.builder<CommandSender>().build(),
            SenderMapper.identity()
        )
        if (commandManager.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
            commandManager.registerBrigadier()
            commandManager.brigadierManager().setNativeNumberSuggestions(false)
        } else if (commandManager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            (commandManager as LegacyPaperCommandManager<*>).registerAsynchronousCompletions()
        }
        return commandManager
    }

    private fun registerMinecraftExceptionHandler(commandManager: LegacyPaperCommandManager<CommandSender>) {
        MinecraftExceptionHandler.createNative<CommandSender>()
            .defaultHandlers()
            .decorator { component ->
                ModernText.miniModernText(config.getString("plugin.prefix", "<red>[!]").toString())
                    .appendSpace()
                    .append(component)
            }
            .registerTo(commandManager)
    }

    private fun registerSuggestionProviders(commandManager: LegacyPaperCommandManager<CommandSender>) {
        commandManager.parserRegistry().registerSuggestionProvider("players") { _, _ ->
            modifiableSuggestionPlayerSuggestion()
        }
        commandManager.parserRegistry().registerSuggestionProvider("currencies") { commandSender, _ ->
            CompletableFuture.completedFuture(currencyImpl.getCurrenciesKeys().filter {
                commandSender.hasPermission("lite.eco.balance.$it") || commandSender.hasPermission("lite.eco.balance.*")
            }.map { Suggestion.suggestion(it) })
        }
        commandManager.parserRegistry().registerSuggestionProvider("economies") {_, _ ->
            CompletableFuture.completedFuture(Economies.entries.map { Suggestion.suggestion(it.name) })
        }
        commandManager.parserRegistry().registerSuggestionProvider("langKeys") { _, _ ->
            CompletableFuture.completedFuture(Locales.LangKey.entries.map { Suggestion.suggestion(it.name) })
        }
        commandManager.parserRegistry().registerSuggestionProvider("purgeKeys") { _, _ ->
            CompletableFuture.completedFuture(PurgeKey.entries.map { Suggestion.suggestion(it.name) })
        }
        commandManager.parserRegistry().registerSuggestionProvider("migrationKeys") { _, _ ->
            CompletableFuture.completedFuture(MigrationTool.MigrationKey.entries.map { Suggestion.suggestion(it.name) })
        }
    }

    private fun modifiableSuggestionPlayerSuggestion(): CompletableFuture<List<Suggestion>> {
        val suggestion = if (config.getBoolean("plugin.offline-suggestion-players", true)) {
            CompletableFuture.completedFuture(Bukkit.getOfflinePlayers()
                .map { Suggestion.suggestion(it.name.toString()) })
        } else {
            CompletableFuture.completedFuture(Bukkit.getOnlinePlayers().map { Suggestion.suggestion(it.name) })
        }

        return suggestion
    }

    private fun createAnnotationParser(commandManager: LegacyPaperCommandManager<CommandSender>): AnnotationParser<CommandSender> {
        return AnnotationParser(commandManager, CommandSender::class.java)
    }
}