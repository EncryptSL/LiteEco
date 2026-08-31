package com.github.encryptsl.lite.eco.common.manager.importer.economies

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.common.hook.economy.cmi.CMIEconomyHook
import com.github.encryptsl.lite.eco.common.manager.importer.BaseEconomyImporter
import com.github.encryptsl.lite.eco.common.manager.importer.EconomyImportResults
import java.math.BigDecimal

class CMIEconomyImport(
    private val liteEco: LiteEco,
) : BaseEconomyImporter() {

    override val name: String = "CMI"

    override suspend fun import(
        pluginName: String,
        intoCurrency: String,
        fromCurrency: String?
    ): EconomyImportResults {
        val cmiEconomy = CMIEconomyHook(liteEco)

        return executeImport(name, liteEco, intoCurrency, offlinePlayers.asIterable()) { player ->
            val balance = cmiEconomy.getBalance(player.uniqueId) ?: BigDecimal.ZERO

            if (balance <= BigDecimal.ZERO) return@executeImport null

            Triple(player.uniqueId, player.name ?: "Unknown", balance)
        }
    }
}