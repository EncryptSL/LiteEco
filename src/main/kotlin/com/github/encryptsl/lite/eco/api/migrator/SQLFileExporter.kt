package com.github.encryptsl.lite.eco.api.migrator

import com.github.encryptsl.lite.eco.api.migrator.entity.PlayerBalances
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bukkit.plugin.Plugin
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException

class SQLFileExporter(private val plugin: Plugin, private val fileName: String, private val currency: String) : Export() {

    suspend fun exportToSQLFile(balances: List<PlayerBalances.PlayerBalance>) : Boolean {
        val file = File("${plugin.dataFolder}/migration/", "${fileName}_${currency}_${timestamp}.sql")

        return withContext(Dispatchers.IO) {
            try {
                file.parentFile.mkdirs()

                val insertStatement = balances.joinToString {
                    "\n(${it.id}, '${it.username}', 0x${it.uuid.toString().replace("-", "")}, ${it.money})"
                }

                BufferedWriter(FileWriter(file)).use { writer ->
                    writer.write("DROP TABLE IF EXISTS lite_eco_$currency;\n")
                    writer.write("CREATE TABLE lite_eco_$currency (id INT(11), username VARCHAR(36), uuid BINARY(16), money DECIMAL(18,9));")
                    writer.write("\nINSERT INTO lite_eco_$currency (id, username, uuid, money) VALUES $insertStatement;")
                    writer.write("\nALTER TABLE `lite_eco_$currency` ADD PRIMARY KEY(`id`);")
                    writer.flush()
                    writer.close()
                }
                true
            } catch (e : IOException) {
                plugin.logger.severe("Error while migrating to SQL file: ${e.message}")
                e.printStackTrace()
                false
            }
        }

    }

    suspend fun exportToSQLFileLite(balances: List<PlayerBalances.PlayerBalance>) : Boolean {
        val file = File("${plugin.dataFolder}/migration/", "${fileName}_${currency}_${timestamp}.sql")
        return withContext(Dispatchers.IO) {
            try {
                file.parentFile.mkdirs()
                BufferedWriter(FileWriter(file)).use { writer ->
                    writer.write("CREATE TABLE lite_eco_$currency (id INT(11), username VARCHAR(36), uuid BINARY(16), money DECIMAL(18,9));")
                    val insertStatements = balances.joinToString {
                        "\n(${it.id}, '${it.username}', X'${it.uuid.toString().replace("-", "")}', ${it.money})"
                    }
                    writer.write("INSERT INTO lite_eco_$currency (id, username, uuid, money) VALUES $insertStatements;")
                    writer.flush()
                    writer.close()
                }
                true
            } catch (e : IOException) {
                plugin.logger.severe("Error while migrating to SQL file: ${e.message}")
                e.printStackTrace()
                false
            }
        }
    }
}