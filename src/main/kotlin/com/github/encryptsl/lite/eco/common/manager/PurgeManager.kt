package com.github.encryptsl.lite.eco.common.manager

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.enums.PurgeKey
import kotlinx.coroutines.launch
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.command.CommandSender

class PurgeManager(private val liteEco: LiteEco) {

    fun purge(sender: CommandSender, purgeKey: PurgeKey, currency: String) {
        liteEco.pluginScope.launch {
            @Suppress("REDUNDANT_ELSE_IN_WHEN")
            val (count, messageKey) = when (purgeKey) {
                PurgeKey.ACCOUNTS -> {
                    liteEco.api.purgeAccounts(currency) to "messages.admin.purge_accounts"
                }
                PurgeKey.TEST_ACCOUNTS -> {
                    liteEco.api.purgeTestAccounts() to "messages.admin.purge_test_accounts"
                }
                PurgeKey.NULL_ACCOUNTS -> {
                    liteEco.api.purgeInvalidAccounts(currency) to "messages.admin.purge_null_accounts"
                }
                PurgeKey.DEFAULT_ACCOUNTS -> {
                    val defaultMoney = liteEco.currencyImpl.getCurrencyStartBalance(currency)
                    liteEco.api.purgeDefaultAccounts(currency, defaultMoney) to "messages.admin.purge_default_accounts"
                }
                PurgeKey.MONO_LOG -> {
                    liteEco.loggerModel.clearLogs() to "messages.admin.purge_monolog_success"
                }
                else -> {
                    sender.sendMessage(liteEco.locale.translation("messages.error.purge_argument"))
                    return@launch
                }
            }

            if (count > 0) {
                sender.sendMessage(
                    liteEco.locale.translation(messageKey, TagResolver.resolver(
                        Placeholder.parsed("deleted", count.toString())
                    ))
                )
            } else {
                sender.sendMessage(liteEco.locale.translation("messages.error.purge_fail"))
            }
        }
    }

}