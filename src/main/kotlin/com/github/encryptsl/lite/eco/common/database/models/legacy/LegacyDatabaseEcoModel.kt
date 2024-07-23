package com.github.encryptsl.lite.eco.common.database.models.legacy

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.common.database.tables.legacy.LegacyAccountTable
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class LegacyDatabaseEcoModel(
    private val liteEco: LiteEco
) {

    private val playersBalances = mutableMapOf<UUID, MutableMap<String, Double>>()
    private val playerBalance = mutableMapOf<String, Double>()

    private fun getPlayerBalances(): MutableMap<UUID, MutableMap<String, Double>> {

        transaction {
            LegacyAccountTable.selectAll().forEach { resultRow: ResultRow ->
                playerBalance[resultRow[LegacyAccountTable.username]] = resultRow[LegacyAccountTable.money]
                playersBalances[UUID.fromString(resultRow[LegacyAccountTable.uuid])] = playerBalance
            }
        }

        return playersBalances
    }

    fun convertOldBalance(currency: String): Boolean {
        return try {
            getPlayerBalances().forEach { (key, data) ->
                data.entries.forEach { (t, u) ->
                    liteEco.databaseEcoModel.createPlayerAccount(t, key, currency, u.toBigDecimal())
                }
            }
            liteEco.loggerModel.info("Migration Finished !")
            playersBalances.clear()
            playerBalance.clear()
            true
        } catch (e : ExposedSQLException) {
            liteEco.loggerModel.error(e.message ?: e.localizedMessage)
            false
        }
    }

}