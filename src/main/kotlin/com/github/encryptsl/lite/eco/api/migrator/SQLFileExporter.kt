package com.github.encryptsl.lite.eco.api.migrator

import com.github.encryptsl.lite.eco.api.migrator.entity.PlayerBalances
import org.bukkit.plugin.Plugin
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.PrintWriter

class SQLFileExporter(private val plugin: Plugin, private val fileName: String, private val currency: String) : Export() {

    fun exportToSQLFile(balances: List<PlayerBalances.PlayerBalance>) : Boolean {
        val file = File("${plugin.dataFolder}/migration/", "${fileName}_${currency}_${timestamp}.sql")
        return try {
            file.parentFile.mkdirs()
            PrintWriter(FileWriter(file)).use { writer ->
                writer.println("DROP TABLE IF EXISTS lite_eco_$currency;")
                writer.println("CREATE TABLE lite_eco_$currency (id INT(11), username VARCHAR(36), uuid BINARY(16), money DECIMAL(18,9));")
                val insertStatements = balances.joinToString {
                    "\n(${it.id}, '${it.username}', 0x${it.uuid.toString().replace("-", "")}, ${it.money})"
                }
                writer.println("INSERT INTO lite_eco_$currency (id, username, uuid, money) VALUES $insertStatements;")
                writer.println("ALTER TABLE `lite_eco_$currency` ADD PRIMARY KEY(`id`);")
            }
            true
        } catch (e: IOException) {
            plugin.logger.severe("Error while migrating to SQL file: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    fun exportToSQLFileLite(balances: List<PlayerBalances.PlayerBalance>) : Boolean {
        val file = File("${plugin.dataFolder}/migration/", "${fileName}_${currency}_${timestamp}.sql")
        return try {
            file.parentFile.mkdirs()
            PrintWriter(FileWriter(file)).use { writer ->
                writer.println("CREATE TABLE lite_eco_$currency (id INT(11), username VARCHAR(36), uuid BINARY(16), money DECIMAL(18,9));")
                val insertStatements = balances.joinToString {
                    "\n(${it.id}, '${it.username}', X${it.uuid.toString().replace("-", "")}, ${it.money})"
                }
                writer.println("INSERT INTO lite_eco_$currency (id, username, uuid, money) VALUES $insertStatements;")
            }
            true
        } catch (e: IOException) {
            plugin.logger.severe("Error while migrating to SQL file: ${e.message}")
            e.printStackTrace()
            false
        }
    }
}