package com.github.encryptsl.lite.eco.common.database.models.legacy

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.migrator.entity.PlayerBalances
import com.github.encryptsl.lite.eco.common.database.tables.Account
import com.github.encryptsl.lite.eco.common.database.tables.legacy.LegacyAccountTable
import com.github.encryptsl.lite.eco.common.extensions.io
import com.github.encryptsl.lite.eco.common.extensions.loggedTransaction
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import java.util.*

class LegacyDatabaseEcoModel(
    private val liteEco: LiteEco,
    private val currency: String
) {

    private val playersBalances = mutableMapOf<UUID, PlayerBalances.PlayerBalance>()

    private fun getPlayerBalances(): MutableMap<UUID, PlayerBalances.PlayerBalance> {
        loggedTransaction {
            LegacyAccountTable.selectAll().forEach { row ->
                playersBalances[UUID.fromString(row[LegacyAccountTable.uuid])] = PlayerBalances.PlayerBalance(
                    row[LegacyAccountTable.id].value.toInt(),
                    UUID.fromString(row[LegacyAccountTable.uuid]),
                    row[LegacyAccountTable.username],
                    row[LegacyAccountTable.money].toBigDecimal()
                )
            }
        }

        return playersBalances
    }

    suspend fun exportToLiteEcoDollarsTable() : Boolean {
        return io {
            loggedTransaction {
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
                    true
                } catch (e : ExposedSQLException) {
                    liteEco.logger.severe(e.sqlState)
                    false
                }
            }
        }
    }
}