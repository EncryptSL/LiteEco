package com.github.encryptsl.lite.eco.common.database.models

import com.github.encryptsl.lite.eco.api.interfaces.PlayerSQL
import com.github.encryptsl.lite.eco.common.database.entity.User
import com.github.encryptsl.lite.eco.common.database.tables.Account
import com.github.encryptsl.lite.eco.common.extensions.loggedTransaction
import org.bukkit.Bukkit
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
        loggedTransaction {
            val table = Account(currency)
            table.insertIgnore {
                it[table.username] = username
                it[table.uuid] = uuid
                it[table.money] = money
            }
        }
    }

    override fun getUserByUUID(uuid: UUID, currency: String): CompletableFuture<User> {
        val future = CompletableFuture<User>()
        loggedTransaction {
            val table = Account(currency)

            val row = table.select(table.uuid, table.username, table.money).where(table.uuid eq uuid)
                .singleOrNull()
            if (row == null) {
                future.completeExceptionally(Exception("User not Found"))
            } else {
                future.completeAsync { User(row[table.username], row[table.uuid], row[table.money]) }
            }
        }

        return future
    }

    override fun deletePlayerAccount(uuid: UUID, currency: String) {
        loggedTransaction {
            val table = Account(currency)
            table.deleteWhere { table.uuid eq uuid }
        }
    }

    override fun getExistPlayerAccount(uuid: UUID, currency: String): CompletableFuture<Boolean> {
        val future = CompletableFuture<Boolean>()
        val boolean = loggedTransaction {
            val table = Account(currency)
            !table.select(table.uuid).where(table.uuid eq uuid).empty()
        }
        return future.completeAsync { boolean }
    }

    override fun getTopBalance(currency: String): MutableMap<String, BigDecimal> = loggedTransaction {
        val table = Account(currency)

        table.selectAll().orderBy(table.money, SortOrder.DESC).associate {
            it[table.username] to it[table.money]
        }.toMutableMap()
    }

    override fun getPlayersIds(currency: String): CompletableFuture<MutableCollection<UUID>> {
        val future = CompletableFuture<MutableCollection<UUID>>()
        val collection = loggedTransaction {
            val table = Account(currency)
            table.selectAll().map {it[table.uuid] }.toMutableList()
        }

        return future.completeAsync { collection }
    }

    override fun depositMoney(uuid: UUID, currency: String, money: BigDecimal) {
        loggedTransaction {
            val table = Account(currency)
            table.update({ table.uuid eq uuid }) {
                it[table.money] = table.money plus money
            }
        }
    }
    override fun withdrawMoney(uuid: UUID, currency: String, money: BigDecimal) {
        loggedTransaction {
            val table = Account(currency)
            table.update({ table.uuid eq uuid }) {
                it[table.money] = table.money minus money
            }
        }
    }
    override fun setMoney(uuid: UUID, currency: String, money: BigDecimal) {
        loggedTransaction {
            val table = Account(currency)
            table.update({ table.uuid eq uuid }) {
                it[table.money] = money
            }
        }
    }

    override fun purgeAccounts(currency: String) {
        loggedTransaction {
            val table = Account(currency)
            table.deleteAll()
        }
    }

    override fun purgeDefaultAccounts(defaultMoney: BigDecimal, currency: String) {
        loggedTransaction {
            val table = Account(currency)
            table.deleteWhere { money eq defaultMoney }
        }
    }

    override fun purgeInvalidAccounts(currency: String) {
        loggedTransaction {
            val table = Account(currency)
            table.deleteWhere {
                uuid notInList Bukkit.getOfflinePlayers().map { it.uniqueId }
            }
        }
    }
}