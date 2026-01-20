package com.github.encryptsl.lite.eco.common.manager.importer

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.interfaces.EconomyImporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.bukkit.Bukkit
import java.math.BigDecimal
import java.util.*

abstract class BaseEconomyImporter : EconomyImporter {

    override val isMultiCurrency: Boolean = false

    val offlinePlayers = Bukkit.getOfflinePlayers()

    suspend fun <T> executeImport(
        importerName: String,
        liteEco: LiteEco,
        intoCurrency: String,
        dataSource: Iterable<T>,
        processor: suspend (T) -> Triple<UUID, String, BigDecimal>?
    ): EconomyImportResults = coroutineScope {
        var converted = 0
        var totalBalances = BigDecimal.ZERO

        liteEco.logger.info("Starting $importerName data import...")

        dataSource.chunked(100).forEach { chunk ->
            val dataToImport = chunk.map { item ->
                async(Dispatchers.IO) {
                    try {
                        processor(item)
                    } catch (e: Exception) {
                        liteEco.logger.warn("Failed to migrate a record in $importerName: ${e.message}")
                        null
                    }
                }
            }.awaitAll().filterNotNull()

            if (dataToImport.isNotEmpty()) {
                liteEco.api.batchInsert(dataToImport, intoCurrency)
                dataToImport.forEach { (_, _, balance) ->
                    totalBalances = totalBalances.add(balance)
                    converted++
                }
            }

            if (converted > 0 && converted % 500 == 0) {
                liteEco.logger.info("$importerName progress: $converted accounts migrated...")
            }
        }

        liteEco.logger.info("$importerName import completed. Total: $converted accounts.")
        EconomyImportResults(converted, totalBalances)
    }
}