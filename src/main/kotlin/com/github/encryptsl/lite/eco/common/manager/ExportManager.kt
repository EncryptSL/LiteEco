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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.command.CommandSender
import kotlin.system.measureTimeMillis

class ExportManager(
    private val liteEco: LiteEco
) {

    fun export(sender: CommandSender, exportKeys: ExportKeys, currency: String) {
        val cleanCurrency = currency.lowercase()

        liteEco.pluginScope.launch(Dispatchers.IO) {
            try {
                val timer = measureTimeMillis {

                    // 1. Fetch data efficiently
                    val balances: List<PlayerBalances.PlayerBalance> = if (exportKeys == ExportKeys.LEGACY_TO_NEW) {
                        val legacyModel = LegacyDatabaseEcoModel(liteEco)
                        legacyModel.getPlayerBalances().values.toList()
                    } else {
                        // Pre-fetch all balances in bulk directly from database/API to avoid N+1 queries
                        liteEco.api.getBalancesForCurrency(cleanCurrency)
                    }

                    // 2. Select exporter implementation
                    val exporter: Export = when (exportKeys) {
                        ExportKeys.CSV -> getCSVFileExporter("economy_migration", cleanCurrency)
                        ExportKeys.SQL -> getSQLFileExporter("economy_migration", cleanCurrency, Export.SQLDialect.MARIADB)
                        ExportKeys.SQL_LITE_FILE -> getSQLFileExporter("economy_migration_sql_lite", cleanCurrency, Export.SQLDialect.SQLITE)
                        ExportKeys.LEGACY_TO_NEW -> getLegacyTableExporter("legacy_to_new", cleanCurrency)
                    }

                    // 3. Execute export
                    val result = exporter.export(balances)
                    val messageKey = if (result) "messages.admin.export_success" else "messages.error.export_failed"

                    // 4. Send response to command sender
                    sender.sendMessage(
                        liteEco.locale.translation(
                            messageKey,
                            TagResolver.resolver(
                                Placeholder.parsed("type", exportKeys.name),
                                Placeholder.parsed("currency", cleanCurrency)
                            )
                        )
                    )
                }

                liteEco.componentLogger.info(ModernText.miniModernText("Export of ${exportKeys.name} elapsed ${timer}ms"))

            } catch (e: Exception) {
                liteEco.componentLogger.error("Export error for ${exportKeys.name}: ${e.message}", e)
                sender.sendMessage(ModernText.miniModernText("<red>Export failed! Check console for details."))
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