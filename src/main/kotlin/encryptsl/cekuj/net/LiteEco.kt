package encryptsl.cekuj.net

import cloud.commandframework.annotations.AnnotationParser
import cloud.commandframework.arguments.parser.ParserParameters
import cloud.commandframework.arguments.parser.StandardParameters
import cloud.commandframework.bukkit.CloudBukkitCapabilities
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator
import cloud.commandframework.meta.CommandMeta
import cloud.commandframework.paper.PaperCommandManager
import encryptsl.cekuj.net.api.*
import encryptsl.cekuj.net.api.economy.vault.AdaptiveEconomyVaultAPI
import encryptsl.cekuj.net.api.economy.LiteEcoEconomyAPI
import encryptsl.cekuj.net.api.enums.TranslationKey
import encryptsl.cekuj.net.commands.MoneyCMD
import encryptsl.cekuj.net.config.TranslationConfig
import encryptsl.cekuj.net.database.DatabaseConnector
import encryptsl.cekuj.net.database.models.PreparedStatements
import net.milkbowl.vault.economy.Economy
import org.bstats.bukkit.Metrics
import org.bstats.charts.SingleLineChart
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.plugin.PluginManager
import org.bukkit.plugin.ServicePriority
import org.bukkit.plugin.java.JavaPlugin
import java.util.function.Function

class LiteEco : JavaPlugin() {

    private val databaseConnector: DatabaseConnector by lazy { DatabaseConnector() }
    private lateinit var metrics: Metrics
    lateinit var econ: Economy
    private lateinit var updateNotifier: UpdateNotifier
    var countTransactions: LinkedHashMap<String, Int> = LinkedHashMap()
    val pluginManger: PluginManager = server.pluginManager
    val preparedStatements: PreparedStatements by lazy { PreparedStatements() }
    val translationConfig: TranslationConfig by lazy { TranslationConfig(this) }
    val api: LiteEcoEconomyAPI by lazy { LiteEcoEconomyAPI(this) }
    private val configLoaderAPI: ConfigLoaderAPI by lazy { ConfigLoaderAPI(this) }
    override fun onLoad() {
        configLoaderAPI
            .create("database.db")
            .create("config.yml")
        translationConfig
            .loadTranslation()
        databaseConnector.initConnect(
            config.getString("database.connection.jdbc_host")!!,
            config.getString("database.connection.user")!!,
            config.getString("database.connection.pass")!!
        )
    }

    override fun onEnable() {
        val start = System.currentTimeMillis()
        setupPAPI()
        if (!connectToVault()) {
            slF4JLogger.info("[%s] - Disabled due to no Vault dependency found!".format(description.name))
            server.pluginManager.disablePlugin(this)
            return
        }
        metrics = Metrics(this, 15144)
        metrics.addCustomChart(SingleLineChart("transactions") {
            countTransactions["transactions"]
        })
        updateNotifier = UpdateNotifier("101934", description.version)
        slF4JLogger.info(updateNotifier.checkPluginVersion())
        registerCommands()
        val handlerListeners = HandlerListeners(this)
        handlerListeners.registerListener()
        slF4JLogger.info("Plugin enabled in time ${System.currentTimeMillis() - start} ms")
    }

    override fun onDisable() {
        slF4JLogger.info("Plugin is disabled")
    }

    private fun registerCommands() {
        slF4JLogger.info("Registering commands with Cloud Command Framework !")
        val executionCoordinatorFunction = AsynchronousCommandExecutionCoordinator.builder<CommandSender>().build()
        val mapperFunction = Function.identity<CommandSender>()
        val commandManager: PaperCommandManager<CommandSender> = PaperCommandManager(
            this,
            executionCoordinatorFunction,
            mapperFunction,
            mapperFunction
        )
        if (commandManager.hasCapability(CloudBukkitCapabilities.BRIGADIER)) {
            commandManager.registerBrigadier()
            commandManager.brigadierManager()?.setNativeNumberSuggestions(false)
        }
        if (commandManager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            (commandManager as PaperCommandManager<*>).registerAsynchronousCompletions()
        }
        val commandMetaFunction =
            Function<ParserParameters, CommandMeta> { p: ParserParameters ->
                CommandMeta.simple() // This will allow you to decorate commands with descriptions
                    .with(
                        CommandMeta.DESCRIPTION,
                        p.get(StandardParameters.DESCRIPTION, "No description")
                    )
                    .build()
            }
        val annotationParser = AnnotationParser( /* Manager */
            commandManager,  /* Command sender type */
            CommandSender::class.java,  /* Mapper for command meta instances */
            commandMetaFunction
        )
        commandManager.parserRegistry().registerSuggestionProvider("players") { commandSender, input ->
                Bukkit.getOfflinePlayers().toList().stream()
                    .map(OfflinePlayer::getName).filter { p ->
                        commandSender.hasPermission("lite.eco.suggestion.players") && (p?.startsWith(
                            input
                        ) ?: false)
                    }.toList()
        }
        commandManager.parserRegistry().registerSuggestionProvider("offlinePlayers") { _, input ->
            Bukkit.getOfflinePlayers().toList().stream()
                .map(OfflinePlayer::getName).filter { p ->
                    p?.startsWith(input) ?: false
                }.toList()
        }
        commandManager.parserRegistry().registerSuggestionProvider("translationKeys") { _, _ ->
            TranslationKey.values().map { key -> key.name }.toList()
        }
        annotationParser.parse(MoneyCMD(this))
    }

    private fun connectToVault(): Boolean {
        return if (pluginManger.isPluginEnabled("Vault")) {
            server.servicesManager.register(Economy::class.java, AdaptiveEconomyVaultAPI(this), this, ServicePriority.Highest)
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