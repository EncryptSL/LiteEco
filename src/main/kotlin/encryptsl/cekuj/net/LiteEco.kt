package encryptsl.cekuj.net

import cloud.commandframework.annotations.AnnotationParser
import cloud.commandframework.arguments.parser.ParserParameters
import cloud.commandframework.arguments.parser.StandardParameters
import cloud.commandframework.bukkit.CloudBukkitCapabilities
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator
import cloud.commandframework.meta.CommandMeta
import cloud.commandframework.paper.PaperCommandManager
import encryptsl.cekuj.net.api.ConfigAPI
import encryptsl.cekuj.net.api.HookManager
import encryptsl.cekuj.net.api.UpdateNotifier
import encryptsl.cekuj.net.api.economy.LiteEcoEconomyAPI
import encryptsl.cekuj.net.api.enums.LangKey
import encryptsl.cekuj.net.api.enums.MigrationKey
import encryptsl.cekuj.net.api.enums.PurgeKey
import encryptsl.cekuj.net.commands.MoneyCMD
import encryptsl.cekuj.net.config.TranslationConfig
import encryptsl.cekuj.net.database.DatabaseConnector
import encryptsl.cekuj.net.database.models.PreparedStatements
import encryptsl.cekuj.net.listeners.*
import org.bstats.bukkit.Metrics
import org.bstats.charts.SingleLineChart
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.plugin.PluginManager
import org.bukkit.plugin.java.JavaPlugin
import java.util.function.Function
import kotlin.system.measureTimeMillis

class LiteEco : JavaPlugin() {

    private val databaseConnector: DatabaseConnector by lazy { DatabaseConnector() }
    private lateinit var metrics: Metrics
    private lateinit var updateNotifier: UpdateNotifier
    var countTransactions: LinkedHashMap<String, Int> = LinkedHashMap()
    val pluginManager: PluginManager = server.pluginManager
    val preparedStatements: PreparedStatements by lazy { PreparedStatements() }
    val translationConfig: TranslationConfig by lazy { TranslationConfig(this) }
    val api: LiteEcoEconomyAPI by lazy { LiteEcoEconomyAPI(this) }
    private val configAPI: ConfigAPI by lazy { ConfigAPI(this) }
    private val hookManager: HookManager by lazy { HookManager(this) }

    private fun initDatabase() {
        databaseConnector.initConnect(
            config.getString("database.connection.jdbc_host") ?: "jdbc:mysql://localhost:3306/mydatabase",
            config.getString("database.connection.user") ?: "root",
            config.getString("database.connection.pass") ?: "password"
        )
    }

    private fun setupMetrics() {
        metrics = Metrics(this, 15144)
        metrics.addCustomChart(SingleLineChart("transactions") {
            countTransactions["transactions"]
        })
    }

    private fun checkUpdates() {
        updateNotifier = UpdateNotifier("101934", description.version)
        logger.info(updateNotifier.checkPluginVersion())
    }

    override fun onLoad() {
        configAPI
            .create("database.db")
            .createConfig("config.yml", "1.0.0")
        translationConfig
            .loadTranslation()
        initDatabase()
    }

    override fun onEnable() {
        val start = System.currentTimeMillis()
        blockPlugins()
        hookRegistration()
        setupMetrics()
        checkUpdates()
        registerCommands()
        registerListener()
        logger.info("Plugin enabled in time ${System.currentTimeMillis() - start} ms")
    }

    override fun onDisable() {
        logger.info("Plugin is disabled")
    }

    private fun registerCommands() {
        logger.info("Registering commands with Cloud Command Framework !")

        val commandManager = createCommandManager()

        registerSuggestionProviders(commandManager)

        val annotationParser = createAnnotationParser(commandManager)
        annotationParser.parse(MoneyCMD(this))
    }

    private fun createCommandManager(): PaperCommandManager<CommandSender> {
        val executionCoordinatorFunction = AsynchronousCommandExecutionCoordinator.builder<CommandSender>().build()
        val mapperFunction = Function.identity<CommandSender>()
        val commandManager = PaperCommandManager(
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

        return commandManager
    }

    private fun registerSuggestionProviders(commandManager: PaperCommandManager<CommandSender>) {
        commandManager.parserRegistry().registerSuggestionProvider("players") { commandSender, input ->
            Bukkit.getOfflinePlayers().toList()
                .filter { p ->
                    commandSender.hasPermission("lite.eco.suggestion.players") && (p.name?.startsWith(input) ?: false)
                }
                .mapNotNull { it.name }
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
    }

    private fun createAnnotationParser(commandManager: PaperCommandManager<CommandSender>): AnnotationParser<CommandSender> {
        val commandMetaFunction = Function<ParserParameters, CommandMeta> { p: ParserParameters ->
            CommandMeta.simple() // Decorate commands with descriptions
                .with(CommandMeta.DESCRIPTION, p[StandardParameters.DESCRIPTION, "No Description"])
                .build()
        }

        return AnnotationParser( /* Manager */
            commandManager,  /* Command sender type */
            CommandSender::class.java,  /* Mapper for command meta instances */
            commandMetaFunction
        )
    }

    private fun registerListener() {
        val listeners = arrayListOf(
            AccountEconomyManageListener(this),
            PlayerEconomyPayListener(this),
            AdminEconomyGlobalDepositListener(this),
            AdminEconomyGlobalSetListener(this),
            AdminEconomyGlobalWithdrawListener(this),
            AdminEconomyMoneyDepositListener(this),
            AdminEconomyMoneyWithdrawListener(this),
            AdminEconomyMoneySetListener(this),
            PlayerJoinListener(this)
        )
        val timeTaken = measureTimeMillis {
            listeners.forEach { listener -> pluginManager.registerEvents(listener, this)
                logger.info("Bukkit Listener ${listener.javaClass.simpleName} registered () -> ok")
            }
        }
        logger.info("Listeners registered(${listeners.size}) in time $timeTaken ms -> ok")
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