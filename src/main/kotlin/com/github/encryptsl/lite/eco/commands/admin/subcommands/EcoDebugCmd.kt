package com.github.encryptsl.lite.eco.commands.admin.subcommands

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.objects.ModernText
import com.github.encryptsl.lite.eco.commands.internal.CommandFeature
import com.github.encryptsl.lite.eco.commands.parsers.CurrencyParser
import com.github.encryptsl.lite.eco.common.database.models.AccountRepository
import com.github.encryptsl.lite.eco.utils.Helper
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.incendo.cloud.Command
import org.incendo.cloud.bukkit.parser.OfflinePlayerParser
import org.incendo.cloud.bukkit.parser.PlayerParser
import org.incendo.cloud.component.DefaultValue
import org.incendo.cloud.description.CommandDescription
import org.incendo.cloud.paper.PaperCommandManager
import org.incendo.cloud.paper.util.sender.Source
import org.incendo.cloud.parser.standard.DoubleParser
import org.incendo.cloud.parser.standard.IntegerParser
import java.math.BigDecimal

class EcoDebugCmd(
    private val liteEco: LiteEco,
    private val helper: Helper,
) : CommandFeature {

    override fun register(
        commandManager: PaperCommandManager<Source>,
        base: Command.Builder<Source>
    ) {
        val debugSubCommand = base.literal("debug")

        commandManager.command(
            debugSubCommand.literal("failmode")
                .commandDescription(CommandDescription.commandDescription("Toggle database failure simulation."))
                .permission("lite.eco.admin.debug.failmode")
                .handler { context ->
                    AccountRepository.debugFailMode = !AccountRepository.debugFailMode
                    val status = if (AccountRepository.debugFailMode) "§cENABLED (Error)" else "§aDISABLED (OK)"
                    context.sender().source().sendMessage("§8[§bLiteEco§8] §7Simulated DB failure: $status")
                }
        )

        commandManager.command(
            debugSubCommand.literal("test-janitor")
                .commandDescription(CommandDescription.commandDescription("Run automated persistence test (failMode + deposit)."))
                .required("target", PlayerParser.playerParser())
                .permission("lite.eco.admin.debug.testjanitor")
                .handler { context ->
                    val target: Player = context.get("target")
                    helper.executeJanitorTest(target)
                }
        )

        commandManager.command(
            debugSubCommand.literal("janitor")
                .commandDescription(CommandDescription.commandDescription("Force immediate synchronization of all offline players in cache."))
                .permission("lite.eco.admin.debug.janitor")
                .handler { context ->
                    helper.forceJanitorSync(context.sender().source())
                }
        )

        commandManager.command(
            debugSubCommand.literal("inspect")
                .commandDescription(CommandDescription.commandDescription("View detailed cache content for a player."))
                .required("target", OfflinePlayerParser.offlinePlayerParser())
                .permission("lite.eco.admin.debug.inspect")
                .handler { context ->
                    val target: OfflinePlayer = context.get("target")
                    helper.inspectCache(context.sender().source(), target.uniqueId)
                }
        )

        commandManager.command(
            debugSubCommand.literal("stress")
                .commandDescription(CommandDescription.commandDescription("Run simultaneous stress test of transaction atomicity."))
                .required("target", PlayerParser.playerParser())
                .optional("iterations", IntegerParser.integerParser(1, 1000), DefaultValue.constant(100))
                .permission("lite.eco.admin.debug.stress")
                .handler { context ->
                    val target: Player = context.get("target")
                    val iterations: Int = context.get("iterations")
                    helper.executeStressTest(target, 1.0, iterations)
                }
        )

        commandManager.command(
            debugSubCommand.literal("stress-shutdown")
                .commandDescription(CommandDescription.commandDescription("Run global shutdown sync stress test on multiple cached accounts."))
                .optional("accounts", IntegerParser.integerParser(1, 10000), DefaultValue.constant(100))
                .permission("lite.eco.admin.debug.stress")
                .handler { context ->
                    val accountCount: Int = context.getOrDefault("accounts", 100)
                    helper.executeStressShutdownTest(context.sender(), accountCount)
                }
        )

        commandManager.command(
            debugSubCommand.literal("dupe-test")
                .commandDescription(CommandDescription.commandDescription("Run concurrent transfer dupe test to verify locks."))
                .required("target", OfflinePlayerParser.offlinePlayerParser(), commandManager.parserRegistry().getSuggestionProvider("players").get())
                .optional("source", OfflinePlayerParser.offlinePlayerParser(), commandManager.parserRegistry().getSuggestionProvider("players").get())
                .optional("amount", DoubleParser.doubleParser(0.01), DefaultValue.constant(10.0))
                .optional("requests", IntegerParser.integerParser(1), DefaultValue.constant(20))
                .optional("currency", commandManager.componentBuilder(String::class.java, "currency")
                    .parser(CurrencyParser()).defaultValue(DefaultValue.parsed(liteEco.currencyImpl.defaultCurrency()))
                )
                .permission("lite.eco.admin.debug.stress")
                .handler { context ->
                    val sender = context.sender().source()
                    val target: OfflinePlayer = context.get("target")
                    val sourcePlayer: OfflinePlayer? = context.getOrDefault("source", null)

                    val sourceUuid = sourcePlayer?.uniqueId ?: (sender as? Player)?.uniqueId
                    val sourceName = sourcePlayer?.name ?: (sender as? Player)?.name ?: "Console"

                    if (sourceUuid == null) {
                        val errorMsg = ModernText.miniModernText("<red>[LiteEco DupeTest] You must specify a source player when running from console!</red>")
                        sender.sendMessage(errorMsg)
                        liteEco.logger.warn(errorMsg)
                        return@handler
                    }

                    val amount = BigDecimal.valueOf(context.getOrDefault("amount", 10.0))
                    val requestsCount = context.getOrDefault("requests", 20)
                    val currency = context.getOrDefault("currency", liteEco.currencyImpl.defaultCurrency())

                    helper.executeDupeTest(context.sender(), sourceUuid, sourceName, target, amount, requestsCount, currency)
                }
        )
    }
}