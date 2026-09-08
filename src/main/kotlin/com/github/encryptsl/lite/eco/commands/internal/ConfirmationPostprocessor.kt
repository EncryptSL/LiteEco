package com.github.encryptsl.lite.eco.commands.internal

import com.github.benmanes.caffeine.cache.Caffeine
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.execution.postprocessor.CommandPostprocessingContext
import org.incendo.cloud.execution.postprocessor.CommandPostprocessor
import org.incendo.cloud.key.CloudKey
import org.incendo.cloud.services.type.ConsumerService
import java.time.Duration
import java.util.*

class ConfirmationPostprocessor<C : Any>(
    private val timeoutSeconds: Long = 15,
    private val bypassPermission: String? = "lite.eco.admin.bypass.confirmation",
    private val getUuid: (C) -> UUID?,
    private val hasPermission: (C, String) -> Boolean,
    private val sendNotification: (C, CommandContext<C>) -> Unit
) : CommandPostprocessor<C> {

    companion object {
        val CONFIRMATION_REQUIRED_KEY: CloudKey<Boolean> =
            CloudKey.of("custom_requires_confirmation", Boolean::class.javaObjectType)
    }

    private val cache = Caffeine.newBuilder()
        .expireAfterWrite(Duration.ofSeconds(timeoutSeconds))
        .build<UUID, String>()

    override fun accept(context: CommandPostprocessingContext<C>) {
        val command = context.command()

        val requiresConfirmation = command.commandMeta()
            .getOrDefault(CONFIRMATION_REQUIRED_KEY, false)

        if (!requiresConfirmation) return

        val commandContext = context.commandContext()
        val sender = commandContext.sender()

        if (bypassPermission != null && hasPermission(sender, bypassPermission)) {
            return
        }

        val uuid = getUuid(sender) ?: return

        val inputString = commandContext.rawInput().input()
        val pendingInput = cache.getIfPresent(uuid)

        if (pendingInput != null && pendingInput == inputString) {
            cache.invalidate(uuid)
        } else {
            cache.put(uuid, inputString)
            sendNotification(sender, commandContext)

            ConsumerService.interrupt()
        }
    }

    fun <T : C> applyToBuilder(builder: org.incendo.cloud.Command.Builder<T>): org.incendo.cloud.Command.Builder<T> {
        return builder.meta(CONFIRMATION_REQUIRED_KEY, true)
    }
}