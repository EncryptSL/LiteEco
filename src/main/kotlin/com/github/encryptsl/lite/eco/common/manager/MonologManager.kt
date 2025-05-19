package com.github.encryptsl.lite.eco.common.manager

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.ComponentPaginator
import com.github.encryptsl.lite.eco.utils.Helper
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.command.CommandSender

class MonologManager(
    private val liteEco: LiteEco,
    private val helper: Helper
) {

    fun displayMonolog(sender: CommandSender, parameter: String, page: Int) {
        val log = helper.validateLog(parameter)
        val pagination = ComponentPaginator(log) { itemsPerPage = 10 }.apply { page(page) }

        if (pagination.isAboveMaxPage(page))
            return sender.sendMessage(liteEco.locale.translation("messages.error.maximum_page",
                Placeholder.parsed("max_page", pagination.maxPages.toString()))
            )

        val tags = TagResolver.resolver(Placeholder.parsed("page", page.toString()), Placeholder.parsed("max_page", pagination.maxPages.toString()))
        sender.sendMessage(liteEco.locale.translation("messages.monolog.header", tags))
        for (content in pagination.display()) {
            sender.sendMessage(content)
        }
        sender.sendMessage(liteEco.locale.translation("messages.monolog.footer", tags))
    }
}