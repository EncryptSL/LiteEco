package encryptsl.cekuj.net.utils

import encryptsl.cekuj.net.LiteEco
import encryptsl.cekuj.net.api.enums.CheckLevel
import encryptsl.cekuj.net.api.objects.ModernText
import encryptsl.cekuj.net.extensions.isApproachingZero
import encryptsl.cekuj.net.extensions.isNegative
import encryptsl.cekuj.net.extensions.toValidDecimal
import org.bukkit.command.CommandSender

class Helper(private val liteEco: LiteEco) {
    fun validateAmount(amountStr: String, commandSender: CommandSender, checkLevel: CheckLevel = CheckLevel.FULL): Double? {
        val amount = amountStr.toValidDecimal()
        return when {
            amount == null -> {
                commandSender.sendMessage(ModernText.miniModernText(liteEco.locale.getMessage("messages.error.format_amount")))
                null
            }
            checkLevel == CheckLevel.ONLY_NEGATIVE && amount.isNegative() || checkLevel == CheckLevel.FULL && (amount.isApproachingZero()) -> {
                commandSender.sendMessage(ModernText.miniModernText(liteEco.locale.getMessage("messages.error.negative_amount")))
                null
            }
            else -> amount
        }
    }
}