package com.github.encryptsl.lite.eco.api.migrator

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.migrator.entity.PlayerBalances
import com.github.encryptsl.lite.eco.api.migrator.interfaces.Export
import com.github.encryptsl.lite.eco.common.extensions.io
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class LegacyTableExporter(
    private val liteEco: LiteEco,
    private val fileName: String,
    private val currency: String = "dollars",
    private val dialect: Export.SQLDialect = Export.SQLDialect.MARIADB
) : Export {

    override suspend fun export(balances: List<PlayerBalances.PlayerBalance>): Boolean = io {
        if (balances.isEmpty()) return@io false

        val timeStamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(Date())
        val migrationFolder = File(liteEco.dataFolder, "migration")
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
                writer.appendLine("-- Legacy Migration Export - LiteEco")
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

                    batch.forEachIndexed { index, player ->
                        val hexUuid = dialect.formatHex(player.uuid.toString())
                        val safeName = player.username?.replace("'", "''") ?: "Unknown"
                        val isLast = index == batch.size - 1

                        val row = "  (${player.id}, '$safeName', $hexUuid, ${player.money.toPlainString()})${if (isLast) "" else ","}"
                        writer.appendLine(row)
                    }

                    // Dialect-specific Upsert clause to safely handle duplicate primary/unique keys
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
            liteEco.logger.error("Legacy Table Export failed for currency '$currency': ${e.message}")
            false
        }
    }
}