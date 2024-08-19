package com.github.encryptsl.lite.eco.common.database.models.legacy

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.migrator.entity.PlayerBalances
import com.github.encryptsl.lite.eco.common.database.tables.Account
import com.github.encryptsl.lite.eco.common.database.tables.legacy.LegacyAccountTable
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class LegacyDatabaseEcoModel(
    private val liteEco: LiteEco,
    private val currency: String
) {

    private val playersBalances = mutableMapOf<UUID, PlayerBalances.PlayerBalance>()

    private fun getPlayerBalances(): MutableMap<UUID, PlayerBalances.PlayerBalance> {

        transaction {
            LegacyAccountTable.selectAll().forEach { resultRow: ResultRow ->
                playersBalances[UUID.fromString(resultRow[LegacyAccountTable.uuid])] = PlayerBalances.PlayerBalance(
                    resultRow[LegacyAccountTable.id],
                    UUID.fromString(resultRow[LegacyAccountTable.uuid]),
                    resultRow[LegacyAccountTable.username],
                    resultRow[LegacyAccountTable.money].toBigDecimal()
                )
            }
        }

        return playersBalances
    }

    fun exportToLiteEcoDollarsTable() : Boolean {
        return try {
            transaction {
                val table = Account(currency)
                SchemaUtils.create(table)
                getPlayerBalances().forEach { (t, u) ->
                    table.insert {
                        it[uuid] = t
                        it[username] = u.username ?: "null"
                        it[money] = u.money
                    }
                }
            }
            true
        } catch (e : ExposedSQLException) {
            liteEco.loggerModel.error(e.message ?: e.localizedMessage)
            false
        }
    }
}