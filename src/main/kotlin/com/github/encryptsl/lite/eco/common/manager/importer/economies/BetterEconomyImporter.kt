package com.github.encryptsl.lite.eco.common.manager.importer.economies

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.common.hook.economy.bettereconomy.BetterEconomyHook
import com.github.encryptsl.lite.eco.common.manager.importer.BaseEconomyImporter
import com.github.encryptsl.lite.eco.common.manager.importer.EconomyImportResults
import java.math.BigDecimal

class BetterEconomyImporter(
    private val liteEco: LiteEco
) : BaseEconomyImporter() {

    override val name = "BetterEconomy"

    override suspend fun import(
        pluginName: String,
        intoCurrency: String,
        fromCurrency: String?
    ): EconomyImportResults {
        val betterEconomy = BetterEconomyHook(liteEco)

        return executeImport(pluginName, liteEco, intoCurrency, offlinePlayers.asIterable()) { player ->
            val balance = betterEconomy.getBalance(player.uniqueId).toBigDecimal()

            if (balance <= BigDecimal.ZERO) return@executeImport null

            Triple(player.uniqueId, player.name ?: "Unknown", balance)
        }
    }
}