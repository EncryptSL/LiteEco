package com.github.encryptsl.lite.eco.common.manager.importer.economies

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.common.hook.economy.playerpoints.PlayerPointsHook
import com.github.encryptsl.lite.eco.common.manager.importer.BaseEconomyImporter
import com.github.encryptsl.lite.eco.common.manager.importer.EconomyImportResults
import java.math.BigDecimal

class PlayerPointsImporter(
    private val liteEco: LiteEco
) : BaseEconomyImporter() {

    override val name: String = "PlayerPoints"

    override suspend fun import(
        pluginName: String,
        intoCurrency: String,
        fromCurrency: String?
    ): EconomyImportResults {

        val playerPointsHook = PlayerPointsHook(liteEco)

        return executeImport(pluginName, liteEco, intoCurrency, offlinePlayers.asIterable()) { player ->
            val balance = playerPointsHook.getBalance(player.uniqueId).toBigDecimal()

            if (balance <= BigDecimal.ZERO) return@executeImport null

            Triple(player.uniqueId, player.name ?: "Unknown", balance)
        }
    }

}