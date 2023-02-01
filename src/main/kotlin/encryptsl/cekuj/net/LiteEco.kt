package encryptsl.cekuj.net

import cloud.commandframework.annotations.AnnotationParser
import cloud.commandframework.arguments.parser.ParserParameters
import cloud.commandframework.arguments.parser.StandardParameters
import cloud.commandframework.bukkit.CloudBukkitCapabilities
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator
import cloud.commandframework.meta.CommandMeta
import cloud.commandframework.paper.PaperCommandManager
import encryptsl.cekuj.net.api.ConfigLoaderAPI
import encryptsl.cekuj.net.api.HookManager
import encryptsl.cekuj.net.api.UpdateNotifier
import encryptsl.cekuj.net.api.economy.LiteEcoEconomyAPI
import encryptsl.cekuj.net.api.enums.MigrationKey
import encryptsl.cekuj.net.api.enums.PurgeKey
import encryptsl.cekuj.net.api.enums.LangKey
import encryptsl.cekuj.net.commands.MoneyCMD
import encryptsl.cekuj.net.config.TranslationConfig
import encryptsl.cekuj.net.database.DatabaseConnector
import encryptsl.cekuj.net.database.models.PreparedStatements
import org.bstats.bukkit.Metrics
import org.bstats.charts.SingleLineChart
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.plugin.PluginManager
import org.bukkit.plugin.java.JavaPlugin
import java.util.function.Function

class LiteEco : JavaPlugin() {

    private val databaseConnector: DatabaseConnector by lazy { DatabaseConnector() }
    private lateinit var metrics: Metrics
    private lateinit var updateNotifier: UpdateNotifier
    var countTransactions: LinkedHashMap<String, Int> = LinkedHashMap()
    val pluginManger: PluginManager = server.pluginManager
    val preparedStatements: PreparedStatements by lazy { PreparedStatements() }
    val translationConfig: TranslationConfig by lazy { TranslationConfig(this) }
    val api: LiteEcoEconomyAPI by lazy { LiteEcoEconomyAPI(this) }
    private val configLoaderAPI: ConfigLoaderAPI by lazy { ConfigLoaderAPI(this) }
    private val hookManager: HookManager by lazy { HookManager(this) }
    override fun onLoad() {
        configLoaderAPI
            .create("database.db")
            .createConfig("config.yml", "1.0.0")
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
        blockPlugins()
        hookRegistration()
        metrics = Metrics(this, 15144)
        metrics.addCustomChart(SingleLineChart("transactions") {
            countTransactions["transactions"]
        })
        updateNotifier = UpdateNotifier("101934", description.version)
        logger.info(updateNotifier.checkPluginVersion())
        registerCommands()
        val handlerListeners = HandlerListeners(this)
        handlerListeners.registerListener()
        logger.info("Plugin enabled in time ${System.currentTimeMillis() - start} ms")
    }

    override fun onDisable() {
        logger.info("Plugin is disabled")
    }

    private fun registerCommands() {
        logger.info("Registering commands with Cloud Command Framework !")
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
        commandManager.parserRegistry().registerSuggestionProvider("langKeys") { _, _ ->
            LangKey.values().map { key -> key.name }.toList()
        }
        commandManager.parserRegistry().registerSuggestionProvider("purgeKeys") { _, _ ->
            PurgeKey.values().filter { key -> key != PurgeKey.NULL_ACCOUNTS }.map { key -> key.name }.toList()
        }
        commandManager.parserRegistry().registerSuggestionProvider("migrationKeys") { _, _ ->
            MigrationKey.values().map { key -> key.name }.toList()
        }
        annotationParser.parse(MoneyCMD(this))
    }

    private fun blockPlugins() {
        hookManager.blockPlugin("Treasury")
        hookManager.blockPlugin("Towny")
    }

    private fun hookRegistration() {
        hookManager.hookPAPI()
        hookManager.hookVault()
        hookManager.hookTreasury()
    }

}