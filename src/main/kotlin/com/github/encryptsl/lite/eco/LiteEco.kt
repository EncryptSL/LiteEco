package com.github.encryptsl.lite.eco

import com.github.encryptsl.lite.eco.api.ConfigAPI
import com.github.encryptsl.lite.eco.api.UpdateNotifier
import com.github.encryptsl.lite.eco.api.economy.Currency
import com.github.encryptsl.lite.eco.api.economy.SuspendLiteEcoEconomyWrapper
import com.github.encryptsl.lite.eco.api.enums.ExportKeys
import com.github.encryptsl.lite.eco.api.enums.PurgeKey
import com.github.encryptsl.lite.eco.api.objects.ModernText
import com.github.encryptsl.lite.eco.commands.EcoCMD
import com.github.encryptsl.lite.eco.commands.MoneyCMD
import com.github.encryptsl.lite.eco.common.AccountManager
import com.github.encryptsl.lite.eco.common.config.Locales
import com.github.encryptsl.lite.eco.common.database.DatabaseConnector
import com.github.encryptsl.lite.eco.common.database.models.DatabaseEcoModel
import com.github.encryptsl.lite.eco.common.database.models.DatabaseMonologModel
import com.github.encryptsl.lite.eco.common.hook.HookManager
import com.github.encryptsl.lite.eco.listeners.PlayerAsyncPreLoginListener
import com.github.encryptsl.lite.eco.listeners.PlayerQuitListener
import com.github.encryptsl.lite.eco.utils.BukkitDispatchers
import com.github.encryptsl.lite.eco.utils.Debugger
import com.github.encryptsl.lite.eco.utils.PlaceholderHelper
import com.tchristofferson.configupdater.ConfigUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.bstats.bukkit.Metrics
import org.bstats.charts.SingleLineChart
import org.bukkit.Bukkit
import org.bukkit.plugin.PluginManager
import org.bukkit.plugin.java.JavaPlugin
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.minecraft.extras.MinecraftExceptionHandler
import org.incendo.cloud.paper.PaperCommandManager
import org.incendo.cloud.paper.util.sender.PaperSimpleSenderMapper
import org.incendo.cloud.paper.util.sender.Source
import org.incendo.cloud.suggestion.Suggestion
import java.io.File
import java.util.concurrent.CompletableFuture
import kotlin.system.measureTimeMillis


class LiteEco : JavaPlugin() {
    companion object {
        const val PAPI_VERSION = "2.0.5"

        lateinit var instance: LiteEco
            private set
    }

    val pluginManager: PluginManager = server.pluginManager

    lateinit var bukkitDispatchers: BukkitDispatchers

    private var countTransactions: LinkedHashMap<String, Int> = LinkedHashMap()

    val api: SuspendLiteEcoEconomyWrapper by lazy { SuspendLiteEcoEconomyWrapper() }
    val locale: Locales by lazy { Locales(this) }
    val databaseEcoModel: DatabaseEcoModel by lazy { DatabaseEcoModel() }
    val loggerModel: DatabaseMonologModel by lazy { DatabaseMonologModel(this) }
    val currencyImpl: Currency by lazy { Currency(this) }
    val databaseConnector: DatabaseConnector by lazy { DatabaseConnector(this) }
    val accountManager: AccountManager by lazy { AccountManager(this) }
    val debugger: Debugger by lazy { Debugger(this) }
    val placeholderHelper by lazy { PlaceholderHelper(this) }

    val pluginScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val configAPI: ConfigAPI by lazy { ConfigAPI(this) }
    private val hookManager: HookManager by lazy { HookManager(this) }
    private val configFile = File(dataFolder, "config.yml")

    override fun onLoad() {
        instance = this

        configAPI
            .create("database.db")
            .createConfig("config.yml")
        locale.load()
        databaseConnector.onLoad()
    }

