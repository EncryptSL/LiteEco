package com.github.encryptsl.lite.eco.api.migrator

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.migrator.entity.PlayerBalances
import com.github.encryptsl.lite.eco.api.migrator.interfaces.Export
import com.github.encryptsl.lite.eco.common.extensions.io
import java.io.File

class LegacyTableExporter(
    private val liteEco: LiteEco,
    fileName: String,
    private val currency: String = "dollars",
    private val dialect: Export.SQLDialect = Export.SQLDialect.MARIADB
) : Export {

    private val file = File("${liteEco.dataFolder}/migration/${fileName}_${currency}_${timestamp}.sql")

    override suspend fun export(balances: List<PlayerBalances.PlayerBalance>): Boolean = io {
        try {
            file.parentFile.mkdirs()
            val tableName = "lite_eco_$currency"

            file.bufferedWriter().use { writer ->
                writer.write("-- Legacy Migration Export\n")
                if (dialect == Export.SQLDialect.MARIADB) {
                    writer.write("DROP TABLE IF EXISTS $tableName;\n")
                }
                writer.write("CREATE TABLE IF NOT EXISTS $tableName (id INT PRIMARY KEY, username VARCHAR(36), uuid BINARY(16), money DECIMAL(18,9));\n")

                balances.chunked(500).forEach { batch ->
                    val values = batch.joinToString(",\n") { player ->
                        val hexUuid = dialect.formatHex(player.uuid.toString())
                        val safeName = player.username?.replace("'", "''") ?: "Unknown"
                        "(${player.id}, '$safeName', $hexUuid, ${player.money.toPlainString()})"
                    }
                    writer.write("INSERT INTO $tableName (id, username, uuid, money) VALUES $values;\n")
                }
            }
            true
        } catch (e: Exception) {
            liteEco.logger.error("Legacy Table Export failed: ${e.message}")
            false
        }
    }
}