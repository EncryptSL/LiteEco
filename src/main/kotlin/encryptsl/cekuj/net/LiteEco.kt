package encryptsl.cekuj.net

import co.aikar.commands.CommandCompletions
import co.aikar.commands.PaperCommandManager
import encryptsl.cekuj.net.api.*
import encryptsl.cekuj.net.commands.MoneyCMD
import encryptsl.cekuj.net.config.TranslationConfig
import encryptsl.cekuj.net.database.DatabaseConnector
import encryptsl.cekuj.net.database.PreparedStatements
import encryptsl.cekuj.net.extensions.registerOfflinePlayers
import encryptsl.cekuj.net.extensions.registerTranslationKeys
import net.milkbowl.vault.economy.Economy
import org.bstats.bukkit.Metrics
import org.bstats.charts.SingleLineChart
import org.bukkit.plugin.PluginManager
import org.bukkit.plugin.ServicePriority
import org.bukkit.plugin.java.JavaPlugin

class LiteEco : JavaPlugin() {

    lateinit var metrics: Metrics
    lateinit var econ: Economy
    lateinit var updateNotifier: UpdateNotifier
    lateinit var transactions: LinkedHashMap<String, Int>
    val pluginManger: PluginManager = server.pluginManager
    val databaseConnector: DatabaseConnector by lazy { DatabaseConnector() }
    val preparedStatements: PreparedStatements by lazy { PreparedStatements(this) }
    val translationConfig: TranslationConfig by lazy { TranslationConfig(this) }
    private val configLoaderAPI: ConfigLoaderAPI by lazy { ConfigLoaderAPI(this) }
    private val commandManager: PaperCommandManager by lazy { PaperCommandManager(this) }
    override fun onLoad() {
        configLoaderAPI.setConfigName("database.db").load()
            .setConfigName("config.yml").load()
        translationConfig.loadTranslation()
    }

    override fun onEnable() {
        val start = System.currentTimeMillis()
        setupPAPI()
        if (!connectToVault()) {
            slF4JLogger.info("[%s] - Disabled due to no Vault dependency found!".format(description.name))
            server.pluginManager.disablePlugin(this)
            return
        }
        transactions = LinkedHashMap()
        metrics = Metrics(this, 15144)
        metrics.addCustomChart(SingleLineChart("transactions") {
            transactions["transactions"]
        })
        updateNotifier = UpdateNotifier("101934", description.version)
        slF4JLogger.info(updateNotifier.checkPluginVersion())
        registerCommands()
        databaseConnector.initConnect(
            config.getString("database.connection.jdbc_host")!!,
            config.getString("database.connection.user")!!,
            config.getString("database.connection.pass")!!
        )
        preparedStatements.createTable(config.getString("database.connection.jdbc_host").toString())
        val handlerListeners = HandlerListeners(this)
        handlerListeners.registerListener()
        slF4JLogger.info("Plugin enabled in time ${System.currentTimeMillis() - start} ms")
    }

    override fun onDisable() {
        slF4JLogger.info("Plugin is disabled")
    }

    private fun registerCommands() {
        slF4JLogger.info("Registering commands with Aikar Commands Framework !")
        val commandCompletions: CommandCompletions<*> = commandManager.commandCompletions
        commandManager.registerCommand(MoneyCMD(this))
        commandCompletions.registerOfflinePlayers()
        commandCompletions.registerTranslationKeys()
    }

    private fun connectToVault(): Boolean {
        return if (pluginManger.isPluginEnabled("Vault")) {
            server.servicesManager.register(Economy::class.java, AdaptiveEconomyProvider(this), this, ServicePriority.Highest)
            slF4JLogger.warn("Registered Vault interface.")
            val rsp = server.servicesManager.getRegistration(
                Economy::class.java
            )
            if (rsp != null) {
                econ = rsp.provider
            }
            true
        } else {
            slF4JLogger.error("Vault not found. Please download Vault to use LiteEco " + server.version)
            false
        }
    }

    private fun setupPAPI()  {
        if (pluginManger.getPlugin("PlaceholderAPI") != null) {
            slF4JLogger.warn("PlaceholderAPI hook initialized")
            PlaceHolderExtensionProvider(this).register()
        } else {
            slF4JLogger.error("PlaceholderAPI hook not found")
        }
    }

}