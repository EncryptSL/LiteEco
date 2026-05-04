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
import com.github.encryptsl.lite.eco.common.config.BaseConfig
import com.github.encryptsl.lite.eco.common.config.Locales
import com.github.encryptsl.lite.eco.common.database.DatabaseConnector
import com.github.encryptsl.lite.eco.common.database.models.DatabaseEcoModel
import com.github.encryptsl.lite.eco.common.database.models.DatabaseMonologModel
import com.github.encryptsl.lite.eco.common.hook.HookManager
import com.github.encryptsl.lite.eco.listeners.PlayerListeners
import com.github.encryptsl.lite.eco.utils.ClassUtil
import com.github.encryptsl.lite.eco.utils.Debugger
import com.github.encryptsl.lite.eco.utils.PlaceholderHelper
import com.github.encryptsl.lite.eco.utils.SchedulerHelper
import de.exlll.configlib.NameFormatters
import de.exlll.configlib.YamlConfigurationProperties
import de.exlll.configlib.YamlConfigurations
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.bukkit.plugin.PluginManager
import org.bukkit.plugin.java.JavaPlugin
import java.nio.file.Files
import java.util.concurrent.ConcurrentHashMap
import kotlin.system.measureTimeMillis


class LiteEco : JavaPlugin() {
    companion object {
        const val PAPI_VERSION = "2.0.5"

        lateinit var instance: LiteEco
            private set

        fun isFolia(): Boolean {
            return ClassUtil.isValidClasspath("io.papermc.paper.threadedregions.RegionizedServer")
        }
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
    val schedulerHelper by lazy { SchedulerHelper(this, isFolia()) }
    val configFile by lazy { dataFolder.toPath().resolve("config.yml") }

    val pluginScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val configAPI: ConfigAPI by lazy { ConfigAPI(this) }
    private val hookManager: HookManager by lazy { HookManager(this) }
    private val commandFeatureManager by lazy { CommandFeatureManager(this) }
    private val settings = YamlConfigurationProperties.newBuilder()
        .setNameFormatter(NameFormatters.LOWER_UNDERSCORE)
        .build()

    lateinit var baseConfig: BaseConfig
        private set

    override fun onLoad() {
        instance = this
        createConfig()
        configAPI
            .create("database.db")
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
        PlayerAccount.startJanitor(this)
        val pluginRunningOnFolia = if (isFolia()) "<blue>[Folia]</blue>" else "<yellow>[PaperMC]</yellow>"
        logger.info(ModernText.miniModernText("Contribute to other updates <yellow>https://ko-fi.com/encryptsl"))
        logger.info(ModernText.miniModernText("<green>Plugin enabled on $pluginRunningOnFolia in time $timeTaken ms"))
    }

    override fun onDisable() {
        // Needed we must cancel all tasks because janitor can corrupt final saving.
        schedulerHelper.cancelTasks()
        //END
        hookManager.unregisterHooks()
        api.syncAccounts()
        pluginScope.cancel()
        databaseConnector.onDisable()
        logger.info("Plugin is disabled")
    }

    fun increaseTransactions(value: Int) {
        countTransactions["transactions"] = (countTransactions["transactions"] ?: 0) + value
    }

    fun saveBaseConfig() {
        YamlConfigurations.save(configFile, BaseConfig::class.java, baseConfig, settings)
    }

    fun updateLanguage(newLocale: String) {
        baseConfig.plugin.translation = newLocale
        saveBaseConfig()
    }

    fun reloadBaseConfig() {
        baseConfig = YamlConfigurations.update(configFile, BaseConfig::class.java, settings)
    }

    private fun createConfig() {
        try {
            if (Files.notExists(dataFolder.toPath())) {
                Files.createDirectories(configFile)
            }

            baseConfig = YamlConfigurations.update(configFile, BaseConfig::class.java, settings)

        } catch (e: Exception) {
            logger.error("Error during initialization: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun registerListeners() {
        val listeners = arrayOf(
            PlayerListeners(this)
        )
        val timeTaken = measureTimeMillis {
            for (listener in listeners) {
                pluginManager.registerEvents(listener, this)
            }
        }
        logger.info(ModernText.miniModernText("Registering <yellow>(${listeners.size})</yellow> of listeners took <yellow>$timeTaken ms</yellow> -> ok"))
    }
}