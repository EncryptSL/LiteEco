package com.github.encryptsl.lite.eco.commands.admin.subcommands

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.economy.account.AccountCache
import com.github.encryptsl.lite.eco.api.objects.ModernText
import com.github.encryptsl.lite.eco.commands.internal.CommandFeature
import com.github.encryptsl.lite.eco.common.database.models.DatabaseEcoModel
import com.github.encryptsl.lite.eco.utils.Helper
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
                    val accountCount: Int = context.get("accounts")

                    sender.sendMessage(ModernText.miniModernText("<yellow>[LiteEco Debug] Preparing $accountCount test accounts in database and cache...</yellow>"))

                    val generatedUuids = Collections.synchronizedList(mutableListOf<UUID>())
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

                                    AccountCache.cache(testUuid, currency, randomAmount)
                                    generatedUuids.add(testUuid)
                                }
                                yield()
                            }
                        }

                        sender.sendMessage(ModernText.miniModernText("<yellow>[LiteEco Debug] Injected $accountCount accounts in ${preparationTime}ms. Executing AccountCache.syncAccounts()...</yellow>"))

                        val syncTime = measureTimeMillis {
                            AccountCache.syncAccounts()
                        }

                        val remainingInCache = generatedUuids.count { AccountCache.isAccountCached(it, null) }

                        if (remainingInCache == 0) {
                            sender.sendMessage(ModernText.miniModernText("<green>[LiteEco Debug] PASSED: All $accountCount accounts were saved to DB and cleared from cache in ${syncTime}ms.</green>"))
                        } else {
                            sender.sendMessage(ModernText.miniModernText("<red>[LiteEco Debug] FAILED: $remainingInCache / $accountCount accounts remained in cache after sync!</red>"))
                        }
                    }
                }
        )
    }
}