package com.github.encryptsl.lite.eco.common.database.models

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.interfaces.PlayerSQL
import com.github.encryptsl.lite.eco.common.database.entity.User
import com.github.encryptsl.lite.eco.common.database.tables.Account
import com.github.encryptsl.lite.eco.common.extensions.loggedTransaction
import org.bukkit.Bukkit
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.notInList
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException
import org.jetbrains.exposed.v1.jdbc.*
import java.math.BigDecimal
import java.util.*

class DatabaseEcoModel : PlayerSQL {

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
                LiteEco.instance.logger.severe(e.message ?: e.localizedMessage)
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
                LiteEco.instance.logger.severe(e.message ?: e.localizedMessage)
            }
        }
    }

    override fun getUserByUUID(uuid: UUID, currency: String): User? {
        return loggedTransaction {
            try {
                val table = Account(currency)
                val row = table.select(table.uuid, table.username, table.money).where(table.uuid eq uuid).single()
                User(row[table.username], row[table.uuid], row[table.money])
            } catch (e : Exception) {
                LiteEco.instance.logger.severe(e.message ?: e.localizedMessage)
                null
            }
        }
    }

    override fun getBalance(uuid: UUID, currency: String): BigDecimal {
        return loggedTransaction {
            try {
                val table = Account(currency)
                table.select(table.uuid, table.money).where { table.uuid eq uuid }.single()[table.money]
            } catch (e : ExposedSQLException) {
                LiteEco.instance.logger.severe(e.message ?: e.localizedMessage)
                BigDecimal.ZERO
            }
        }
    }

    override fun deletePlayerAccount(uuid: UUID, currency: String) {
        loggedTransaction {
            try {
                val table = Account(currency)
                table.deleteWhere { table.uuid eq uuid }
            } catch (e : ExposedSQLException) {
                LiteEco.instance.logger.severe(e.message ?: e.localizedMessage)
            }
        }
    }

    override fun getExistPlayerAccount(uuid: UUID, currency: String): Boolean {
        return loggedTransaction {
            try {
                val table = Account(currency)
                !table.select(table.uuid).where(table.uuid eq uuid).empty()
            } catch (e : ExposedSQLException) {
                LiteEco.instance.logger.severe(e.message ?: e.localizedMessage)
                false
            }
        }
    }

    override fun getTopBalance(currency: String): MutableMap<String, User> = loggedTransaction {
        val table = Account(currency)

        table.selectAll().orderBy(table.money, SortOrder.DESC).associate {
            it[table.username] to User(it[table.username], it[table.uuid], it[table.money])
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
                LiteEco.instance.logger.severe(e.message ?: e.localizedMessage)
                mutableListOf()
            }
        }
    }

    override fun deposit(uuid: UUID, currency: String, money: BigDecimal) {
        loggedTransaction {
            try {
                val table = Account(currency)
                table.update({ table.uuid eq uuid }) {
                    with(SqlExpressionBuilder) {
                        it[table.money] = table.money + money
                    }
                }
            } catch (e : ExposedSQLException) {
                LiteEco.instance.logger.severe(e.message ?: e.localizedMessage)
            }
        }
    }
    override fun withdraw(uuid: UUID, currency: String, money: BigDecimal) {
        loggedTransaction {
            try {
                val table = Account(currency)
                table.update({ table.uuid eq uuid }) {
                    with(SqlExpressionBuilder) {
                        it[table.money] = table.money - money
                    }
                }
            } catch (e : ExposedSQLException) {
                LiteEco.instance.logger.severe(e.message ?: e.localizedMessage)
            }
        }
    }
    override fun set(uuid: UUID, currency: String, money: BigDecimal) {
        loggedTransaction {
            try {
                val table = Account(currency)
                table.update({ table.uuid eq uuid }) {
                    with(SqlExpressionBuilder) {
                        it[table.money] = money
                    }
                }
            } catch (e : ExposedSQLException) {
                LiteEco.instance.logger.severe(e.message ?: e.localizedMessage)
            }
        }
    }

    override fun purgeAccounts(currency: String) {
        loggedTransaction {
            try {
                Account(currency).deleteAll()
            } catch (e : ExposedSQLException) {
                LiteEco.instance.logger.severe(e.message ?: e.localizedMessage)
            }
        }
    }

    override fun purgeDefaultAccounts(defaultMoney: BigDecimal, currency: String) {
        loggedTransaction {
            try {
                Account(currency).deleteWhere { money eq defaultMoney }
            } catch (e : ExposedSQLException) {
                LiteEco.instance.logger.severe(e.message ?: e.localizedMessage)
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
                LiteEco.instance.logger.severe(e.message ?: e.localizedMessage)
            }
        }
    }
}