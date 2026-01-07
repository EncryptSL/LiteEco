package com.github.encryptsl.lite.eco.common.manager.importer.economies

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.interfaces.EconomyImporter
import com.github.encryptsl.lite.eco.common.hook.economy.craftconomy3.CraftConomyHook
import com.github.encryptsl.lite.eco.common.manager.importer.EconomyImportResults
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.bukkit.OfflinePlayer
import java.math.BigDecimal

class CraftConomy3Importer : EconomyImporter {

    override val name = "CraftConomy3"

    override suspend fun import(
        currency: String,
        liteEco: LiteEco,
        offlinePlayers: Array<OfflinePlayer>
    ): EconomyImportResults = coroutineScope {
        var converted = 0
        var totalBalances = BigDecimal.ZERO

        try {
            val craftEconomy = CraftConomyHook(liteEco)

            liteEco.logger.info("Starting $name data import for ${offlinePlayers.size} players...")
            offlinePlayers.asIterable().chunked(100).forEach { chunk ->
                val results = chunk.map { p ->
                    async(Dispatchers.IO) {
                        try {
                            val uuid = p.uniqueId
                            val name = p.name ?: "Unknown"
                            val balance = craftEconomy.getBalance(name, currency).toBigDecimal()

                            if (balance.compareTo(BigDecimal.ZERO) <= 0) {
                                return@async null
                            }

                            val success = liteEco.api.createOrUpdateAccount(uuid, name, currency, balance)
                            if (success) balance else null
                        } catch (e: Exception) {
                            liteEco.logger.warning("Failed to migrate player ${p.name}: ${e.message}")
                            null
                        }
                    }
                }.awaitAll()

                results.filterNotNull().forEach { balance ->
                    totalBalances = totalBalances.add(balance)
                    converted++
                }
                liteEco.logger.info("Import progress: $converted accounts migrated...")
            }
        } catch (e: Exception) {
            liteEco.logger.severe("An error occurred during the $name import process: ${e.message}")
        }

        liteEco.logger.info("$name import completed. Total accounts migrated: $converted.")
        EconomyImportResults(converted, totalBalances)
    }
}