package com.github.encryptsl.lite.eco.common.manager

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.enums.PurgeKey
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.command.CommandSender

class PurgeManager(private val liteEco: LiteEco) {

    fun purge(sender: CommandSender, purgeKey: PurgeKey, currency: String) {
        @Suppress("REDUNDANT_ELSE_IN_WHEN")
        when (purgeKey) {
            PurgeKey.ACCOUNTS -> {
                liteEco.databaseEcoModel.purgeAccounts(currency)
                sender.sendMessage(liteEco.locale.translation("messages.admin.purge_accounts"))
            }
            PurgeKey.NULL_ACCOUNTS -> {
                liteEco.databaseEcoModel.purgeInvalidAccounts(currency)
                sender.sendMessage(liteEco.locale.translation("messages.admin.purge_null_accounts"))
            }
            PurgeKey.DEFAULT_ACCOUNTS -> {
                liteEco.databaseEcoModel.purgeDefaultAccounts(liteEco.currencyImpl.getCurrencyStartBalance(currency), currency)
                sender.sendMessage(liteEco.locale.translation("messages.admin.purge_default_accounts"))
            }
            PurgeKey.MONO_LOG -> {
                val monolog = liteEco.loggerModel.getLog()
                if (monolog.isEmpty()) {
                    return sender.sendMessage(liteEco.locale.translation("messages.error.purge_monolog_fail"))
                }
                liteEco.loggerModel.clearLogs()
                sender.sendMessage(liteEco.locale.translation("messages.admin.purge_monolog_success", Placeholder.parsed("deleted", monolog.size.toString())))
            }
            else -> {
                sender.sendMessage(liteEco.locale.translation("messages.error.purge_argument"))
            }
        }
    }

}