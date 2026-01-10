package com.github.encryptsl.lite.eco.commands.admin.subcommands

import com.github.encryptsl.lite.eco.commands.internal.CommandFeature
import com.github.encryptsl.lite.eco.common.database.models.DatabaseEcoModel
import com.github.encryptsl.lite.eco.utils.Helper
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.incendo.cloud.Command
import org.incendo.cloud.bukkit.parser.OfflinePlayerParser
import org.incendo.cloud.bukkit.parser.PlayerParser
import org.incendo.cloud.component.DefaultValue
import org.incendo.cloud.description.Description
import org.incendo.cloud.paper.PaperCommandManager
import org.incendo.cloud.paper.util.sender.Source
import org.incendo.cloud.parser.standard.IntegerParser

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
                .commandDescription(Description.description("Toggle database failure simulation (writes will fail)."))
                .permission("lite.eco.admin.debug.failmode")
                .handler { context ->
                    DatabaseEcoModel.debugFailMode = !DatabaseEcoModel.debugFailMode
                    val status = if (DatabaseEcoModel.debugFailMode) "§cENABLED (Error)" else "§aDISABLED (OK)"
                    context.sender().source().sendMessage("§8[§bLiteEco§8] §7Simulated DB failure: $status")
                }
        )

        commandManager.command(
            debugSubCommand.literal("test-janitor")
                .commandDescription(Description.description("Run automated persistence test (failMode + deposit)."))
                .required("target", PlayerParser.playerParser())
                .permission("lite.eco.admin.debug.testjanitor")
                .handler { context ->
                    val target: Player = context.get("target")
                    helper.executeJanitorTest(target)
                }
        )

        commandManager.command(
            debugSubCommand.literal("janitor")
                .commandDescription(Description.description("Force immediate synchronization of all offline players in cache."))
                .permission("lite.eco.admin.debug.janitor")
                .handler { context ->
                    helper.forceJanitorSync(context.sender().source())
                }
        )

        commandManager.command(
            debugSubCommand.literal("inspect")
                .commandDescription(Description.description("View detailed cache content (failed currencies) for a player."))
                .required("target", OfflinePlayerParser.offlinePlayerParser())
                .permission("lite.eco.admin.debug.inspect")
                .handler { context ->
                    val target: OfflinePlayer = context.get("target")
                    helper.inspectCache(context.sender().source(), target.uniqueId)
                }
        )

        commandManager.command(
            debugSubCommand.literal("stress")
                .commandDescription(Description.description("Run simultaneous stress test of transaction atomicity."))
                .required("target", PlayerParser.playerParser())
                .optional("iterations", IntegerParser.integerParser(1, 1000), DefaultValue.constant(100))
                .permission("lite.eco.admin.debug.stress")
                .handler { context ->
                    val target: Player = context.get("target")
                    val iterations: Int = context.get("iterations")
                    helper.executeStressTest(target, 1.0, iterations)
                }
        )
    }
}