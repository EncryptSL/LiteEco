package com.github.encryptsl.lite.eco.common.manager.importer.economies

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.interfaces.EconomyImporter
import com.github.encryptsl.lite.eco.common.hook.economy.simpleeconomy.SimpleEconomyHook
import com.github.encryptsl.lite.eco.common.manager.importer.EconomyImportResults
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import java.math.BigDecimal
import java.util.*

class SimpleEconomyImporter : EconomyImporter {

    override val name: String = "SimpleEconomy"

    override suspend fun import(
        currency: String,
        liteEco: LiteEco,
        offlinePlayers: Array<OfflinePlayer>
    ): EconomyImportResults = coroutineScope {
        var converted = 0
        var totalBalances = BigDecimal.ZERO

        try {
            val simpleEconomy = SimpleEconomyHook(liteEco)
            liteEco.logger.info("Starting $name data import for all existing accounts...")

            val accounts = simpleEconomy.getAccounts()

            accounts.asIterable().chunked(100).forEach { chunk ->
                val dataToImport = chunk.map { entry ->
                    async(Dispatchers.IO) {
                        try {
                            val uuid = UUID.fromString(entry.key)
                            val player = Bukkit.getOfflinePlayer(uuid)
                            val name = player.name ?: "Unknown"
                            val balance = entry.value.toBigDecimal()

                            if (balance <= BigDecimal.ZERO) {
                                return@async null
                            }

                            Triple(uuid, name, balance)
                        } catch (e: Exception) {
                            liteEco.logger.warn("Failed to migrate account ${entry.key}: ${e.message}")
                            null
                        }
                    }
                }.awaitAll().filterNotNull()

                if (dataToImport.isNotEmpty()) {
                    liteEco.api.batchInsert(dataToImport, currency)

                    dataToImport.forEach { (_, _, balance) ->
                        totalBalances = totalBalances.add(balance)
                        converted++
                    }
                }

                liteEco.logger.info("Import in progress: $converted accounts migrated...")
            }
        } catch (e: Exception) {
            liteEco.logger.error("Critical error during the $name import process: ${e.message}")
        }

        liteEco.logger.info("$name import completed. Total accounts migrated: $converted.")
        EconomyImportResults(converted, totalBalances)
    }

}