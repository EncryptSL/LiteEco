package com.github.encryptsl.lite.eco.common.manager.importer.economies

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.interfaces.EconomyImporter
import com.github.encryptsl.lite.eco.common.manager.importer.EconomyImportResults
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.bukkit.OfflinePlayer
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.math.BigDecimal

class EssentialsXImporter : EconomyImporter {

    override val name: String = "EssentialsX"

    override suspend fun import(
        currency: String,
        liteEco: LiteEco,
        offlinePlayers: Array<OfflinePlayer>
    ): EconomyImportResults = coroutineScope {
        var converted = 0
        var totalBalances = BigDecimal.ZERO
        val userDataFolder = File("plugins/Essentials/userdata/")

        liteEco.logger.info("Starting EssentialsX import. Scanning user data files...")

        if (!userDataFolder.exists() || !userDataFolder.isDirectory) {
            liteEco.logger.error("EssentialsX userdata folder not found! Path: ${userDataFolder.absolutePath}")
            return@coroutineScope EconomyImportResults(0, BigDecimal.ZERO)
        }

        offlinePlayers.asIterable().chunked(100).forEach { chunk ->
            val dataToImport = chunk.map { p ->
                async(Dispatchers.IO) {
                    try {
                        val playerFile = File(userDataFolder, "${p.uniqueId}.yml")

                        if (playerFile.exists()) {
                            val config = YamlConfiguration.loadConfiguration(playerFile)
                            val balanceStr = config.getString("money")
                            val balance = balanceStr?.toBigDecimalOrNull() ?: BigDecimal.ZERO

                            if (balance <= BigDecimal.ZERO) {
                                return@async null
                            }

                            val name = p.name ?: "Unknown"
                            Triple(p.uniqueId, name, balance)
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        liteEco.logger.error("Could not import EssentialsX data for ${p.name}: ${e.message}")
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

            if (converted % 500 == 0 && converted > 0) {
                liteEco.logger.info("Import progress: $converted files processed...")
            }
        }
        liteEco.logger.info("EssentialsX import finished. Successfully migrated $converted accounts.")
        EconomyImportResults(converted, totalBalances)
    }

}