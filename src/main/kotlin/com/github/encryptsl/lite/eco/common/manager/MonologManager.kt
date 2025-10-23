package com.github.encryptsl.lite.eco.common.manager

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.ComponentPaginator
import com.github.encryptsl.lite.eco.common.extensions.safeSendMessage
import com.github.encryptsl.lite.eco.utils.Helper
import kotlinx.coroutines.launch
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.command.CommandSender

class MonologManager(
    private val liteEco: LiteEco,
    private val helper: Helper
) {

    fun displayMonolog(sender: CommandSender, parameter: String, page: Int) {
        liteEco.pluginScope.launch {
            val log = helper.validateLog(parameter)
            val pagination = ComponentPaginator(log) {
                selectedPage = page
                itemsPerPage = 10
                headerFormat = liteEco.locale.getMessage("messages.monolog.header")
                navigationFormat = liteEco.locale.getMessage("messages.monolog.footer")
            }

            if (pagination.isAboveMaxPage(page))
                return@launch sender.safeSendMessage(liteEco,liteEco.locale.translation("messages.error.maximum_page",
                    Placeholder.parsed("max_page", pagination.maxPages.toString()))
                )
            pagination.header("").let { sender.safeSendMessage(liteEco,it) }
            pagination.display().forEach {
                sender.safeSendMessage(liteEco,it)
            }
            pagination.navigationBar("eco monolog", parameter).let { sender.safeSendMessage(liteEco,it) }
        }
    }
}