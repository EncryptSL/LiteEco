package com.github.encryptsl.lite.eco.api

import net.kyori.adventure.text.Component
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

class ComponentPaginator(private val components: List<Component>, options: PaginationOptions.() -> Unit = {}) {

    private val paginationOptions = PaginationOptions().apply(options)
    val maxPages = ceil(components.size.toDouble() / paginationOptions.itemsPerPage).toInt()

    fun currentPage() = paginationOptions.selectedPage
    fun isAboveMaxPage(page: Int) = page > maxPages

    fun page(newPage: Int) {
        val targetPage = min(max(newPage, 1), maxPages)
        this.paginationOptions.selectedPage = targetPage
    }

    fun display(): List<Component> {
        val selectedPage = paginationOptions.selectedPage
        val itemsPerPage = paginationOptions.itemsPerPage

        val indexStart = (selectedPage - 1) * itemsPerPage
        val indexEnd = min(indexStart + itemsPerPage, components.size)

        return components.subList(indexStart, indexEnd)
    }


    inner class PaginationOptions(
        var selectedPage: Int = 1,
        var itemsPerPage: Int = 10,
    )
}