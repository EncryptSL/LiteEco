package com.github.encryptsl.lite.eco

import com.github.encryptsl.lite.eco.api.ConfigAPI
import com.github.encryptsl.lite.eco.api.MetricsCollector
import com.github.encryptsl.lite.eco.api.UpdateNotifier
import com.github.encryptsl.lite.eco.api.account.PlayerAccount
import com.github.encryptsl.lite.eco.api.economy.Currency
import com.github.encryptsl.lite.eco.api.economy.SuspendLiteEcoEconomyWrapper
import com.github.encryptsl.lite.eco.api.objects.ModernText
import com.github.encryptsl.lite.eco.commands.CommandFeatureManager
import com.github.encryptsl.lite.eco.common.AccountManager
import com.github.encryptsl.lite.eco.common.config.Locales
import com.github.encryptsl.lite.eco.common.database.DatabaseConnector
import com.github.encryptsl.lite.eco.common.database.models.DatabaseEcoModel
import com.github.encryptsl.lite.eco.common.database.models.DatabaseMonologModel
import com.github.encryptsl.lite.eco.common.hook.HookManager
import com.github.encryptsl.lite.eco.listeners.PlayerListeners
import com.github.encryptsl.lite.eco.utils.Debugger
import com.github.encryptsl.lite.eco.utils.PlaceholderHelper
import com.tchristofferson.configupdater.ConfigUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.bukkit.Bukkit
import org.bukkit.plugin.PluginManager
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlin.system.measureTimeMillis


class LiteEco : JavaPlugin() {
    companion object {
        const val PAPI_VERSION = "2.0.5"

        lateinit var instance: LiteEco
            private set
    }

    val pluginManager: PluginManager = server.pluginManager

    val logger = componentLogger

    private val countTransactions = ConcurrentHashMap<String, Int>()
    private val updateNotifier: UpdateNotifier by lazy { UpdateNotifier(this,"101934", pluginMeta.version) }
    private val metricsCollector: MetricsCollector by lazy { MetricsCollector(this,15144, countTransactions) }

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
    private val commandFeatureManager by lazy { CommandFeatureManager(this) }
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
            hookManager.blockPlugin("Towny")
            hookManager.registerHooks()
            metricsCollector.setupMetrics()
            updateNotifier.checkForUpdateAsync()
            commandFeatureManager.createCommandManager()
            registerListeners()
        }
        try {
            ConfigUpdater.update(this, "config.yml", configFile,
                "economy.currencies",
                "database.connection",
                "formatting.placeholders"
            )
            componentLogger.info(ModernText.miniModernText("<green>Config was updated on current version !"))
        } catch (e : Exception) {
            logger.error(e.message ?: e.localizedMessage)
        }
        PlayerAccount.startJanitor(this)
        componentLogger.info(ModernText.miniModernText("Contribute to other updates <yellow>https://ko-fi.com/encryptsl"))
        componentLogger.info(ModernText.miniModernText("<green>Plugin enabled in time $timeTaken ms"))
    }

    override fun onDisable() {
        Bukkit.getScheduler().cancelTasks(this) // Needed we must cancel all tasks because janitor can corrupt final saving.
        hookManager.unregisterHooks()
        api.syncAccounts()
        pluginScope.cancel()
        databaseConnector.onDisable()
        logger.info("Plugin is disabled")
    }

    fun increaseTransactions(value: Int) {
        countTransactions["transactions"] = (countTransactions["transactions"] ?: 0) + value
    }

    private fun registerListeners() {
        val listeners = arrayOf(
            PlayerListeners(this),
        )
        val timeTaken = measureTimeMillis {
            for (listener in listeners) {
                pluginManager.registerEvents(listener, this)
            }
        }
        logger.info(ModernText.miniModernText("Registering <yellow>(${listeners.size})</yellow> of listeners took <yellow>$timeTaken ms</yellow> -> ok"))
    }
}