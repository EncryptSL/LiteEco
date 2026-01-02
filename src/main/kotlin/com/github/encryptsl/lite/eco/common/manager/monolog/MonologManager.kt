package com.github.encryptsl.lite.eco.common.manager.monolog

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.ComponentPaginator
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
            val result = helper.validateLog(parameter, page)

            val pagination = ComponentPaginator(result.components) {
                selectedPage = page
                itemsPerPage = 10
                manualMaxPages = result.totalPages
                headerFormat = liteEco.locale.getMessage("messages.monolog.header")
                navigationFormat = liteEco.locale.getMessage("messages.monolog.footer")
            }

            if (page > result.totalPages || result.components.isEmpty()) {
                sender.sendMessage(liteEco.locale.translation("messages.error.maximum_page",
                    Placeholder.parsed("max_page", result.totalPages.toString()))
                )
                return@launch
            }

            sender.sendMessage(pagination.header(""))

            result.components.forEach { sender.sendMessage(it) }

            sender.sendMessage(pagination.navigationBar("eco monolog", parameter))
        }
    }
}