package com.github.encryptsl.lite.eco.utils

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.economy.EconomyOperations
import com.github.encryptsl.lite.eco.api.enums.CheckLevel
import com.github.encryptsl.lite.eco.common.database.entity.User
import com.github.encryptsl.lite.eco.common.extensions.isApproachingZero
import com.github.encryptsl.lite.eco.common.extensions.isNegative
import com.github.encryptsl.lite.eco.common.extensions.positionIndexed
import com.github.encryptsl.lite.eco.common.extensions.toValidDecimal
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.command.CommandSender
import java.math.BigDecimal

class Helper(private val liteEco: LiteEco) {
    fun validateAmount(amountStr: String, sender: CommandSender, level: CheckLevel = CheckLevel.FULL): BigDecimal? {
        val amount = amountStr.toValidDecimal() ?: run {
            sender.sendMessage(liteEco.locale.translation("messages.error.format_amount"))
            return null
        }

        val isInvalid = when (level) {
            CheckLevel.ONLY_NEGATIVE -> amount.isNegative()
            CheckLevel.FULL -> amount.isApproachingZero()
        }

        if (isInvalid) {
            sender.sendMessage(liteEco.locale.translation("messages.error.negative_amount"))
            return null
        }

        return amount
    }

    suspend fun validateLog(parameter: String): List<Component> {
        val log = liteEco.loggerModel.getLog()
            .let { if (parameter != "all") it.filter { p -> p.target == parameter } else it }

        return log.map { el ->
            liteEco.loggerModel.message("messages.monolog.formatting",
                EconomyOperations.valueOf(el.action),
                el.sender,
                el.target,
                el.currency,
                el.previousBalance,
                el.newBalance,
                el.timestamp
            )
        }
    }

    fun getTopBalancesFormatted(currency: String): List<Component> {
        return liteEco.api.getTopBalance(currency).toList().positionIndexed { index, pair ->
                liteEco.locale.translation("messages.balance.top_format", TagResolver.resolver(
                    Placeholder.parsed("position", index.toString()),
                    Placeholder.parsed("player", pair.first),
                    Placeholder.parsed("money", liteEco.api.fullFormatting(pair.second, currency)),
                    Placeholder.parsed("currency", liteEco.currencyImpl.currencyModularNameConvert(currency, pair.second))
                ))
        }
    }

    fun getComponentBal(user: User, currency: String): TagResolver {
        return TagResolver.resolver(
            Placeholder.parsed("target", user.userName),
            Placeholder.parsed("money", liteEco.api.fullFormatting(user.money, currency)),
            Placeholder.parsed("currency", liteEco.currencyImpl.currencyModularNameConvert(currency, user.money))
        )
    }
}