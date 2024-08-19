package com.github.encryptsl.lite.eco.api.migrator

import com.github.encryptsl.lite.eco.api.migrator.entity.PlayerBalances
import org.bukkit.plugin.Plugin
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.PrintWriter

class CSVFileExporter(private val plugin: Plugin, private val fileName: String, private val currency: String = "dollars") : Export() {

    fun exportToCsvFile(balances: List<PlayerBalances.PlayerBalance>): Boolean {
        val file = File("${plugin.dataFolder}/migration/", "${fileName}_${currency}_${timestamp}.csv")

        return try {
            file.parentFile.mkdirs()
            PrintWriter(FileWriter(file)).use { writer ->
                writer.println("id,username,uuid,money")
                balances.forEach {
                    writer.println("${it.id},${it.username}, 0x${it.uuid.toString().replace("-", "")},${it.money}")
                }
            }
            true
        }catch (e: IOException) {
            plugin.logger.severe("Error while migrating to CSV file: ${e.message}")
            e.printStackTrace()
            false
        }
    }

}