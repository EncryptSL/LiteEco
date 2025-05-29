package com.github.encryptsl.lite.eco.api

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.objects.ModernText
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

class ComponentPaginator(
    private val components: List<Component>,
    options: PaginationOptions.() -> Unit = {}
) {

    private val paginationOptions = PaginationOptions().apply(options)

    val maxPages get() = ceil(components.size.toDouble() / paginationOptions.itemsPerPage).toInt()
    fun currentPage() = paginationOptions.selectedPage

    fun isAboveMaxPage(page: Int) = page > maxPages

    fun page(newPage: Int) {
        val targetPage = min(max(newPage, 1), maxPages)
        paginationOptions.selectedPage = targetPage
    }

    fun display(): List<Component> {
        val selectedPage = paginationOptions.selectedPage
        val itemsPerPage = paginationOptions.itemsPerPage

        if (components.isEmpty()) return emptyList()

        val indexStart = (selectedPage - 1) * itemsPerPage
        val indexEnd = min(indexStart + itemsPerPage, components.size)

        return components.subList(indexStart, indexEnd)
    }

    fun displayPage(page: Int): List<Component> {
        val targetPage = min(max(page, 1), maxPages)
        val indexStart = (targetPage - 1) * paginationOptions.itemsPerPage
        val indexEnd = min(indexStart + paginationOptions.itemsPerPage, components.size)

        return components.subList(indexStart, indexEnd)
    }

    /**
     * Generates translated and styled pagination component, inserted into custom wrapper.
     */
    fun navigationBar(commandLabel: String, param: String = ""): Component {
        val page = currentPage()
        val prevPage = page - 1
        val nextPage = page + 1

        val prevComponent = if (page > 1) {
            LiteEco.instance.locale.translation("messages.pagination.previous")
                .color(NamedTextColor.GRAY)
                .clickEvent(ClickEvent.runCommand("/$commandLabel $prevPage $param"))
                .decorate(TextDecoration.UNDERLINED)
        } else {
            LiteEco.instance.locale.translation("messages.pagination.previous")
                .color(NamedTextColor.DARK_GRAY)
        }

        val nextComponent = if (page < maxPages) {
            LiteEco.instance.locale.translation("messages.pagination.next")
                .color(NamedTextColor.GRAY)
                .clickEvent(ClickEvent.runCommand("/$commandLabel $nextPage $param"))
                .decorate(TextDecoration.UNDERLINED)
        } else {
            LiteEco.instance.locale.translation("messages.pagination.next")
                .color(NamedTextColor.DARK_GRAY)
        }

        val center = LiteEco.instance.locale.translation("messages.pagination.center", TagResolver.resolver(
            Placeholder.parsed("page", page.toString()),
            Placeholder.parsed("max_page", maxPages.toString())
        ))

        val joined = Component.join(
            JoinConfiguration.separator(Component.space()),
            listOf(prevComponent, center, nextComponent)
        )

        return injectIntoFormat(paginationOptions.navigationFormat, joined)
    }

    fun header(commandLabel: String): Component {
        val headerFormat = paginationOptions.headerFormat
        val baseNav = navigationBar(commandLabel)
        return injectIntoFormat(headerFormat, baseNav)
    }

    /**
     * Injects pagination component into provided format with placeholder <pagination>
     */
    private fun injectIntoFormat(format: String, paginationComponent: Component): Component {
        val template = format.replace("<pagination>", "<insertion>")
        val built = ModernText.miniModernText(template)
        return built.replaceText {
            it.matchLiteral("<insertion>").replacement(paginationComponent)
        }
    }

    inner class PaginationOptions(
        var selectedPage: Int = 1,
        var itemsPerPage: Int = 10,
        var navigationFormat: String = "<gray><st>─━━━━━━─</st> <blue>[ <yellow><pagination><blue> ] <gray><st>─━━━━━━─</st>",
        var headerFormat: String = "<gray><st>─━━━━━━─</st> <blue>[ <yellow>LiteEco<blue> ] <gray><st>─━━━━━━─</st>"
    )
}