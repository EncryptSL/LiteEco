package com.github.encryptsl.lite.eco.commands

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.commands.admin.EcoAccountManagementCmd
import com.github.encryptsl.lite.eco.commands.admin.EcoAddCmd
import com.github.encryptsl.lite.eco.commands.admin.EcoSetCmd
import com.github.encryptsl.lite.eco.commands.admin.EcoWithdrawCmd
import com.github.encryptsl.lite.eco.commands.admin.subcommands.EcoConfigCmd
import com.github.encryptsl.lite.eco.commands.admin.subcommands.EcoDatabaseCmd
import com.github.encryptsl.lite.eco.commands.admin.subcommands.EcoDebugCmd
import com.github.encryptsl.lite.eco.commands.admin.subcommands.EcoMonologCmd
import com.github.encryptsl.lite.eco.common.manager.ExportManager
import com.github.encryptsl.lite.eco.common.manager.ImportManager
import com.github.encryptsl.lite.eco.common.manager.monolog.MonologManager
import com.github.encryptsl.lite.eco.common.manager.PurgeManager
import com.github.encryptsl.lite.eco.common.manager.economy.admin.*
import com.github.encryptsl.lite.eco.common.manager.importer.ImportEconomy
import com.github.encryptsl.lite.eco.utils.Helper
import org.bukkit.command.CommandSender
import org.incendo.cloud.description.Description
import org.incendo.cloud.kotlin.extension.buildAndRegister
import org.incendo.cloud.paper.PaperCommandManager
import org.incendo.cloud.paper.util.sender.Source

class EcoCMD(
    private val liteEco: LiteEco
) : InternalCmd {

    companion object {
        private const val DESCRIPTION = "Provided plugin by LiteEco"
    }

    private val helper: Helper = Helper(liteEco)
    private val importEconomy: ImportEconomy = ImportEconomy(liteEco)
    private val exportManager: ExportManager = ExportManager(liteEco)
    private val monologManager: MonologManager = MonologManager(liteEco, helper)
    private val importManager: ImportManager = ImportManager(liteEco, importEconomy)
    private val purgeManager: PurgeManager = PurgeManager(liteEco)

    private val globalEconomyDeposit by lazy { EconomyGlobalDepositHandler(liteEco) }
    private val globalEconomyWithdraw by lazy { EconomyGlobalWithdrawHandler(liteEco) }
    private val globalEconomySet by lazy { EconomyGlobalSetHandler(liteEco) }
    private val economyDeposit by lazy { EconomyMoneyDepositHandler(liteEco) }
    private val economySet by lazy { EconomyMoneySetHandler(liteEco) }
    private val economyWithdraw by lazy { EconomyMoneyWithdrawHandler(liteEco) }

    override fun execute(commandManager: PaperCommandManager<Source>) {
        commandManager.buildAndRegister("eco", Description.description(DESCRIPTION)) {
            permission("lite.eco.admin.eco")
            handler { ctx -> helpMessage(ctx.sender().source()) }

            commandManager.command(
                commandBuilder.literal("help")
                    .permission("lite.eco.admin.help")
                    .handler { ctx ->
                        helpMessage(ctx.sender().source())
                    })

            val features = listOf(
                EcoAddCmd(liteEco, economyDeposit, globalEconomyDeposit),
                EcoSetCmd(liteEco, economySet, globalEconomySet),
                EcoWithdrawCmd(liteEco, economyWithdraw, globalEconomyWithdraw),
                EcoAccountManagementCmd(liteEco),
                EcoDatabaseCmd(liteEco, importEconomy, purgeManager, exportManager, importManager),
                EcoConfigCmd(liteEco),
                EcoDebugCmd(helper),
                EcoMonologCmd(monologManager)
            )

            features.forEach { it.register(commandManager, commandBuilder) }
        }
    }

    private fun helpMessage(sender: CommandSender) {
        liteEco.locale.translationList("messages.admin-help").forEach {
            sender.sendMessage(it)
        }
    }

}