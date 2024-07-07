package com.github.encryptsl.lite.eco.utils

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.enums.CheckLevel
import com.github.encryptsl.lite.eco.common.database.entity.EconomyLog
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
    fun validateAmount(amountStr: String, commandSender: CommandSender, checkLevel: CheckLevel = CheckLevel.FULL): BigDecimal? {
        val amount = amountStr.toValidDecimal()
        return when {
            amount == null -> {
                commandSender.sendMessage(liteEco.locale.translation("messages.error.format_amount"))
                null
            }
            checkLevel == CheckLevel.ONLY_NEGATIVE && amount.isNegative() || checkLevel == CheckLevel.FULL && (amount.isApproachingZero()) -> {
                commandSender.sendMessage(liteEco.locale.translation("messages.error.negative_amount"))
                null
            }
            else -> amount
        }
    }

    fun validateLog(player: String?): List<EconomyLog> {
        val log = liteEco.loggerModel.getLog().thenApply { el ->
            if (player != null) {
                return@thenApply el.filter { l -> l.log.contains(player, true) }
            }
            return@thenApply el
        }
        return log.join()
    }

    fun getTopBalancesFormatted(currency: String): List<Component> {
        return liteEco.api.getTopBalance(currency).toList().positionIndexed { index, pair ->
            liteEco.locale.translation("messages.balance.top_format", TagResolver.resolver(
                Placeholder.parsed("position", index.toString()),
                Placeholder.parsed("player", pair.first),
                Placeholder.parsed("money", liteEco.api.fullFormatting(pair.second)),
                Placeholder.parsed("currency", liteEco.currencyImpl.getCurrencyName(currency))
            ))
        }
    }

    fun getComponentBal(user: User, currency: String): TagResolver {
        return TagResolver.resolver(
            Placeholder.parsed("target", user.userName),
            Placeholder.parsed("money", liteEco.api.fullFormatting(user.money)),
            Placeholder.parsed("currency", liteEco.currencyImpl.getCurrencyName(currency))
        )
    }
}