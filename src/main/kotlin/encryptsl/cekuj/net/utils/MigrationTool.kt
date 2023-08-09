package encryptsl.cekuj.net.utils

import encryptsl.cekuj.net.LiteEco
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.PrintWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MigrationTool(private val liteEco: LiteEco) {

    fun migrateToCSV(data: List<MigrationData>, fileName: String): Boolean {
        val file = File("${liteEco.dataFolder}/migration/", "${fileName}_${dateTime()}.csv")

        return try {
            file.parentFile.mkdirs()
            PrintWriter(FileWriter(file)).use { writer ->
                writer.println("id,uuid,money")
                data.forEach {
                    writer.println("${it.id},${it.uuid},${it.money}")
                }
            }
            true
        }catch (e: IOException) {
            liteEco.logger.severe("Error while migrating to CSV file: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    fun migrateToSQL(data: List<MigrationData>, fileName: String): Boolean {
        val file = File("${liteEco.dataFolder}/migration/", "${fileName}_${dateTime()}.sql")

        return try {
            file.parentFile.mkdirs()
            PrintWriter(FileWriter(file)).use { writer ->
                writer.println("DROP TABLE IF EXISTS lite_eco;")
                writer.println("CREATE TABLE lite_eco (id INT, uuid VARCHAR(36), money DOUBLE);")
                val insertStatements = data.joinToString {
                    "\n(${it.id}, '${it.uuid}', ${it.money})"
                }
                writer.println("INSERT INTO lite_eco (id, uuid, money) VALUES $insertStatements;")
            }
            true
        } catch (e: IOException) {
            liteEco.logger.severe("Error while migrating to SQL file: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    private fun dateTime(): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm")
        return LocalDateTime.now().format(formatter)
    }
}

data class MigrationData(val id: Int, val uuid: String, val money: Double)
