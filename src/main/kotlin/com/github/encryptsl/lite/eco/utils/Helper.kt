package com.github.encryptsl.lite.eco.utils

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.enums.CheckLevel
import com.github.encryptsl.lite.eco.common.database.entity.EconomyLog
import com.github.encryptsl.lite.eco.common.extensions.isApproachingZero
import com.github.encryptsl.lite.eco.common.extensions.isNegative
import com.github.encryptsl.lite.eco.common.extensions.positionIndexed
import com.github.encryptsl.lite.eco.common.extensions.toValidDecimal
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import java.util.*

class Helper(private val liteEco: LiteEco) {
    fun validateAmount(amountStr: String, commandSender: CommandSender, checkLevel: CheckLevel = CheckLevel.FULL): Double? {
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
        val log = liteEco.loggerModel.getLog()

        if (player != null) {
            return log.filter { l -> l.log.contains(player, true) }
        }
        return log
    }

    fun getTopBalancesFormatted(): List<String> {
        return liteEco.api.getTopBalance().toList().positionIndexed { index, pair ->
            liteEco.locale.getMessage("messages.balance.top_format")
                .replace("<position>", index.toString())
                .replace("<player>", Bukkit.getOfflinePlayer(UUID.fromString(pair.first)).name.toString())
                .replace("<money>", liteEco.api.fullFormatting(pair.second))
        }
    }

    fun getComponentBal(offlinePlayer: OfflinePlayer): TagResolver {
        return TagResolver.resolver(
            Placeholder.parsed("target", offlinePlayer.name.toString()),
            Placeholder.parsed("money", liteEco.api.fullFormatting(liteEco.api.getBalance(offlinePlayer)))
        )
    }
}