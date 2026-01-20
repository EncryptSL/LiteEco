package com.github.encryptsl.lite.eco.common.manager.importer.economies

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.common.hook.economy.ezeconomy.EzEconomyHook
import com.github.encryptsl.lite.eco.common.manager.importer.BaseEconomyImporter
import com.github.encryptsl.lite.eco.common.manager.importer.EconomyImportResults
import java.math.BigDecimal

class EzEconomyImporter(
    private val liteEco: LiteEco,
) : BaseEconomyImporter() {

    override val name = "EzEconomy"
    override val isMultiCurrency: Boolean = true

    override suspend fun import(
        pluginName: String,
        intoCurrency: String,
        fromCurrency: String?
    ): EconomyImportResults {
        val ezEconomy = EzEconomyHook(liteEco)
        val source = fromCurrency ?: throw IllegalArgumentException("Source currency required")

        return executeImport(pluginName, liteEco, intoCurrency, offlinePlayers.asIterable()) { player ->
            val balance = ezEconomy.getBalance(player.uniqueId, source).toBigDecimal()

            if (balance <= BigDecimal.ZERO) return@executeImport null

            Triple(player.uniqueId, player.name ?: "Unknown", balance)
        }
    }

}