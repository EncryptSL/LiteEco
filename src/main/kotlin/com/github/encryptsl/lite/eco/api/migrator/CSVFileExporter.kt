package com.github.encryptsl.lite.eco.api.migrator

import com.github.encryptsl.lite.eco.api.migrator.entity.PlayerBalances
import com.github.encryptsl.lite.eco.api.migrator.interfaces.Export
import com.github.encryptsl.lite.eco.common.extensions.io
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.bukkit.plugin.Plugin
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.text.SimpleDateFormat
import java.util.*

class CSVFileExporter(
    private val plugin: Plugin,
    private val fileName: String,
    private val currency: String = "dollars"
) : Export {

    override suspend fun export(balances: List<PlayerBalances.PlayerBalance>): Boolean = io {
        if (balances.isEmpty()) return@io false

        val timeStamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(Date())
        val migrationFolder = File(plugin.dataFolder, "migration")
        val file = File(migrationFolder, "${fileName}_${currency}_$timeStamp.csv")

        try {
            if (!migrationFolder.exists()) {
                migrationFolder.mkdirs()
            }

            // Prepare CSV format with header
            val csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader("id", "username", "uuid", "money")
                .setDelimiter(',')
                .get()

            Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8).use { writer ->
                CSVPrinter(writer, csvFormat).use { printer ->
                    balances.forEach { balance ->
                        printer.printRecord(
                            balance.id,
                            balance.username ?: "Unknown",
                            balance.uuid.toString(), // Standard UUID string representation (e.g. 550e8400-e29b-41d4-a716-446655440000)
                            balance.money.toPlainString()
                        )
                    }
                    printer.flush()
                }
            }
            true
        } catch (e: IOException) {
            plugin.logger.severe("CSV Export failed for currency '$currency': ${e.message}")
            false
        } catch (e: Exception) {
            plugin.logger.severe("Unexpected error during CSV Export for currency '$currency': ${e.message}")
            false
        }
    }
}