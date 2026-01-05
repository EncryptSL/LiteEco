package com.github.encryptsl.lite.eco.api.migrator

import com.github.encryptsl.lite.eco.api.migrator.entity.PlayerBalances
import com.github.encryptsl.lite.eco.api.migrator.interfaces.Export
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bukkit.plugin.Plugin
import java.io.File

class SQLFileExporter(
    private val plugin: Plugin,
    private val fileName: String,
    private val currency: String,
    private val dialect: Export.SQLDialect,
) : Export {

    override suspend fun export(balances: List<PlayerBalances.PlayerBalance>): Boolean = withContext(Dispatchers.IO) {
        val file = File("${plugin.dataFolder}/migration/", "${fileName}_${currency}_${timestamp}.sql")
        val tableName = "lite_eco_$currency"

        if (balances.isEmpty()) return@withContext false

        try {
            file.parentFile.mkdirs()
            file.bufferedWriter().use { writer ->
                if (dialect == Export.SQLDialect.MARIADB) {
                    writer.write("DROP TABLE IF EXISTS $tableName;\n")
                }
                writer.write("CREATE TABLE IF NOT EXISTS $tableName (id INT PRIMARY KEY, username VARCHAR(36), uuid BINARY(16), money DECIMAL(18,9));\n")

                balances.chunked(500).forEach { batch ->
                    val values = batch.joinToString(",\n") {
                        val formattedUUID = dialect.formatHex(it.uuid.toString())
                        val safeName = it.username?.replace("'", "''") ?: "Unknown"
                        "(${it.id}, '$safeName', $formattedUUID, ${it.money.toPlainString()})"
                    }
                    writer.write("INSERT INTO $tableName (id, username, uuid, money) VALUES $values;\n")
                }
            }
            true
        } catch (e: Exception) {
            plugin.logger.severe("SQL Export failed: ${e.message}")
            false
        }
    }
}