package encryptsl.cekuj.net.utils

import encryptsl.cekuj.net.LiteEco
import java.io.BufferedWriter
import java.io.File
import java.io.FileNotFoundException
import java.io.FileWriter
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MigrationTool(private val liteEco: LiteEco) {

    fun migrateToCSV(data: List<MigrationData>, fileName: String) {
        val file =  File("${liteEco.dataFolder}/migration/", "${fileName}_${dateTime()}.csv")

        BufferedWriter(FileWriter(file.path)).use {writer ->
            try {
                writer.write(""""id", "uuid", "money"""")
                writer.newLine()
                data.forEach {
                    writer.write("${it.id}, ${it.uuid}, \"${it.money}\"")
                    writer.newLine()
                }
            } catch (e: FileNotFoundException) {
                file.mkdirs()
                file.createNewFile()
            } catch (e : IOException) {
                liteEco.getLogger().severe("Something wrong while migration to csv file !")
            } finally {
                writer.close()
            }
        }
    }

    fun migrateToSQL(data: List<MigrationData>, fileName: String) {
        val file =  File("${liteEco.dataFolder}/migration/", "${fileName}_${dateTime()}.sql")
        BufferedWriter(FileWriter(file.path)).use {writer ->
            try {
                writer.write("DROP TABLE IF EXIST lite_eco;")
                writer.newLine()
                writer.write("INSERT INTO lite_eco (id, uuid, money) VALUES " + data.joinToString {"\n(${it.id} ${it.uuid} ${it.money})" } + ";")
            } catch (e: FileNotFoundException) {
                file.mkdirs()
                file.createNewFile()
            } catch (e : IOException) {
                liteEco.getLogger().severe("Something wrong while migration to sql file !")
            } finally {
                writer.close()
            }
        }
    }

    private fun dateTime(): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm")
        return LocalDateTime.now().format(formatter)
    }
}

data class MigrationData(val id: Int, val uuid: String, val money: Double)