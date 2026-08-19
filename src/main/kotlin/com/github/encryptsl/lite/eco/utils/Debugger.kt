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

    fun dumpUnsavedAccounts(failedAccounts: Map<UUID, Wallet>, reason: String) {
        if (failedAccounts.isEmpty()) return

        try {
            val errorsFolder = File(liteEco.dataFolder, "errors")
            if (!errorsFolder.exists()) {
                errorsFolder.mkdirs()
            }

            val timeStamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(Date())
            val dumpFile = File(errorsFolder, "restore_$timeStamp.sql")

            // 1. Group unsaved balances by currency (database tables)
            val groupedByCurrency = mutableMapOf<String, MutableMap<UUID, BigDecimal>>()

            failedAccounts.forEach { (uuid, wallet) ->
                wallet.balances.forEach { (currency, amount) ->
                    groupedByCurrency
                        .computeIfAbsent(currency.lowercase()) { mutableMapOf() }[uuid] = amount
                }
            }

            // 2. Build bulk SQL query statements
            val sqlBuilder = StringBuilder().apply {
                appendLine("-- Emergency SQL Dump - LiteEco")
                appendLine("-- Generated: ${Date()}")
                appendLine("-- Reason: $reason")
                appendLine("-- Total unsaved accounts: ${failedAccounts.size}")
                appendLine()

                groupedByCurrency.forEach { (tableName, records) ->
                    if (records.isEmpty()) return@forEach

                    appendLine("-- Bulk insert for currency table: `lite_eco_$tableName` (${records.size} accounts)")
                    appendLine("INSERT INTO `lite_eco_$tableName` (`uuid`, `money`) VALUES")

                    val valueRows = records.entries.joinToString(",\n") { (uuid, amount) ->
                        "  ('$uuid', ${amount.toPlainString()})"
                    }
                    appendLine(valueRows)
                    appendLine("ON DUPLICATE KEY UPDATE `money` = VALUES(`money`);")
                    appendLine()
                }
            }

            // 3. Save to disk
            dumpFile.writeText(sqlBuilder.toString())

            // 4. Clear and actionable error log for server admins
            liteEco.logger.error("-----------------------------------------------------------------")
            liteEco.logger.error("ERROR: Failed to sync cache data with the database!")
            liteEco.logger.error("A total of ${failedAccounts.size} account(s) could not be saved.")
            liteEco.logger.error("Reason: $reason")
            liteEco.logger.error(" ")
            liteEco.logger.error("To prevent data loss, the unsaved cache has been dumped to:")
            liteEco.logger.error("Path -> ${dumpFile.path}")
            liteEco.logger.error("")
            liteEco.logger.error("MANUAL RESTORE INSTRUCTIONS:")
            liteEco.logger.error("Open the SQL file above in your database tool (HeidiSQL, phpMyAdmin, DBeaver)")
            liteEco.logger.error("and execute it to manually update the player balances in the database.")
            liteEco.logger.error("-----------------------------------------------------------------")

        } catch (e: Exception) {
            liteEco.logger.error("CRITICAL ERROR: Failed to write recovery SQL file to disk!", e)
        }
    }
}