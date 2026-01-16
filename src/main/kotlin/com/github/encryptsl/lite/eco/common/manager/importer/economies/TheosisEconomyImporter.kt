package com.github.encryptsl.lite.eco.common.manager.importer.economies

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.interfaces.EconomyImporter
import com.github.encryptsl.lite.eco.common.hook.economy.theosiseconomy.TheosisEconomyHook
import com.github.encryptsl.lite.eco.common.manager.importer.EconomyImportResults
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.bukkit.OfflinePlayer
import java.math.BigDecimal

class TheosisEconomyImporter : EconomyImporter {

    override val name = "TheosisEconomy"

    override suspend fun import(
        currencyForImport: String?,
        liteEcoCurrency: String,
        liteEco: LiteEco,
        offlinePlayers: Array<OfflinePlayer>
    ): EconomyImportResults = coroutineScope {
        var converted = 0
        var totalBalances = BigDecimal.ZERO

        try {
            val theosisEconomy = TheosisEconomyHook(liteEco)
            liteEco.logger.info("Starting $name data import for ${offlinePlayers.size} players...")

            offlinePlayers.asIterable().chunked(100).forEach { chunk ->
                val dataToImport = chunk.map { p ->
                    async(Dispatchers.IO) {
                        try {
                            val uuid = p.uniqueId
                            val name = p.name ?: "Unknown"

                            val balance = theosisEconomy.getBalance(uuid)

                            if (balance <= BigDecimal.ZERO) {
                                return@async null
                            }

                            Triple(uuid, name, balance)
                        } catch (e: Exception) {
                            liteEco.logger.warn("Failed to migrate player ${p.name}: ${e.message}")
                            null
                        }
                    }
                }.awaitAll().filterNotNull()

                if (dataToImport.isNotEmpty()) {
                    liteEco.api.batchInsert(dataToImport, liteEcoCurrency)

                    dataToImport.forEach { (_, _, balance) ->
                        totalBalances = totalBalances.add(balance)
                        converted++
                    }
                }

                liteEco.logger.info("Import in progress: $converted accounts migrated...")
            }
        } catch (e: Exception) {
            liteEco.logger.error("An error occurred during the $name import process: ${e.message}")
        }

        liteEco.logger.info("$name import completed. Total accounts migrated: $converted.")
        EconomyImportResults(converted, totalBalances)
    }

}