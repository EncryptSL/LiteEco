package com.github.encryptsl.lite.eco.common.manager.importer.economies

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.common.hook.economy.theosiseconomy.TheosisEconomyHook
import com.github.encryptsl.lite.eco.common.manager.importer.BaseEconomyImporter
import com.github.encryptsl.lite.eco.common.manager.importer.EconomyImportResults
import java.math.BigDecimal

class TheosisEconomyImporter(
    private val liteEco: LiteEco
) : BaseEconomyImporter() {

    override val name = "TheosisEconomy"

    override suspend fun import(
        pluginName: String,
        intoCurrency: String,
        fromCurrency: String?
    ): EconomyImportResults {
        val theosisEconomyHook = TheosisEconomyHook(liteEco)

        return executeImport(name, liteEco, pluginName, offlinePlayers.asIterable()) { player ->
            val balance = theosisEconomyHook.getBalance(player.uniqueId)

            if (balance <= BigDecimal.ZERO) return@executeImport null

            Triple(player.uniqueId, player.name ?: "Unknown", balance)
        }
    }
}