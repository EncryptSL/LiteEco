package com.github.encryptsl.lite.eco.common.database.models

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.interfaces.PlayerSQL
import com.github.encryptsl.lite.eco.common.database.entity.UserEntity
import com.github.encryptsl.lite.eco.common.database.tables.Account
import com.github.encryptsl.lite.eco.common.database.tables.toUserEntity
import com.github.encryptsl.lite.eco.common.extensions.loggedTransaction
import com.github.encryptsl.lite.eco.common.extensions.moneyScale
import com.github.encryptsl.lite.eco.common.migrator.entity.PlayerBalances
import org.bukkit.Bukkit
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException
import org.jetbrains.exposed.v1.jdbc.*
import java.math.BigDecimal
import java.sql.SQLException
import java.util.*
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

class AccountRepository : PlayerSQL {

    companion object {
        var debugFailMode: Boolean = false
    }

    private fun accountTable(currency: String): Account = Account(currency.lowercase())

    override fun createPlayerAccount(username: String, uuid: UUID, currency: String, money: BigDecimal) {
        loggedTransaction {
            try {
                val table = accountTable(currency)
                table.insertIgnore {
                    it[table.username] = username
                    it[table.uuid] = uuid.toKotlinUuid()
                    it[table.money] = money.moneyScale()
                }
            } catch (e: ExposedSQLException) {
                LiteEco.instance.componentLogger.error("Failed to create account for $username ($uuid): ${e.message}")
            }
        }
    }

    override fun updatePlayerName(uuid: UUID, username: String, currency: String) {
        loggedTransaction {
            try {
                val table = accountTable(currency)
                table.update({ table.uuid eq uuid.toKotlinUuid() }) {
                    it[table.username] = username
                }
            } catch (e: ExposedSQLException) {
                LiteEco.instance.componentLogger.error("Failed to update name for $uuid to $username: ${e.message}")
            }
        }
    }

    override fun getUserByUUID(uuid: UUID, currency: String): UserEntity? = loggedTransaction {
        val table = accountTable(currency)
        table.selectAll()
            .where { table.uuid eq uuid.toKotlinUuid() }
            .singleOrNull()
            ?.toUserEntity(table)
    }

    override fun getBalance(uuid: UUID, currency: String): BigDecimal = loggedTransaction {
        try {
            val table = accountTable(currency)
            table
                .select(table.money)
                .where { table.uuid eq uuid.toKotlinUuid() }
                .singleOrNull()
                ?.get(table.money) ?: BigDecimal.ZERO
        } catch (e: ExposedSQLException) {
            LiteEco.instance.componentLogger.error("Failed to get balance for $uuid: ${e.message}")
            throw e
        }
    }

    override fun deletePlayerAccount(uuid: UUID, currency: String) {
        loggedTransaction {
            try {
                val table = accountTable(currency)
                table.deleteWhere { table.uuid eq uuid.toKotlinUuid() }
            } catch (e: ExposedSQLException) {
                LiteEco.instance.componentLogger.error("Failed to delete account $uuid: ${e.message}")
            }
        }
    }

    override fun getExistPlayerAccount(uuid: UUID, currency: String): Boolean = loggedTransaction {
        try {
            val table = accountTable(currency)
            table.select(table.uuid eq uuid.toKotlinUuid()).count() > 0
        } catch (e: ExposedSQLException) {
            LiteEco.instance.componentLogger.error("Failed to check existence for $uuid: ${e.message}")
            false
        }
    }

    override fun getTopBalance(currency: String): MutableMap<String, UserEntity> = loggedTransaction {
        val table = accountTable(currency)
        table.selectAll()
            .orderBy(table.money, SortOrder.DESC)
            .limit(100)
            .associate {
                it[table.username] to UserEntity(
                    it[table.username],
                    it[table.uuid].toJavaUuid(),
                    it[table.money]
                )
            }.toMutableMap()
    }

    override fun getUUIDNameMap(currency: String): MutableMap<UUID, String> = loggedTransaction {
        val table = accountTable(currency)
        table.selectAll().associate {
            it[table.uuid].toJavaUuid() to it[table.username]
        }.toMutableMap()
    }

    override fun getBalancesForCurrency(currency: String): List<PlayerBalances.PlayerBalance> = loggedTransaction {
        val table = accountTable(currency)
        table.selectAll().mapIndexed { index, row ->
            PlayerBalances.PlayerBalance(
                id = index + 1,
                username = row[table.username],
                uuid = row[table.uuid].toJavaUuid(),
                money = row[table.money]
            )
        }
    }

