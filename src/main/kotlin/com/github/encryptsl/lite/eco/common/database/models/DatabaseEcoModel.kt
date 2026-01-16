package com.github.encryptsl.lite.eco.common.database.models

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.interfaces.PlayerSQL
import com.github.encryptsl.lite.eco.common.database.entity.UserEntity
import com.github.encryptsl.lite.eco.common.database.tables.Account
import com.github.encryptsl.lite.eco.common.extensions.loggedTransaction
import org.bukkit.Bukkit
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException
import org.jetbrains.exposed.v1.jdbc.*
import java.math.BigDecimal
import java.sql.SQLException
import java.util.*

class DatabaseEcoModel : PlayerSQL {

    companion object {
        var debugFailMode: Boolean = false
    }

    override fun createPlayerAccount(username: String, uuid: UUID, currency: String, money: BigDecimal) {
        loggedTransaction {
            try {
                val table = Account(currency)
                table.insertIgnore {
                    it[table.username] = username
                    it[table.uuid] = uuid
                    it[table.money] = money
                }
            } catch (e : ExposedSQLException) {
                LiteEco.instance.componentLogger.error(e.message ?: e.localizedMessage)
            }
        }
    }

    override fun updatePlayerName(uuid: UUID, username: String, currency: String) {
        loggedTransaction {
            try {
                val table = Account(currency)
                table.update ({ table.uuid eq uuid }) {
                    it[table.username] = username
                }
            } catch (e : ExposedSQLException) {
                LiteEco.instance.componentLogger.error(e.message ?: e.localizedMessage)
            }
        }
    }

    override fun getUserByUUID(uuid: UUID, currency: String): UserEntity? {
        return loggedTransaction {
            val table = Account(currency)
            table.select(table.uuid, table.username, table.money)
                .where(table.uuid eq uuid).singleOrNull()?.let {
                    UserEntity(it[table.username], it[table.uuid], it[table.money])
                }
        }
    }

    override fun getBalance(uuid: UUID, currency: String): BigDecimal {
        return loggedTransaction {
            try {
                val table = Account(currency)
                val row = table.select(table.uuid, table.money).where { table.uuid eq uuid }.singleOrNull()
                row?.getOrNull(table.money) ?: BigDecimal.ZERO
            } catch (e : ExposedSQLException) {
                LiteEco.instance.componentLogger.error(e.message ?: e.localizedMessage)
                throw e
            }
        }
    }

    override fun deletePlayerAccount(uuid: UUID, currency: String) {
        loggedTransaction {
            try {
                val table = Account(currency)
                table.deleteWhere { table.uuid eq uuid }
            } catch (e : ExposedSQLException) {
                LiteEco.instance.componentLogger.error(e.message ?: e.localizedMessage)
            }
        }
    }

    override fun getExistPlayerAccount(uuid: UUID, currency: String): Boolean {
        return loggedTransaction {
            try {
                val table = Account(currency)
                !table.select(table.uuid).where(table.uuid eq uuid).empty()
            } catch (e : ExposedSQLException) {
                LiteEco.instance.componentLogger.error(e.message ?: e.localizedMessage)
                false
            }
        }
    }

    override fun getTopBalance(currency: String): MutableMap<String, UserEntity> = loggedTransaction {
        val table = Account(currency)

        table.selectAll().orderBy(table.money, SortOrder.DESC).associate {
            it[table.username] to UserEntity(it[table.username], it[table.uuid], it[table.money])
        }.toMutableMap()
    }

    override fun getUUIDNameMap(currency: String): MutableMap<UUID, String> = loggedTransaction {
        val table = Account(currency)
        table.selectAll().associate {
            it[table.uuid] to it[table.username]
        }.toMutableMap()
    }

    override fun getPlayersIds(currency: String): MutableCollection<UUID> {
        return loggedTransaction {
            try {
                val table = Account(currency)
                table.selectAll().map {it[table.uuid] }.toMutableList()
            } catch (e : ExposedSQLException) {
                LiteEco.instance.componentLogger.error(e.message ?: e.localizedMessage)
                mutableListOf()
            }
        }
    }

    override fun deposit(uuid: UUID, currency: String, money: BigDecimal) {
        loggedTransaction {
            try {
                val table = Account(currency)
                table.update({ table.uuid eq uuid }) {
                    it[table.money] = table.money + money
                }
            } catch (e : ExposedSQLException) {
                LiteEco.instance.componentLogger.error(e.message ?: e.localizedMessage)
            }
        }
    }
    override fun withdraw(uuid: UUID, currency: String, money: BigDecimal) {
        loggedTransaction {
            try {
                val table = Account(currency)
                table.update({ table.uuid eq uuid }) {
                    it[table.money] = table.money - money
                }
            } catch (e : ExposedSQLException) {
                LiteEco.instance.componentLogger.error(e.message ?: e.localizedMessage)
            }
        }
    }
    override fun set(uuid: UUID, currency: String, money: BigDecimal) {

        if (debugFailMode) {
            println("[DEBUG] I am throwing out a false error for $uuid")
            throw SQLException("DEBUG: Database is currently in fail-mode.")
        }

        loggedTransaction {
            try {
                val table = Account(currency)
                table.update({ table.uuid eq uuid }) {
                    it[table.money] = money
                }
            } catch (e : ExposedSQLException) {
                LiteEco.instance.componentLogger.error(e.message ?: e.localizedMessage)
            }
        }
    }

    override fun purgeAccounts(currency: String) {
        loggedTransaction {
            try {
                Account(currency).deleteAll()
            } catch (e : ExposedSQLException) {
                LiteEco.instance.componentLogger.error(e.message ?: e.localizedMessage)
            }
        }
    }

    override fun purgeDefaultAccounts(defaultMoney: BigDecimal, currency: String) {
        loggedTransaction {
            try {
                Account(currency).deleteWhere { money eq defaultMoney }
            } catch (e : ExposedSQLException) {
                LiteEco.instance.componentLogger.error(e.message ?: e.localizedMessage)
            }
        }
    }

    override fun purgeInvalidAccounts(currency: String) {
        loggedTransaction {
            try {
                Account(currency).deleteWhere {
                    uuid notInList Bukkit.getOfflinePlayers().map { it.uniqueId }
                }
            } catch (e : ExposedSQLException) {
                LiteEco.instance.componentLogger.error(e.message ?: e.localizedMessage)
            }
        }
    }

    override fun batchInsert(importData: List<Triple<UUID, String, BigDecimal>>, currency: String) {
        loggedTransaction {
            try {
                val table = Account(currency)
                table.batchInsert(importData) { (uuid, username, money) ->
                    this[table.uuid] = uuid
                    this[table.username] = username
                    this[table.money] = money
                }
            } catch (e : ExposedSQLException) {
                LiteEco.instance.componentLogger.error(e.message ?: e.localizedMessage)
            }
        }
    }
}