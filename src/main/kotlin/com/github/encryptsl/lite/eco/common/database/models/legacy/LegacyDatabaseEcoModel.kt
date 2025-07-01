package com.github.encryptsl.lite.eco.common.database.models.legacy

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.migrator.entity.PlayerBalances
import com.github.encryptsl.lite.eco.common.database.tables.Account
import com.github.encryptsl.lite.eco.common.database.tables.legacy.LegacyAccountTable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
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
                    resultRow[LegacyAccountTable.id].value.toInt(),
                    UUID.fromString(resultRow[LegacyAccountTable.uuid]),
                    resultRow[LegacyAccountTable.username],
                    resultRow[LegacyAccountTable.money].toBigDecimal()
                )
            }
        }

        return playersBalances
    }

    suspend fun exportToLiteEcoDollarsTable() : Boolean {
        return withContext(Dispatchers.IO) {
            transaction {
                try {
                    val table = Account(currency)
                    SchemaUtils.create(table)
                    for (eco in getPlayerBalances()) {
                        table.insert {
                            it[uuid] = eco.key
                            it[username] = eco.value.username ?: "null"
                            it[money] = eco.value.money
                        }
                    }
                    return@transaction true
                } catch (e : ExposedSQLException) {
                    liteEco.logger.severe(e.sqlState)
                    return@transaction false
                }
            }
        }
    }
}