    override fun getPlayersIds(currency: String): MutableCollection<UUID> = loggedTransaction {
        try {
            val table = accountTable(currency)
            table.select(table.uuid).map { it[table.uuid].toJavaUuid() }.toMutableList()
        } catch (e: ExposedSQLException) {
            LiteEco.instance.componentLogger.error("Failed to get player IDs: ${e.message}")
            mutableListOf()
        }
    }

    override fun deposit(uuid: UUID, currency: String, money: BigDecimal) {
        val sanitizedMoney = money.moneyScale()
        if (sanitizedMoney <= BigDecimal.ZERO) return

        loggedTransaction {
            try {
                val table = accountTable(currency)
                table.update({ table.uuid eq uuid.toKotlinUuid() }) {
                    it.update(table.money, table.money + sanitizedMoney)
                }
            } catch (e: ExposedSQLException) {
                LiteEco.instance.componentLogger.error("Deposit failed for $uuid: ${e.message}")
            }
        }
    }

    override fun withdraw(uuid: UUID, currency: String, money: BigDecimal) {
        val sanitizedMoney = money.moneyScale()
        if (sanitizedMoney <= BigDecimal.ZERO) return

        loggedTransaction {
            try {
                val table = accountTable(currency)
                table.update({ table.uuid eq uuid.toKotlinUuid() }) {
                    it.update(table.money, table.money - sanitizedMoney)
                }
            } catch (e: ExposedSQLException) {
                LiteEco.instance.componentLogger.error("Withdraw failed for $uuid: ${e.message}")
            }
        }
    }

    override fun set(uuid: UUID, currency: String, money: BigDecimal): Boolean {
        if (debugFailMode) {
            throw SQLException("DEBUG: Database is currently in fail-mode.")
        }

        val sanitizedMoney = money.moneyScale()

        return try {
            loggedTransaction {
                val table = accountTable(currency)
                val rowsUpdated = table.update({ table.uuid eq uuid.toKotlinUuid() }) {
                    it[table.money] = sanitizedMoney
                }
                rowsUpdated > 0
            }
        } catch (e: Exception) {
            LiteEco.instance.logger.error("Failed to set balance for $uuid: ${e.message}")
            false
        }
    }

    private fun deleteAccounts(currency: String, op: Account.() -> Op<Boolean>): Int {
        return loggedTransaction {
            try {
                val table = accountTable(currency)
                table.deleteWhere(op = op)
            } catch (e: ExposedSQLException) {
                LiteEco.instance.componentLogger.error("Purge failed for currency $currency: ${e.message}")
                0
            }
        }
    }

    override fun purgeAccounts(currency: String): Int {
        return loggedTransaction {
            try {
                accountTable(currency).deleteAll()
            } catch (e: ExposedSQLException) {
                LiteEco.instance.componentLogger.error("Failed to purge all accounts for $currency: ${e.message}")
                0
            }
        }
    }

    override fun purgeTestAccounts(): Int = deleteAccounts(LiteEco.instance.currencyImpl.defaultCurrency()) {
        val table = accountTable(LiteEco.instance.currencyImpl.defaultCurrency())
        table.username like "TestPlayer_%"
    }

    override fun purgeDefaultAccounts(defaultMoney: BigDecimal, currency: String): Int = deleteAccounts(currency) {
        val table = accountTable(currency)
        table.money eq defaultMoney
    }

    override fun purgeInvalidAccounts(currency: String): Int {
        val onlinePlayerUuids = Bukkit.getOfflinePlayers().map { it.uniqueId.toKotlinUuid() }

        return loggedTransaction {
            try {
                val table = accountTable(currency)
                table.deleteWhere {
                    uuid notInList onlinePlayerUuids
                }
            } catch (e: ExposedSQLException) {
                LiteEco.instance.componentLogger.error("Failed to purge invalid accounts for $currency: ${e.message}")
                0
            }
        }
    }

    override fun batchInsert(importData: List<Triple<UUID, String, BigDecimal>>, currency: String) {
        if (importData.isEmpty()) return

        loggedTransaction {
            try {
                val table = accountTable(currency)
                table.batchInsert(importData) { (uuid, username, money) ->
                    this[table.uuid] = uuid.toKotlinUuid()
                    this[table.username] = username
                    this[table.money] = money.moneyScale()
                }
            } catch (e: ExposedSQLException) {
                LiteEco.instance.componentLogger.error("Batch insert failed for $currency: ${e.message}")
            }
        }
    }
}