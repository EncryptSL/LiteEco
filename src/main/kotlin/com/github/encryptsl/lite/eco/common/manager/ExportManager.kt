package com.github.encryptsl.lite.eco.common.manager

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.enums.ExportKeys
import com.github.encryptsl.lite.eco.api.migrator.CSVFileExporter
import com.github.encryptsl.lite.eco.api.migrator.SQLFileExporter
import com.github.encryptsl.lite.eco.api.migrator.entity.PlayerBalances
import com.github.encryptsl.lite.eco.api.objects.ModernText
import com.github.encryptsl.lite.eco.common.database.models.legacy.LegacyDatabaseEcoModel
import com.github.encryptsl.lite.eco.common.extensions.positionIndexed
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.command.CommandSender
import kotlin.system.measureTimeMillis

class ExportManager(
    private val liteEco: LiteEco
) {
    fun export(sender: CommandSender, exportKeys: ExportKeys, currency: String) {
        try {
            runBlocking {
                val timer = measureTimeMillis {
                    // This is better method to not crash server because data is exported from database and from game cache if is player connected and cached.
                    val output = liteEco.api.getUUIDNameMap(currency.lowercase()).toList().positionIndexed { index, pair ->
                        PlayerBalances.PlayerBalance(index, pair.first, pair.second, liteEco.api.getBalance(pair.first))
                    }

                    val result = when(exportKeys) {
                        ExportKeys.CSV -> getCSVFileExporter("economy_migration", currency.lowercase()).exportToCSVFile(output)
                        ExportKeys.SQL -> getSQLFileExporter("economy_migration", currency.lowercase()).exportToSQLFile(output)
                        ExportKeys.LEGACY_TABLE -> getLegacyTableExporter(currency.lowercase()).exportToLiteEcoDollarsTable()
                        ExportKeys.SQL_LITE_FILE -> getSQLFileExporter("economy_migration_sql_lite", currency.lowercase()).exportToSQLFileLite(output)
                    }

                    val messageKey = if (result) {
                        "messages.admin.export_success"
                    } else {
                        "messages.error.export_failed"
                    }

                    sender.sendMessage(liteEco.locale.translation(messageKey, TagResolver.resolver(
                        Placeholder.parsed("type", exportKeys.name), Placeholder.parsed("currency", currency)
                    )))
                }
                liteEco.componentLogger.info(ModernText.miniModernText("Exporting of ${exportKeys.name} elapsed $timer ms"))
            }
        } catch (e : Exception) {
            liteEco.componentLogger.error(ModernText.miniModernText(e.message ?: e.localizedMessage))
        }
    }

    fun getSQLFileExporter(fileName: String, currency: String) = SQLFileExporter(liteEco, fileName, currency)

    fun getCSVFileExporter(fileName: String, currency: String) = CSVFileExporter(liteEco, fileName, currency)

    fun getLegacyTableExporter(currency: String) = LegacyDatabaseEcoModel(liteEco, currency)

}