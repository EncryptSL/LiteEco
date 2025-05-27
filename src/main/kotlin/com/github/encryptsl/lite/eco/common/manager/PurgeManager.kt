package com.github.encryptsl.lite.eco.common.manager

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.enums.PurgeKey
import kotlinx.coroutines.launch
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.command.CommandSender

class PurgeManager(private val liteEco: LiteEco) {

    fun purge(sender: CommandSender, purgeKey: PurgeKey, currency: String) {
        liteEco.pluginScope.launch {
            @Suppress("REDUNDANT_ELSE_IN_WHEN")
            when (purgeKey) {
                PurgeKey.ACCOUNTS -> {
                    liteEco.suspendApiWrapper.purgeAccounts(currency)
                    sender.sendMessage(liteEco.locale.translation("messages.admin.purge_accounts"))
                }
                PurgeKey.NULL_ACCOUNTS -> {
                    liteEco.suspendApiWrapper.purgeInvalidAccounts(currency)
                    sender.sendMessage(liteEco.locale.translation("messages.admin.purge_null_accounts"))
                }
                PurgeKey.DEFAULT_ACCOUNTS -> {
                    liteEco.suspendApiWrapper.purgeDefaultAccounts(currency,liteEco.currencyImpl.getCurrencyStartBalance(currency))
                    sender.sendMessage(liteEco.locale.translation("messages.admin.purge_default_accounts"))
                }
                PurgeKey.MONO_LOG -> {
                    val monolog = liteEco.loggerModel.getLog()
                    if (monolog.isEmpty()) {
                        return@launch sender.sendMessage(liteEco.locale.translation("messages.error.purge_monolog_fail"))
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

}