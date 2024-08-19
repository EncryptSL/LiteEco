package com.github.encryptsl.lite.eco.utils

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.migrator.CSVFileExporter
import com.github.encryptsl.lite.eco.api.migrator.SQLFileExporter
import com.github.encryptsl.lite.eco.common.database.models.legacy.LegacyDatabaseEcoModel

class MigrationTool(private val liteEco: LiteEco) {

    enum class MigrationKey { CSV, SQL, LEGACY_TABLE, SQL_LITE_FILE }

    fun getSQLFileExporter(fileName: String, currency: String) = SQLFileExporter(liteEco, fileName, currency)

    fun getCSVFileExporter(fileName: String, currency: String) = CSVFileExporter(liteEco, fileName, currency)

    fun getLegacyTableExporter(currency: String) = LegacyDatabaseEcoModel(liteEco, currency)
}