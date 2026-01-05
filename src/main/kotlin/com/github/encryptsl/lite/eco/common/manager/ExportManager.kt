package com.github.encryptsl.lite.eco.common.manager

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.enums.ExportKeys
import com.github.encryptsl.lite.eco.api.migrator.CSVFileExporter
import com.github.encryptsl.lite.eco.api.migrator.LegacyTableExporter
import com.github.encryptsl.lite.eco.api.migrator.SQLFileExporter
import com.github.encryptsl.lite.eco.api.migrator.entity.PlayerBalances
import com.github.encryptsl.lite.eco.api.migrator.interfaces.Export
import com.github.encryptsl.lite.eco.api.objects.ModernText
import com.github.encryptsl.lite.eco.common.database.models.legacy.LegacyDatabaseEcoModel
import com.github.encryptsl.lite.eco.common.extensions.io
import kotlinx.coroutines.launch
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.command.CommandSender
import kotlin.system.measureTimeMillis

class ExportManager(
    private val liteEco: LiteEco
) {
    private val legacyDatabaseModel by lazy { LegacyDatabaseEcoModel(liteEco) }

    fun export(sender: CommandSender, exportKeys: ExportKeys, currency: String) {
        liteEco.pluginScope.launch {
            try {
                val timer = measureTimeMillis {

                    val output = if (exportKeys == ExportKeys.LEGACY_TO_NEW) {
                        val legacyModel = LegacyDatabaseEcoModel(liteEco)
                        io { legacyModel.getPlayerBalances().values.toList() }
                    } else {
                        io {
                            liteEco.api.getUUIDNameMap(currency.lowercase()).toList().mapIndexed { index, pair ->
                                PlayerBalances.PlayerBalance(index + 1, pair.first, pair.second, liteEco.api.getBalance(pair.first))
                            }
                        }
                    }

                    val exporter: Export = when(exportKeys) {
                        ExportKeys.CSV -> getCSVFileExporter("economy_migration", currency.lowercase())
                        ExportKeys.SQL -> getSQLFileExporter("economy_migration", currency.lowercase(), Export.SQLDialect.MARIADB)
                        ExportKeys.SQL_LITE_FILE ->
                            getSQLFileExporter("economy_migration_sql_lite", currency.lowercase(), Export.SQLDialect.SQLITE)
                        ExportKeys.LEGACY_TO_NEW ->getLegacyTableExporter("legacy_to_new", currency)
                    }

                    val result = exporter.export(output)
                    val messageKey = if (result) "messages.admin.export_success" else "messages.error.export_failed"

                    sender.sendMessage(liteEco.locale.translation(messageKey, TagResolver.resolver(
                        Placeholder.parsed("type", exportKeys.name),
                        Placeholder.parsed("currency", currency)
                    )))
                }

                liteEco.componentLogger.info(ModernText.miniModernText("Exporting of ${exportKeys.name} elapsed $timer ms"))

            } catch (e: Exception) {
                liteEco.componentLogger.error(ModernText.miniModernText("Export error: ${e.message}"))
                sender.sendMessage(ModernText.miniModernText("<red>Export failed! Check console."))
            }
        }
    }

    private fun getSQLFileExporter(fileName: String, currency: String, dialect: Export.SQLDialect) =
        SQLFileExporter(liteEco, fileName, currency, dialect)

    private fun getCSVFileExporter(fileName: String, currency: String) =
        CSVFileExporter(liteEco, fileName, currency)

    private fun getLegacyTableExporter(fileName: String, currency: String) =
        LegacyTableExporter(liteEco, fileName, currency)
}