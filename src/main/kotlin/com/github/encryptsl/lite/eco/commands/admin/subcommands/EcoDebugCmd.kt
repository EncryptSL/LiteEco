package com.github.encryptsl.lite.eco.commands.admin.subcommands

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.economy.account.AccountCache
import com.github.encryptsl.lite.eco.api.objects.ModernText
import com.github.encryptsl.lite.eco.commands.internal.CommandFeature
import com.github.encryptsl.lite.eco.commands.parsers.CurrencyParser
import com.github.encryptsl.lite.eco.common.database.models.DatabaseEcoModel
import com.github.encryptsl.lite.eco.utils.Helper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
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
import java.util.*
import kotlin.system.measureTimeMillis

class EcoDebugCmd(
    private val helper: Helper
) : CommandFeature {

    override fun register(
        commandManager: PaperCommandManager<Source>,
        base: Command.Builder<Source>
    ) {
        val debugSubCommand = base.literal("debug")

        commandManager.command(
            debugSubCommand.literal("failmode")
                .commandDescription(CommandDescription.commandDescription("Toggle database failure simulation (writes will fail)."))
                .permission("lite.eco.admin.debug.failmode")
                .handler { context ->
                    DatabaseEcoModel.debugFailMode = !DatabaseEcoModel.debugFailMode
                    val status = if (DatabaseEcoModel.debugFailMode) "§cENABLED (Error)" else "§aDISABLED (OK)"
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
                .commandDescription(CommandDescription.commandDescription("View detailed cache content (failed currencies) for a player."))
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
                    val sender = context.sender().source()
                    val accountCount: Int = context.getOrDefault("accounts", 100)

                    val startMsg = ModernText.miniModernText("<yellow>[LiteEco Debug] Preparing $accountCount test accounts in database and cache...</yellow>")
                    sender.sendMessage(startMsg)
                    LiteEco.instance.logger.info(startMsg)

                    val generatedAccounts = Collections.synchronizedMap(mutableMapOf<UUID, BigDecimal>())
                    val currency = LiteEco.instance.currencyImpl.defaultCurrency()
                    val initialDbAmount = BigDecimal.ZERO
                    val random = kotlin.random.Random

                    LiteEco.instance.pluginScope.launch {
                        val preparationTime = measureTimeMillis {
                            (0 until accountCount).chunked(50).forEach { batch ->
                                batch.forEach { index ->
                                    val testUuid = UUID.randomUUID()
                                    val testUsername = "TestPlayer_$index"
                                    val randomAmount = BigDecimal(random.nextInt(1, 10_000))

                                    LiteEco.instance.databaseEcoModel.createPlayerAccount(
                                        testUsername,
                                        testUuid,
                                        currency,
                                        initialDbAmount
                                    )

                                    AccountCache.cache(testUuid, testUsername, currency, randomAmount)
                                    generatedAccounts[testUuid] = randomAmount
                                }
                                yield()
                            }
                        }

                        val injectedMsg = ModernText.miniModernText("<yellow>[LiteEco Debug] Injected $accountCount accounts in ${preparationTime}ms. Executing AccountCache.syncAccounts()...</yellow>")
                        sender.sendMessage(injectedMsg)
                        LiteEco.instance.logger.info(injectedMsg)

                        val syncTime = measureTimeMillis {
                            AccountCache.syncAccounts()
                        }

                        val remainingInCache = generatedAccounts.keys.count { AccountCache.isAccountCached(it, currency) }

                        var dbMismatches = 0
                        generatedAccounts.forEach { (uuid, expectedAmount) ->
                            val actualDbAmount = LiteEco.instance.databaseEcoModel.getBalance(uuid, currency)
                            if (actualDbAmount.compareTo(expectedAmount) != 0) {
                                dbMismatches++
                            }
                        }

                        if (remainingInCache == 0 && dbMismatches == 0) {
                            val passMsg = ModernText.miniModernText("<green>[LiteEco Debug] PASSED: All $accountCount accounts were successfully flushed to DB and cleared from cache in ${syncTime}ms.</green>")
                            sender.sendMessage(passMsg)
                            LiteEco.instance.logger.info(passMsg)
                        } else {
                            val failMsg = ModernText.miniModernText("<red>[LiteEco Debug] FAILED: Remaining in cache: $remainingInCache | DB value mismatches: $dbMismatches / $accountCount!</red>")
                            sender.sendMessage(failMsg)
                            LiteEco.instance.logger.warn(failMsg)
                        }
                    }
                }
        )

        commandManager.command(
            debugSubCommand.literal("dupe-test")
                .commandDescription(CommandDescription.commandDescription("Run concurrent transfer dupe test to verify locks and prevent race conditions."))
                .required("target", OfflinePlayerParser.offlinePlayerParser(), commandManager.parserRegistry().getSuggestionProvider("players").get())
                .optional("source", OfflinePlayerParser.offlinePlayerParser(), commandManager.parserRegistry().getSuggestionProvider("players").get())
                .optional("amount", DoubleParser.doubleParser(0.01), DefaultValue.constant(10.0))
                .optional("requests", IntegerParser.integerParser(1), DefaultValue.constant(20))
                .optional("currency", commandManager.componentBuilder(String::class.java, "currency").parser(CurrencyParser()).defaultValue(DefaultValue.parsed("dollars")))
                .permission("lite.eco.admin.debug.stress")
                .handler { context ->
                    val sender = context.sender().source()

                    val target: OfflinePlayer = context.get("target")

                    val sourcePlayer: OfflinePlayer? = context.getOrDefault("source", null)
                    val sourceUuid = sourcePlayer?.uniqueId ?: (sender as? Player)?.uniqueId
                    val sourceName = sourcePlayer?.name ?: (sender as? Player)?.name ?: "Console"

                    if (sourceUuid == null) {
                        val errorMsg = ModernText.miniModernText("<red>[LiteEco DupeTest] You must specify a source player when running this command from the console!</red>")
                        sender.sendMessage(errorMsg)
                        LiteEco.instance.logger.warn(errorMsg)
                        return@handler
                    }

                    val amountValue = context.getOrDefault("amount", 10.0)
                    val amount = BigDecimal.valueOf(amountValue)

                    val requestsCount = context.getOrDefault("requests", 20)
                    val currency = context.getOrDefault("currency", "dollars")

                    val targetUuid = target.uniqueId
                    val targetName = target.name ?: "Unknown"

                    val startMsg = ModernText.miniModernText("<yellow>[LiteEco DupeTest] Starting $requestsCount parallel transfers: $amount $currency from $sourceName -> $targetName...</yellow>")
                    sender.sendMessage(startMsg)
                    LiteEco.instance.logger.info(startMsg)

                    LiteEco.instance.pluginScope.launch {
                        val initialSourceBal = LiteEco.instance.api.account().getBalance(sourceUuid, currency)
                        val initialTargetBal = LiteEco.instance.api.account().getBalance(targetUuid, currency)

                        val results = Collections.synchronizedList(mutableListOf<Boolean>())

                        val executionTime = measureTimeMillis {
                            coroutineScope {
                                (1..requestsCount).map {
                                    async(Dispatchers.IO) {
                                        val success = LiteEco.instance.api.account().transfer(sourceUuid, targetUuid, currency, amount)
                                        results.add(success)
                                    }
                                }.awaitAll()
                            }
                        }

                        val successCount = results.count { it }
                        val failCount = results.count { !it }

                        val finalSourceBal = LiteEco.instance.api.account().getBalance(sourceUuid, currency)
                        val finalTargetBal = LiteEco.instance.api.account().getBalance(targetUuid, currency)

                        val expectedTransferredTotal = amount.multiply(BigDecimal.valueOf(successCount.toLong()))
                        val actualSourceDeduction = initialSourceBal.subtract(finalSourceBal)
                        val actualTargetAddition = finalTargetBal.subtract(initialTargetBal)

                        val isBalanceCorrect = actualSourceDeduction.compareTo(expectedTransferredTotal) == 0
                                && actualTargetAddition.compareTo(expectedTransferredTotal) == 0

                        val summaryComponent = ModernText.miniModernText("<yellow>[LiteEco DupeTest] Finished in ${executionTime}ms. (Success: <green>$successCount</green> | Rejected: <red>$failCount</red>)</yellow>")
                        val sourceBalComponent = ModernText.miniModernText("<gray>- Source balance ($sourceName): $initialSourceBal -> $finalSourceBal (Diff: $actualSourceDeduction)</gray>")
                        val targetBalComponent = ModernText.miniModernText("<gray>- Target balance ($targetName): $initialTargetBal -> $finalTargetBal (Diff: +$actualTargetAddition)</gray>")

                        sender.sendMessage(summaryComponent)
                        sender.sendMessage(sourceBalComponent)
                        sender.sendMessage(targetBalComponent)

                        LiteEco.instance.logger.info(summaryComponent)
                        LiteEco.instance.logger.info(sourceBalComponent)
                        LiteEco.instance.logger.info(targetBalComponent)

                        if (isBalanceCorrect && finalSourceBal >= BigDecimal.ZERO) {
                            val passComponent = ModernText.miniModernText("<green>[LiteEco DupeTest] PASSED: Lock held successfully! No money duplicated or overdrawn.</green>")
                            sender.sendMessage(passComponent)
                            LiteEco.instance.logger.info(passComponent)
                        } else {
                            val failComponent = ModernText.miniModernText("<red>[LiteEco DupeTest] FAILED: Race condition / Dupe detected! Total expected transferred: $expectedTransferredTotal</red>")
                            sender.sendMessage(failComponent)
                            LiteEco.instance.logger.warn(failComponent)
                        }
                    }
                }
        )
    }
}