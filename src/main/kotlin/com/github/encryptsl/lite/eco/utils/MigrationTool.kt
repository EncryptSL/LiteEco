package com.github.encryptsl.lite.eco.utils

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.common.database.models.legacy.LegacyDatabaseEcoModel
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.PrintWriter
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MigrationTool(private val liteEco: LiteEco) {

    enum class MigrationKey { CSV, SQL, LEGACY_TABLE }

    fun migrateToCSV(data: List<MigrationData>, fileName: String, currency: String = "dollars"): Boolean {
        val file = File("${liteEco.dataFolder}/migration/", "${fileName}_${currency}_${dateTime()}.csv")

        return try {
            file.parentFile.mkdirs()
            PrintWriter(FileWriter(file)).use { writer ->
                writer.println("id,username,uuid,money")
                data.forEach {
                    writer.println("${it.id},${it.username},${it.uuid},${it.money}")
                }
            }
            true
        }catch (e: IOException) {
            liteEco.logger.severe("Error while migrating to CSV file: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    fun migrateToSQL(data: List<MigrationData>, fileName: String, currency: String = "dollars"): Boolean {
        val file = File("${liteEco.dataFolder}/migration/", "${fileName}_${currency}_${dateTime()}.sql")

        return try {
            file.parentFile.mkdirs()
            PrintWriter(FileWriter(file)).use { writer ->
                writer.println("DROP TABLE IF EXISTS lite_eco_$currency;")
                writer.println("CREATE TABLE lite_eco_$currency (id INT(11), username VARCHAR(36), uuid BINARY(16), money DECIMAL(18,9));")
                val insertStatements = data.toList().joinToString {
                    "\n(${it.id}, '${it.username}', 0x${it.uuid.replace("-", "")}, ${it.money})"
                }
                writer.println("INSERT INTO lite_eco_$currency (id, username, uuid, money) VALUES $insertStatements;")
                writer.println("ALTER TABLE `lite_eco_$currency` ADD PRIMARY KEY(`id`);")
            }
            true
        } catch (e: IOException) {
            liteEco.logger.severe("Error while migrating to SQL file: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    fun migrateLegacyTable(currency: String): Boolean {
        val legacyDatabaseEcoModel = LegacyDatabaseEcoModel(liteEco)
        return legacyDatabaseEcoModel.convertOldBalance(currency)
    }

    private fun dateTime(): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm")
        return LocalDateTime.now().format(formatter)
    }

    data class MigrationData(val id: Int, val username: String, val uuid: String, val money: BigDecimal)
}