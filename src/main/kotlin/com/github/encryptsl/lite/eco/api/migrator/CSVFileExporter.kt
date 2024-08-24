package com.github.encryptsl.lite.eco.api.migrator

import com.github.encryptsl.lite.eco.api.migrator.entity.PlayerBalances
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bukkit.plugin.Plugin
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException

class CSVFileExporter(private val plugin: Plugin, private val fileName: String, private val currency: String = "dollars") : Export() {

    suspend fun exportToCsvFile(balances: List<PlayerBalances.PlayerBalance>): Boolean {
        val file = File("${plugin.dataFolder}/migration/", "${fileName}_${currency}_${timestamp}.csv")
        return withContext(Dispatchers.IO) {
            val insertString = balances.joinToString { balance ->
                "\n${balance.id},${balance.username}, 0x${balance.uuid.toString().replace("-", "")},${balance.money}"
            }

            try {
                file.parentFile.mkdirs()
                BufferedWriter(FileWriter(file)).use { writer ->
                    writer.write("id,username,uuid,money")
                    writer.write(insertString)
                    writer.flush()
                    writer.close()
                }
                true
            } catch (e : IOException) {
                plugin.logger.severe("Error while migrating to CSV file: ${e.message}")
                e.printStackTrace()
                false
            }
        }
    }

}