package encryptsl.cekuj.net.api

import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

class Paginator(private val items: List<String>, options: PaginationOptions.() -> Unit = {}) {

    private val paginationOptions = PaginationOptions().apply(options)
    val maxPages = ceil(items.size.toDouble() / paginationOptions.itemsPerPage).toInt()

    fun page() = paginationOptions.selectedPage

    fun page(newPage: Int) {
        val targetPage = min(max(newPage, 1), maxPages)
        this.paginationOptions.selectedPage = targetPage
    }

    fun display(): String {
        val selectedPage = paginationOptions.selectedPage
        val itemsPerPage = paginationOptions.itemsPerPage

        val indexStart = (selectedPage - 1) * itemsPerPage
        val indexEnd = min(indexStart + itemsPerPage, items.size) - 1
        // IntRange is end inclusive.

        return (indexStart until indexEnd).joinToString("\n", transform = items::elementAt)
    }

    inner class PaginationOptions(
        var selectedPage: Int = 1,
        var itemsPerPage: Int = 10
    )
}