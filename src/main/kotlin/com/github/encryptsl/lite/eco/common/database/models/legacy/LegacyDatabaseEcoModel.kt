package com.github.encryptsl.lite.eco.common.database.models.legacy

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.migrator.entity.PlayerBalances
import com.github.encryptsl.lite.eco.common.database.tables.legacy.LegacyAccountTable
import com.github.encryptsl.lite.eco.common.extensions.loggedTransaction
import org.jetbrains.exposed.v1.jdbc.exists
import org.jetbrains.exposed.v1.jdbc.selectAll
import java.util.*

class LegacyDatabaseEcoModel(
    private val liteEco: LiteEco
) {

    private val playersBalances = mutableMapOf<UUID, PlayerBalances.PlayerBalance>()

    internal fun getPlayerBalances(): MutableMap<UUID, PlayerBalances.PlayerBalance> {
        loggedTransaction {
            if (LegacyAccountTable.exists()) {
                LegacyAccountTable.selectAll().forEach { row ->
                    playersBalances[UUID.fromString(row[LegacyAccountTable.uuid])] = PlayerBalances.PlayerBalance(
                        row[LegacyAccountTable.id].value.toInt(),
                        UUID.fromString(row[LegacyAccountTable.uuid]),
                        row[LegacyAccountTable.username],
                        row[LegacyAccountTable.money].toBigDecimal()
                    )
                }
            } else {
                liteEco.logger.error("Legacy table does not exist, skipping migration.")
            }
        }
        return playersBalances
    }
}