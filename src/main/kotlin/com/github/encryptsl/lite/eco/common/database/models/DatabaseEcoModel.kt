package com.github.encryptsl.lite.eco.common.database.models

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.interfaces.PlayerSQL
import com.github.encryptsl.lite.eco.common.database.entity.User
import com.github.encryptsl.lite.eco.common.database.tables.Account
import com.github.encryptsl.lite.eco.common.extensions.loggedTransaction
import org.bukkit.Bukkit
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.minus
import org.jetbrains.exposed.sql.SqlExpressionBuilder.notInList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.CompletableFuture

class DatabaseEcoModel : PlayerSQL {

    override fun createPlayerAccount(username: String, uuid: UUID, currency: String, money: BigDecimal) {
        try {
            loggedTransaction {
                val table = Account(currency)
                table.insertIgnore {
                    it[table.username] = username
                    it[table.uuid] = uuid
                    it[table.money] = money
                }
            }
        } catch (e : ExposedSQLException) {
            LiteEco.instance.logger.severe(e.message ?: e.localizedMessage)
        }
    }

    override fun getUserByUUID(uuid: UUID, currency: String): CompletableFuture<User> {
        val future = CompletableFuture<User>()
        try {
            loggedTransaction {
                val table = Account(currency)
                val row = table.select(table.uuid, table.username, table.money).where(table.uuid eq uuid).singleOrNull()
                if (row == null) {
                    future.completeExceptionally(Exception("User not Found"))
                } else {
                    future.completeAsync { User(row[table.username], row[table.uuid], row[table.money]) }
                }
            }
        } catch (e : ExposedSQLException) {
            LiteEco.instance.logger.severe(e.message ?: e.localizedMessage)
        }
        return future
    }

    override fun deletePlayerAccount(uuid: UUID, currency: String) {
        try {
            loggedTransaction {
                val table = Account(currency)
                table.deleteWhere { table.uuid eq uuid }
            }
        } catch (e : ExposedSQLException) {
            LiteEco.instance.logger.severe(e.message ?: e.localizedMessage)
        }
    }

    override fun getExistPlayerAccount(uuid: UUID, currency: String): CompletableFuture<Boolean> {
        val future = CompletableFuture<Boolean>()
        try {
            val boolean = loggedTransaction {
                val table = Account(currency)
                !table.select(table.uuid).where(table.uuid eq uuid).empty()
            }
            return future.completeAsync { boolean }
        } catch (e : ExposedSQLException) {
            LiteEco.instance.logger.severe(e.message ?: e.localizedMessage)
        }

        return future.completeAsync { false }
    }

    override fun getTopBalance(currency: String): MutableMap<String, BigDecimal> = loggedTransaction {
        val table = Account(currency)

        table.selectAll().orderBy(table.money, SortOrder.DESC).associate {
            it[table.username] to it[table.money]
        }.toMutableMap()
    }

    override fun getPlayersIds(currency: String): CompletableFuture<MutableCollection<UUID>> {
        val future = CompletableFuture<MutableCollection<UUID>>()
        try {
            val collection = loggedTransaction {
                val table = Account(currency)
                table.selectAll().map {it[table.uuid] }.toMutableList()
            }
            return future.completeAsync { collection }
        } catch (e : ExposedSQLException) {
            LiteEco.instance.logger.severe(e.message ?: e.localizedMessage)
        }
        return future.completeAsync { mutableListOf() }
    }

    override fun depositMoney(uuid: UUID, currency: String, money: BigDecimal) {
        try {
            loggedTransaction {
                val table = Account(currency)
                table.update({ table.uuid eq uuid }) {
                    it[table.money] = table.money plus money
                }
            }
        } catch (e : ExposedSQLException) {
            LiteEco.instance.logger.severe(e.message ?: e.localizedMessage)
        }
    }
    override fun withdrawMoney(uuid: UUID, currency: String, money: BigDecimal) {
        try {
            loggedTransaction {
                val table = Account(currency)
                table.update({ table.uuid eq uuid }) {
                    it[table.money] = table.money minus money
                }
            }
        } catch (e : ExposedSQLException) {
            LiteEco.instance.logger.severe(e.message ?: e.localizedMessage)
        }
    }
    override fun setMoney(uuid: UUID, currency: String, money: BigDecimal) {
        try {
            loggedTransaction {
                val table = Account(currency)
                table.update({ table.uuid eq uuid }) {
                    it[table.money] = money
                }
            }
        } catch (e : ExposedSQLException) {
            LiteEco.instance.logger.severe(e.message ?: e.localizedMessage)
        }
    }

    override fun purgeAccounts(currency: String) {
        try {
            loggedTransaction {
                val table = Account(currency)
                table.deleteAll()
            }
        } catch (e : ExposedSQLException) {
            LiteEco.instance.logger.severe(e.message ?: e.localizedMessage)
        }
    }

    override fun purgeDefaultAccounts(defaultMoney: BigDecimal, currency: String) {
        try {
            loggedTransaction {
                val table = Account(currency)
                table.deleteWhere { money eq defaultMoney }
            }
        } catch (e : ExposedSQLException) {
            LiteEco.instance.logger.severe(e.message ?: e.localizedMessage)
        }
    }

    override fun purgeInvalidAccounts(currency: String) {
        try {
            loggedTransaction {
                val table = Account(currency)
                table.deleteWhere {
                    uuid notInList Bukkit.getOfflinePlayers().map { it.uniqueId }
                }
            }
        } catch (e : ExposedSQLException) {
            LiteEco.instance.logger.severe(e.message ?: e.localizedMessage)
        }
    }
}