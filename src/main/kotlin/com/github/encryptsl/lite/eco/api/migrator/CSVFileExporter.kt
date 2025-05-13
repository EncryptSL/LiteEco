package com.github.encryptsl.lite.eco.api.migrator

import com.github.encryptsl.lite.eco.api.migrator.entity.PlayerBalances
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.bukkit.plugin.Plugin
import java.io.BufferedWriter
import java.io.File
import java.io.IOException
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Files

class CSVFileExporter(private val plugin: Plugin, fileName: String, currency: String = "dollars") : Export() {

    private val file: File = File("${plugin.dataFolder}/migration/${fileName}_${currency}_${timestamp}.csv")


    suspend fun exportToCSVFile(balances: List<PlayerBalances.PlayerBalance>) : Boolean  {

        return withContext(Dispatchers.IO) {
            try {
                val writer: BufferedWriter = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)
                file.parentFile.mkdirs()
                val format = CSVFormat.DEFAULT.builder().setHeader("id", "username", "uuid", "money").get()
                format.print(file, Charset.forName("UTF-8")).use { printer ->
                    balances.forEach { it ->
                        printer.printRecord(it.id, it.username, "0x${it.uuid.toString().replace("-", "")}", it.money)
                    }
                }
                val csvPrinter = CSVPrinter(writer, format)
                csvPrinter.flush()
                csvPrinter.close()
                true
            } catch (e: IOException) {
                plugin.logger.severe("Error while migrating to CSV file: ${e.message}")
                e.printStackTrace()
                false
            }
        }
    }

    @Deprecated("Replaced with better alternative exporting")
    suspend fun exportToCsvFile(balances: List<PlayerBalances.PlayerBalance>): Boolean {
        return exportToCSVFile(balances)
    }

}