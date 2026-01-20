package com.github.encryptsl.lite.eco.commands

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.commands.internal.InternalCmd
import com.github.encryptsl.lite.eco.commands.player.MoneyBalanceCmd
import com.github.encryptsl.lite.eco.commands.player.MoneyPayCmd
import com.github.encryptsl.lite.eco.commands.player.MoneyTopCmd
import com.github.encryptsl.lite.eco.common.manager.economy.PlayerEconomyPayHandler
import com.github.encryptsl.lite.eco.utils.Helper
import org.bukkit.command.CommandSender
import org.incendo.cloud.description.Description
import org.incendo.cloud.kotlin.extension.buildAndRegister
import org.incendo.cloud.paper.PaperCommandManager
import org.incendo.cloud.paper.util.sender.Source

class MoneyCMD(
    private val liteEco: LiteEco
) : InternalCmd {

    companion object {
        private const val DESCRIPTION = "Provided plugin by LiteEco"
    }

    private val helper: Helper = Helper(liteEco)

    private val economyPay by lazy { PlayerEconomyPayHandler(liteEco) }

    override fun execute(commandManager: PaperCommandManager<Source>) {
        commandManager.buildAndRegister("money", Description.description(DESCRIPTION)) {
            permission("lite.eco.money")
            handler { ctx -> helpMessage(ctx.sender().source()) }

            commandManager.command(commandBuilder.literal("help").permission("lite.eco.money").handler { ctx ->
                helpMessage(ctx.sender().source())
            })

            val features = listOf(
                MoneyBalanceCmd(liteEco, helper),
                MoneyTopCmd(liteEco, helper),
                MoneyPayCmd(liteEco, economyPay)
            )

            features.forEach { it.register(commandManager, commandBuilder) }
        }
    }

    fun helpMessage(sender: CommandSender) {
        liteEco.locale.translationList("messages.help").forEach {
            sender.sendMessage(it)
        }
    }

}