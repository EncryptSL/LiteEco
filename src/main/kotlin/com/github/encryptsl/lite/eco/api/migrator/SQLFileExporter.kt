package com.github.encryptsl.lite.eco.api.migrator

import com.github.encryptsl.lite.eco.api.migrator.entity.PlayerBalances
import com.github.encryptsl.lite.eco.api.migrator.interfaces.Export
import com.github.encryptsl.lite.eco.common.extensions.io
import org.bukkit.plugin.Plugin
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class SQLFileExporter(
    private val plugin: Plugin,
    private val fileName: String,
    private val currency: String,
    private val dialect: Export.SQLDialect,
) : Export {

    override suspend fun export(balances: List<PlayerBalances.PlayerBalance>): Boolean = io {
        if (balances.isEmpty()) return@io false

        val timeStamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(Date())
        val migrationFolder = File(plugin.dataFolder, "migration")
        val file = File(migrationFolder, "${fileName}_${currency}_$timeStamp.sql")

        // Clean table name with escaped backticks
        val rawTableName = "lite_eco_${currency.lowercase()}"
        val tableName = "`$rawTableName`"

        try {
            if (!migrationFolder.exists()) {
                migrationFolder.mkdirs()
            }

            file.bufferedWriter().use { writer ->
                // SQL Header metadata
                writer.appendLine("-- LiteEco Migration Export")
                writer.appendLine("-- Generated: ${Date()}")
                writer.appendLine("-- Dialect: ${dialect.name}")
                writer.appendLine("-- Total Records: ${balances.size}")
                writer.appendLine()

                // Dialect-specific DDL Table Creation
                writer.appendLine("CREATE TABLE IF NOT EXISTS $tableName (")
                writer.appendLine("    `id` INT AUTO_INCREMENT PRIMARY KEY,")
                writer.appendLine("    `username` VARCHAR(36),")
                writer.appendLine("    `uuid` BINARY(16) NOT NULL UNIQUE,")
                writer.appendLine("    `money` DECIMAL(18,9) NOT NULL")
                writer.appendLine(");")
                writer.appendLine()

                // Batch Inserts (500 records per statement)
                balances.chunked(500).forEach { batch ->
                    writer.appendLine("INSERT INTO $tableName (`id`, `username`, `uuid`, `money`) VALUES")

                    batch.forEachIndexed { index, record ->
                        val formattedUUID = dialect.formatHex(record.uuid.toString())
                        val safeName = record.username?.replace("'", "''") ?: "Unknown"
                        val isLast = index == batch.size - 1

                        val row = "  (${record.id}, '$safeName', $formattedUUID, ${record.money.toPlainString()})${if (isLast) "" else ","}"
                        writer.appendLine(row)
                    }

                    // Dialect-specific Upsert logic to handle existing records during import
                    if (dialect == Export.SQLDialect.SQLITE) {
                        writer.appendLine("ON CONFLICT(`uuid`) DO UPDATE SET `money` = excluded.`money`, `username` = excluded.`username`;")
                    } else {
                        writer.appendLine("ON DUPLICATE KEY UPDATE `money` = VALUES(`money`), `username` = VALUES(`username`);")
                    }
                    writer.appendLine()
                }
            }
            true
        } catch (e: Exception) {
            plugin.logger.severe("SQL Export failed for currency '$currency': ${e.message}")
            false
        }
    }
}