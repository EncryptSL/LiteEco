package com.github.encryptsl.lite.eco.commands.admin

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.commands.internal.EconomyCommand
import com.github.encryptsl.lite.eco.commands.parsers.AmountValidatorParser
import com.github.encryptsl.lite.eco.commands.parsers.CurrencyParser
import com.github.encryptsl.lite.eco.common.manager.economy.admin.EconomyGlobalDepositHandler
import com.github.encryptsl.lite.eco.common.manager.economy.admin.EconomyMoneyDepositHandler
import org.incendo.cloud.Command
import org.incendo.cloud.bukkit.parser.selector.MultiplePlayerSelectorParser
import org.incendo.cloud.component.DefaultValue
import org.incendo.cloud.paper.PaperCommandManager
import org.incendo.cloud.paper.util.sender.Source
import org.incendo.cloud.parser.standard.BooleanParser
import java.math.BigDecimal

class EcoAddCmd(
    liteEco: LiteEco,
    private val economyDeposit: EconomyMoneyDepositHandler,
    private val globalEconomyDeposit: EconomyGlobalDepositHandler,
) : EconomyCommand(liteEco) {

    override fun register(
        commandManager: PaperCommandManager<Source>,
        base: Command.Builder<Source>
    ) {
        commandManager.command(
            base.literal("add")
                .permission("lite.eco.admin.add")
                .required("target", MultiplePlayerSelectorParser.multiplePlayerSelectorParser())
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
                    val amount: BigDecimal = ctx.get("amount")
                    val currency: String = ctx.get("currency")
                    val silent: Boolean = ctx.get("silent")

                    handleTargetLogic(
                        ctx, "lite.eco.admin.global.add",
                        onGlobal = {
                            globalEconomyDeposit.onAdminGlobalDepositMoney(
                                ctx.sender().source(),
                                currency,
                                amount,
                                it.toMutableList()
                            )
                        },
                        onSingle = {
                            economyDeposit.onAdminDepositMoney(
                                ctx.sender().source(),
                                it,
                                currency,
                                amount,
                                silent
                            )
                        }
                    )
                }
        )
    }
}