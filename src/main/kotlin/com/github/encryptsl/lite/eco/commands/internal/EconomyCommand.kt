package com.github.encryptsl.lite.eco.commands.internal

import com.github.encryptsl.lite.eco.LiteEco
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.incendo.cloud.bukkit.data.MultiplePlayerSelector
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.paper.util.sender.Source

/**
 * An abstract base class for economy-related commands that provides shared logic
 * for handling complex target selectors (e.g., @a, @p) and offline players.
 */
abstract class EconomyCommand(private val liteEco: LiteEco) : CommandFeature {

    /**
     * Orchestrates the target selection logic to distinguish between global actions
     * (multiple players via selectors) and single-target actions (individual or offline players).
     *
     * @param ctx The command context containing the "target" argument.
     * @param permissionGlobal The permission node required to perform this action on multiple targets.
     * @param onGlobal Callback executed when a global selector (like @a) is used.
     * @param onSingle Callback executed when a single player (online or offline) is targeted.
     */
    protected fun handleTargetLogic(
        ctx: CommandContext<Source>,
        permissionGlobal: String,
        onGlobal: (Collection<OfflinePlayer>) -> Unit,
        onSingle: (OfflinePlayer) -> Unit
    ) {
        val sender = ctx.sender().source()
        val selector: MultiplePlayerSelector = ctx.get("target")
        val input = selector.inputString()
        val players = selector.values()

        // Case 1: Multiple players found via a selector (e.g., @a, @r, @p with counts)
        if (input.startsWith("@") && players.size > 1) {
            if (!sender.hasPermission(permissionGlobal)) return
            onGlobal(players.toMutableList())
            return
        }

        // Case 2: Explicit use of @a when no players are currently online
        // This ensures the global action can still target the entire database/server if intended.
        if (input.startsWith("@a") && players.isEmpty()) {
            if (!sender.hasPermission(permissionGlobal)) return
            onGlobal(Bukkit.getOfflinePlayers().toList())
            return
        }

        // Case 3: Direct input that is not a selector or a selector that returned nothing online.
        // Attempts to find a cached offline player by name.
        if (!input.startsWith("@") || players.isEmpty()) {
            val offlinePlayer = Bukkit.getOfflinePlayerIfCached(input)
                ?: return sender.sendMessage(liteEco.locale.translation("messages.error.account_not_exist",
                    Placeholder.parsed("account", input)))
            onSingle(offlinePlayer)
            return
        }

        // Case 4: A selector was used but it resolved to exactly one online player.
        onSingle(players.single())
    }
}