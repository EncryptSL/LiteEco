package com.github.encryptsl.lite.eco.utils

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.account.Wallet
import com.github.encryptsl.lite.eco.api.objects.ModernText
import java.io.File
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*

class Debugger(private val liteEco: LiteEco) {

    fun <T> debug(provider: Class<T>, message: String) {
        if (liteEco.baseConfig.plugin.vaultDebug) {
            liteEco.logger.info(ModernText.miniModernText("<gold> ${provider.name} $message"))
        }
    }

    fun dumpUnsavedAccounts(failedAccounts: Map<UUID, Wallet>, reason: String, isSqlite: Boolean = false) {
        if (failedAccounts.isEmpty()) return

        try {
            val errorsFolder = File(liteEco.dataFolder, "errors")
            if (!errorsFolder.exists()) {
                errorsFolder.mkdirs()
            }

            val timeStamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(Date())
            val dumpFile = File(errorsFolder, "restore_$timeStamp.sql")

            // 1. Group unsaved balances by currency, capturing UUID, username, and amount
            data class DumpRecord(val uuid: UUID, val username: String, val amount: BigDecimal)
            val groupedByCurrency = mutableMapOf<String, MutableList<DumpRecord>>()

            failedAccounts.forEach { (uuid, wallet) ->
                val username = wallet.username.ifBlank { "Unknown" }
                wallet.balances.forEach { (currency, amount) ->
                    groupedByCurrency
                        .computeIfAbsent(currency.lowercase()) { mutableListOf() }
                        .add(DumpRecord(uuid, username, amount))
                }
            }

            // Helper extension function to strip dashes from UUID string for binary literal representation
            fun UUID.toCleanHex(): String = this.toString().replace("-", "")

            // 2. Build dialect-aware bulk SQL query statements
            val sqlBuilder = StringBuilder().apply {
                appendLine("-- Emergency SQL Dump - LiteEco (${if (isSqlite) "SQLite" else "MySQL/MariaDB"})")
                appendLine("-- Generated: ${Date()}")
                appendLine("-- Reason: $reason")
                appendLine("-- Total unsaved accounts: ${failedAccounts.size}")
                appendLine()

                groupedByCurrency.forEach { (tableName, records) ->
                    if (records.isEmpty()) return@forEach

                    appendLine("-- Bulk insert for currency table: `lite_eco_$tableName` (${records.size} accounts)")
                    appendLine("INSERT INTO `lite_eco_$tableName` (`uuid`, `username`, `money`) VALUES")

                    val valueRows = records.joinToString(",\n") { record ->
                        val formattedUuid = if (isSqlite) {
                            "x'${record.uuid.toCleanHex()}'"
                        } else {
                            "0x${record.uuid.toCleanHex()}"
                        }
                        // Escape apostrophes in username to prevent SQL injection or syntax error
                        val safeUsername = record.username.replace("'", "''")

                        "  ($formattedUuid, '$safeUsername', ${record.amount.toPlainString()})"
                    }
                    appendLine(valueRows)

                    // Dialect-specific Upsert clause
                    if (isSqlite) {
                        appendLine("ON CONFLICT(`uuid`) DO UPDATE SET `money` = excluded.`money`, `username` = excluded.`username`;")
                    } else {
                        appendLine("ON DUPLICATE KEY UPDATE `money` = VALUES(`money`), `username` = VALUES(`username`);")
                    }
                    appendLine()
                }
            }

            // 3. Write SQL script to disk
            dumpFile.writeText(sqlBuilder.toString())

            // 4. Actionable error notice in logs for system administrators
            liteEco.logger.error("-----------------------------------------------------------------")
            liteEco.logger.error("ERROR: Failed to sync cache data with the database!")
            liteEco.logger.error("A total of ${failedAccounts.size} account(s) could not be saved.")
            liteEco.logger.error("Reason: $reason")
            liteEco.logger.error(" ")
            liteEco.logger.error("To prevent data loss, the unsaved cache has been dumped to:")
            liteEco.logger.error("Path -> ${dumpFile.path}")
            liteEco.logger.error(" ")
            liteEco.logger.error("MANUAL RESTORE INSTRUCTIONS:")
            liteEco.logger.error("Open the SQL file above in your database client (HeidiSQL, DBeaver, etc.)")
            liteEco.logger.error("and execute it to update the player balances in the database.")
            liteEco.logger.error("-----------------------------------------------------------------")

        } catch (e: Exception) {
            liteEco.logger.error("CRITICAL ERROR: Failed to write recovery SQL file to disk!", e)
        }
    }
}