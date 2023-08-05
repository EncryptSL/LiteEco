package encryptsl.cekuj.net

import cloud.commandframework.annotations.AnnotationParser
import cloud.commandframework.arguments.parser.ParserParameters
import cloud.commandframework.arguments.parser.StandardParameters
import cloud.commandframework.bukkit.CloudBukkitCapabilities
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator
import cloud.commandframework.meta.CommandMeta
import cloud.commandframework.paper.PaperCommandManager
import encryptsl.cekuj.net.api.ConfigAPI
import encryptsl.cekuj.net.api.UpdateNotifier
import encryptsl.cekuj.net.api.economy.LiteEcoEconomyAPI
import encryptsl.cekuj.net.api.enums.LangKey
import encryptsl.cekuj.net.api.enums.MigrationKey
import encryptsl.cekuj.net.api.enums.PurgeKey
import encryptsl.cekuj.net.commands.MoneyCMD
import encryptsl.cekuj.net.config.Locales
import encryptsl.cekuj.net.database.DatabaseConnector
import encryptsl.cekuj.net.database.models.PreparedStatements
import encryptsl.cekuj.net.hook.HookManager
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

    companion object {
        const val CONFIG_VERSION = "1.2.0"
        const val LANG_VERSION = "2.0.0"
        const val PAPI_VERSION = "1.0.5"
    }

    val pluginManager: PluginManager = server.pluginManager

    var countTransactions: LinkedHashMap<String, Int> = LinkedHashMap()

    val api: LiteEcoEconomyAPI by lazy { LiteEcoEconomyAPI(this) }
    val locale: Locales by lazy { Locales(this) }
    val preparedStatements: PreparedStatements by lazy { PreparedStatements() }

    private val configAPI: ConfigAPI by lazy { ConfigAPI(this) }
    private val hookManager: HookManager by lazy { HookManager(this) }

    override fun onLoad() {
        configAPI
            .create("database.db")
            .createConfig("config.yml", CONFIG_VERSION)
        locale
            .reloadTranslation()
        DatabaseConnector()
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
        logger.info("Plugin is disabled")
    }

    private fun blockPlugins() {
        hookManager.blockPlugin("Treasury")
        hookManager.blockPlugin("Towny")
    }

    private fun hookRegistration() {
        hookManager.hookPAPI(PAPI_VERSION)
        hookManager.hookVault()
        hookManager.hookTreasury()
    }

    private fun setupMetrics() {
        val metrics = Metrics(this, 15144)
        metrics.addCustomChart(SingleLineChart("transactions") {
            countTransactions["transactions"]
        })
    }

    @Suppress("UnstableApiUsage")
    private fun checkUpdates() {
        val updateNotifier = UpdateNotifier("101934", pluginMeta.version)
        logger.info(updateNotifier.checkPluginVersion())
    }

    private fun registerListeners() {
        var amount: Int
        val timeTaken = measureTimeMillis {
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
            listeners.forEach { listener -> pluginManager.registerEvents(listener, this)
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
        commandManager.parserRegistry().registerSuggestionProvider("players") { _, input ->
            Bukkit.getOfflinePlayers().toList()
                .filter { p ->
                    p.name?.startsWith(input) ?: false
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
        return AnnotationParser(
            commandManager,
            CommandSender::class.java,
            commandMetaFunction /* Mapper for command meta instances */
        )
    }
}