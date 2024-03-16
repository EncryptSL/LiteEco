package com.github.encryptsl.lite.eco

import org.incendo.cloud.SenderMapper
import org.incendo.cloud.annotations.AnnotationParser
import org.incendo.cloud.bukkit.CloudBukkitCapabilities
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.paper.PaperCommandManager
import org.incendo.cloud.suggestion.Suggestion
import com.github.encryptsl.lite.eco.api.ConfigAPI
import com.github.encryptsl.lite.eco.api.UpdateNotifier
import com.github.encryptsl.lite.eco.api.economy.LiteEcoEconomyAPI
import com.github.encryptsl.lite.eco.api.enums.Economies
import com.github.encryptsl.lite.eco.api.enums.MigrationKey
import com.github.encryptsl.lite.eco.api.enums.PurgeKey
import com.github.encryptsl.lite.eco.commands.EcoCMD
import com.github.encryptsl.lite.eco.commands.MoneyCMD
import com.github.encryptsl.lite.eco.common.config.Locales
import com.github.encryptsl.lite.eco.common.database.DatabaseConnector
import com.github.encryptsl.lite.eco.common.database.models.PreparedStatements
import com.github.encryptsl.lite.eco.common.hook.HookManager
import com.github.encryptsl.lite.eco.listeners.*
import com.github.encryptsl.lite.eco.listeners.admin.*
import org.bstats.bukkit.Metrics
import org.bstats.charts.SingleLineChart
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.plugin.PluginManager
import org.bukkit.plugin.java.JavaPlugin
import java.util.concurrent.CompletableFuture
import kotlin.system.measureTimeMillis

class LiteEco : JavaPlugin() {
    companion object {
        const val CONFIG_VERSION = "1.2.3"
        const val LANG_VERSION = "2.0.2"
        const val PAPI_VERSION = "2.0.5"
    }

    val pluginManager: PluginManager = server.pluginManager

    var countTransactions: LinkedHashMap<String, Int> = LinkedHashMap()

    val api: LiteEcoEconomyAPI by lazy { LiteEcoEconomyAPI(this) }
    val locale: Locales by lazy { Locales(this, LANG_VERSION) }
    val preparedStatements: PreparedStatements by lazy { PreparedStatements() }

    private val configAPI: com.github.encryptsl.lite.eco.api.ConfigAPI by lazy {
        com.github.encryptsl.lite.eco.api.ConfigAPI(
            this
        )
    }
    private val hookManager: HookManager by lazy { HookManager(this) }

    override fun onLoad() {
        configAPI
            .create("database.db")
            .createConfig("config.yml", CONFIG_VERSION)
        locale
            .reloadTranslation()
        DatabaseConnector(this)
            .initConnect(
                config.getString("database.connection.jdbc_url") ?: "jdbc:sqlite:plugins/LiteEco/database.db",
                config.getString("database.connection.username") ?: "root",
                config.getString("database.connection.password") ?: "admin"
            )
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
        hookManager.blockPlugin("Treasury")
        hookManager.blockPlugin("Towny")
    }

    private fun hookRegistration() {
        hookManager.hookPAPI()
        hookManager.hookVault()
        hookManager.hookTreasury() // Not supported : EncryptSL
    }

    private fun setupMetrics() {
        val metrics = Metrics(this, 15144)
        metrics.addCustomChart(SingleLineChart("transactions") {
            countTransactions["transactions"]
        })
    }

    @Suppress("UnstableApiUsage")
    private fun checkUpdates() {
        val updateNotifier = UpdateNotifier(this,"101934", pluginMeta.version)
        updateNotifier.makeUpdateCheck()
    }

    private fun registerListeners() {
        var amount: Int
        val timeTaken = measureTimeMillis {
            val listeners = arrayListOf(
                AccountManageListener(this),
                PlayerEconomyPayListener(this),
                EconomyGlobalDepositListener(this),
                EconomyGlobalSetListener(this),
                EconomyGlobalWithdrawListener(this),
                EconomyMoneyDepositListener(this),
                EconomyMoneyWithdrawListener(this),
                EconomyMoneySetListener(this),
                PlayerJoinListener(this),
                PlayerQuitListener(this)
            )
            for (listener in listeners) {
                pluginManager.registerEvents(listener, this)
                logger.info("Bukkit Listener ${listener.javaClass.simpleName} registered () -> ok")
            }
            amount = listeners.size
        }
        logger.info("Listeners registered ($amount) in time $timeTaken ms -> ok")
    }

    private fun registerCommands() {
        logger.info("Registering commands with Cloud Command Framework !")

        val commandManager = createCommandManager()

        registerSuggestionProviders(commandManager)

        val annotationParser = createAnnotationParser(commandManager)
        annotationParser.parse(MoneyCMD(this))
        annotationParser.parse(EcoCMD(this))
    }

    private fun createCommandManager(): PaperCommandManager<CommandSender> {
        val executionCoordinatorFunction = ExecutionCoordinator.builder<CommandSender>().build()
        val mapperFunction = SenderMapper.identity<CommandSender>()

        val commandManager = PaperCommandManager(
            this,
            executionCoordinatorFunction,
            mapperFunction
        )
        if (commandManager.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
            commandManager.registerBrigadier()
            commandManager.brigadierManager().setNativeNumberSuggestions(false)
        } else if (commandManager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            (commandManager as PaperCommandManager<*>).registerAsynchronousCompletions()
        }
        return commandManager
    }

    private fun registerSuggestionProviders(commandManager: PaperCommandManager<CommandSender>) {
        commandManager.parserRegistry().registerSuggestionProvider("players") { _, _ ->
            CompletableFuture.completedFuture(Bukkit.getOfflinePlayers()
                .map { Suggestion.simple(it.name.toString()) })
        }
        commandManager.parserRegistry().registerSuggestionProvider("economies") {_, _ ->
            CompletableFuture.completedFuture(Economies.entries.map { Suggestion.simple(it.name) })
        }
        commandManager.parserRegistry().registerSuggestionProvider("langKeys") { _, _ ->
            CompletableFuture.completedFuture(Locales.LangKey.entries.map { Suggestion.simple(it.name) })
        }
        commandManager.parserRegistry().registerSuggestionProvider("purgeKeys") { _, _ ->
            CompletableFuture.completedFuture(PurgeKey.entries.map { Suggestion.simple(it.name) })
        }
        commandManager.parserRegistry().registerSuggestionProvider("migrationKeys") { _, _ ->
            CompletableFuture.completedFuture(MigrationKey.entries.map { Suggestion.simple(it.name) })
        }
    }

    private fun createAnnotationParser(commandManager: PaperCommandManager<CommandSender>): AnnotationParser<CommandSender> {
        val annotationParser: AnnotationParser<CommandSender> = AnnotationParser(
            commandManager,
            CommandSender::class.java
        )
        return annotationParser
    }
}