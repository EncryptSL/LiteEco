package com.github.encryptsl.lite.eco.api.migrator

import com.github.encryptsl.lite.eco.api.migrator.entity.PlayerBalances
import com.github.encryptsl.lite.eco.api.migrator.interfaces.Export
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.bukkit.plugin.Plugin
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files

class CSVFileExporter(private val plugin: Plugin, fileName: String, currency: String = "dollars") : Export {

    private val file = File("${plugin.dataFolder}/migration/${fileName}_${currency}_${timestamp}.csv")

    override suspend fun export(balances: List<PlayerBalances.PlayerBalance>): Boolean = withContext(Dispatchers.IO) {

        if (balances.isEmpty()) return@withContext false

        try {
            file.parentFile.mkdirs()
            Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8).use { writer ->
                val format = CSVFormat.DEFAULT.builder()
                    .setHeader("id", "username", "uuid", "money")
                    .setDelimiter(',')
                    .get()

                CSVPrinter(writer, format).use { printer ->
                    balances.forEach {
                        printer.printRecord(
                            it.id,
                            it.username ?: "Unknown",
                            "0x${it.uuid.toString().replace("-", "")}",
                            it.money.toPlainString()
                        )
                    }
                    printer.flush()
                }
            }
            true
        } catch (e: IOException) {
            plugin.logger.severe("CSV Export failed: ${e.message}")
            false
        }
    }
}