    override fun onEnable() {
        val timeTaken = measureTimeMillis {
            bukkitDispatchers = BukkitDispatchers(this)
            blockPlugins()
            hookRegistration()
            setupMetrics()
            checkUpdates()
            createCommandManager()
            registerListeners()
        }
        try {
            ConfigUpdater.update(this, "config.yml", configFile, "economy", "economy.currencies")
            componentLogger.info(ModernText.miniModernText("<green>Config was updated on current version !"))
        } catch (e : Exception) {
            logger.severe(e.message ?: e.localizedMessage)
        }
        componentLogger.info(ModernText.miniModernText("Contribute to other updates <yellow>https://ko-fi.com/encryptsl"))
        componentLogger.info(ModernText.miniModernText("<green>Plugin enabled in time $timeTaken ms"))
    }

    override fun onDisable() {
        hookManager.unregisterHooks()
        api.syncAccounts()
        pluginScope.cancel()
        databaseConnector.onDisable()
        logger.info("Plugin is disabled")
    }

    fun increaseTransactions(value: Int) {
        countTransactions["transactions"] = countTransactions.getOrDefault("transactions", 0) + value
    }

    private fun blockPlugins() {
        hookManager.blockPlugin("Towny")
    }

    private fun hookRegistration() {
        hookManager.registerHooks()
    }

    private fun setupMetrics() {
        if (config.getBoolean("plugin.metrics", true)) {
            val metrics = Metrics(this, 15144)
            metrics.addCustomChart(SingleLineChart("transactions") {
                countTransactions["transactions"]
            })
        }
    }

    private fun checkUpdates() {
        val updateNotifier = UpdateNotifier(this,"101934", pluginMeta.version)
        updateNotifier.checkForUpdateAsync()
    }

    private fun registerListeners() {
        val listeners = arrayOf(
            PlayerAsyncPreLoginListener(this),
            PlayerQuitListener(this)
        )
        val timeTaken = measureTimeMillis {
            for (listener in listeners) {
                pluginManager.registerEvents(listener, this)
            }
        }
        componentLogger.info(ModernText.miniModernText("Registering <yellow>(${listeners.size})</yellow> of listeners took <yellow>$timeTaken ms</yellow> -> ok"))
    }

    private fun createCommandManager() {
        componentLogger.info(ModernText.miniModernText("<blue>Registering commands with Cloud Command Framework !"))

        val commandManager: PaperCommandManager<Source> = PaperCommandManager
            .builder(PaperSimpleSenderMapper.simpleSenderMapper())
            .executionCoordinator(ExecutionCoordinator.simpleCoordinator())
            .buildOnEnable(this)

        registerMinecraftExceptionHandler(commandManager)
        registerSuggestionModernProviders(commandManager)

        listOf(
            MoneyCMD(this),
            EcoCMD(this),
        ).forEach {
            it.execute(commandManager)
        }
    }

    private fun registerMinecraftExceptionHandler(commandManager: PaperCommandManager<Source>) {
        MinecraftExceptionHandler.create<Source> { source -> source.source() }
            .defaultHandlers()
            .decorator { component ->
                ModernText.miniModernText(config.getString("plugin.prefix", "<red>[!]").toString()).appendSpace().append(component)
            }.registerTo(commandManager)
    }

    private fun registerSuggestionModernProviders(commandManager: PaperCommandManager<Source>) {
        commandManager.parserRegistry().registerSuggestionProvider("players") { _, _ ->
            modifiableSuggestionPlayerSuggestion()
        }
        commandManager.parserRegistry().registerSuggestionProvider("purgeKeys") { _, _ ->
            CompletableFuture.completedFuture(PurgeKey.entries.map { Suggestion.suggestion(it.name) })
        }
        commandManager.parserRegistry().registerSuggestionProvider("exportKeys") { _, _ ->
            CompletableFuture.completedFuture(ExportKeys.entries.map { Suggestion.suggestion(it.name) })
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
}