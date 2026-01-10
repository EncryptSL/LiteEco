package com.github.encryptsl.lite.eco.commands.admin

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.commands.internal.EconomyCommand
import com.github.encryptsl.lite.eco.commands.parsers.AmountValidatorParser
import com.github.encryptsl.lite.eco.commands.parsers.CurrencyParser
import com.github.encryptsl.lite.eco.common.manager.economy.admin.EconomyGlobalSetHandler
import com.github.encryptsl.lite.eco.common.manager.economy.admin.EconomyMoneySetHandler
import org.bukkit.command.CommandSender
import org.incendo.cloud.Command
import org.incendo.cloud.bukkit.parser.selector.MultiplePlayerSelectorParser
import org.incendo.cloud.component.DefaultValue
import org.incendo.cloud.paper.PaperCommandManager
import org.incendo.cloud.paper.util.sender.Source
import java.math.BigDecimal

class EcoSetCmd(
    liteEco: LiteEco,
    private val economySet: EconomyMoneySetHandler,
    private val economyGlobalSet: EconomyGlobalSetHandler,
) : EconomyCommand(liteEco) {

    override fun register(
        commandManager: PaperCommandManager<Source>,
        base: Command.Builder<Source>
    ) {
        commandManager.command(
            base.literal("set")
                .permission("lite.eco.admin.set")
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
                .handler { ctx ->
                    val sender: CommandSender = ctx.sender().source()
                    val amount: BigDecimal = ctx.get("amount")
                    val currency: String = ctx.get("currency")

                    handleTargetLogic(
                        ctx, "lite.eco.admin.global.set",
                        onGlobal = {
                            economyGlobalSet.onAdminGlobalSetMoney(
                                sender,
                                currency,
                                amount,
                                it.toMutableList()
                            )
                        },
                        onSingle = { economySet.onAdminSetMoney(sender, it, currency, amount) }
                    )
                }
        )
    }
}