package com.github.encryptsl.lite.eco.commands.admin

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.commands.internal.EconomyCommand
import com.github.encryptsl.lite.eco.commands.parsers.AmountValidatorParser
import com.github.encryptsl.lite.eco.commands.parsers.CurrencyParser
import com.github.encryptsl.lite.eco.common.manager.economy.admin.EconomyGlobalWithdrawHandler
import com.github.encryptsl.lite.eco.common.manager.economy.admin.EconomyMoneyWithdrawHandler
import org.bukkit.command.CommandSender
import org.incendo.cloud.Command
import org.incendo.cloud.bukkit.parser.selector.MultiplePlayerSelectorParser
import org.incendo.cloud.component.DefaultValue
import org.incendo.cloud.description.Description
import org.incendo.cloud.paper.PaperCommandManager
import org.incendo.cloud.paper.util.sender.Source
import org.incendo.cloud.parser.standard.BooleanParser
import java.math.BigDecimal
import kotlin.collections.toMutableList

class EcoWithdrawCmd(
    liteEco: LiteEco,
    private val economyWithdraw: EconomyMoneyWithdrawHandler,
    private val economyWithdrawGlobal: EconomyGlobalWithdrawHandler,
) : EconomyCommand(liteEco) {
    override fun register(
        commandManager: PaperCommandManager<Source>,
        base: Command.Builder<Source>
    ) {
        commandManager.command(
            base.literal("withdraw")
                .commandDescription(Description.description(DESCRIPTION))
                .permission("lite.eco.admin.withdraw")
                .required(
                    "target",
                    MultiplePlayerSelectorParser.multiplePlayerSelectorParser(),
                )
                .required(
                    commandManager
                        .componentBuilder(BigDecimal::class.java, "amount")
                        .parser(AmountValidatorParser())
                        .defaultValue(DefaultValue.constant(BigDecimal.ONE))
                )
                .optional(
                    commandManager
                        .componentBuilder(String::class.java, "currency")
                        .parser(CurrencyParser())
                        .defaultValue(DefaultValue.parsed("dollars"))
                )
                .optional(
                    "silent",
                    BooleanParser.booleanParser(false),
                    DefaultValue.constant(false)
                )
                .handler { ctx ->
                    val sender: CommandSender = ctx.sender().source()
                    val amount: BigDecimal = ctx.get("amount")
                    val currency: String = ctx.get("currency")
                    val silent: Boolean = ctx.get("silent")

                    handleTargetLogic(
                        ctx, "lite.eco.admin.global.withdraw",
                        onGlobal = {
                            economyWithdrawGlobal.onAdminGlobalWithdrawMoney(
                                sender,
                                currency,
                                amount,
                                it.toMutableList()
                            )
                        },
                        onSingle = { economyWithdraw.onAdminWithdrawMoney(sender, it, currency, amount, silent) }
                    )
                })
    }
}