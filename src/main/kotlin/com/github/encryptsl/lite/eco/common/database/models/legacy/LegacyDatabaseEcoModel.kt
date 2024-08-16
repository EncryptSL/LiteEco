package com.github.encryptsl.lite.eco.common.database.models.legacy

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.common.database.tables.legacy.LegacyAccountTable
import org.bukkit.Bukkit
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class LegacyDatabaseEcoModel(
    private val liteEco: LiteEco
) {

    private val playersBalances = mutableMapOf<UUID, PlayerBalances>()

    private fun getPlayerBalances(): MutableMap<UUID, PlayerBalances> {

        transaction {
            LegacyAccountTable.selectAll().forEach { resultRow: ResultRow ->
                playersBalances[UUID.fromString(resultRow[LegacyAccountTable.uuid])]=
                    PlayerBalances(resultRow[LegacyAccountTable.id], resultRow[LegacyAccountTable.username], resultRow[LegacyAccountTable.money])
            }
        }

        return playersBalances
    }

    fun convertOldBalance(currency: String): Boolean {
        return try {
            getPlayerBalances().forEach { (key, data) ->
                liteEco.databaseEcoModel.createPlayerAccount(Bukkit.getOfflinePlayer(key).name.toString(), key, currency, data.money.toBigDecimal())
            }
            liteEco.loggerModel.info("Migration Finished !")
            true
        } catch (e : ExposedSQLException) {
            liteEco.loggerModel.error(e.message ?: e.localizedMessage)
            false
        }
    }

    data class PlayerBalances(val id: Int, val username: String?, val money: Double)

}