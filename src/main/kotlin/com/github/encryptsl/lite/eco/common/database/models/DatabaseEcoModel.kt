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
import java.util.*
import java.util.concurrent.CompletableFuture

class DatabaseEcoModel : PlayerSQL {

    override fun createPlayerAccount(username: String, uuid: UUID, money: Double) {
        loggedTransaction {
            Account.insertIgnore {
                it[Account.username] = username
                it[Account.uuid] = uuid.toString()
                it[Account.money] = money
            }
        }
    }

    override fun getUserByUUID(uuid: UUID): CompletableFuture<User> {
        val future = CompletableFuture<User>()
        loggedTransaction {
            val row = Account.select(Account.uuid, Account.username, Account.money).where(Account.uuid eq uuid.toString())
                .singleOrNull()
            if (row == null) {
                future.completeExceptionally(Exception("User not Found"))
            } else {
                future.completeAsync { User(row[Account.username], UUID.fromString(row[Account.uuid]), row[Account.money]) }
            }
        }

        return future
    }

    override fun deletePlayerAccount(uuid: UUID) {
        loggedTransaction {
            Account.deleteWhere { Account.uuid eq uuid.toString() }
        }
    }

    override fun getExistPlayerAccount(uuid: UUID): CompletableFuture<Boolean> {
        val future = CompletableFuture<Boolean>()
        val boolean = loggedTransaction { !Account.select(Account.uuid).where(Account.uuid eq uuid.toString()).empty() }
        return future.completeAsync { boolean }
    }

    override fun getTopBalance(): MutableMap<String, Double> = loggedTransaction {
        Account.selectAll().orderBy(Account.money, SortOrder.DESC).associate {
            it[Account.uuid] to it[Account.money]
        }.toMutableMap()
    }

    override fun getPlayersIds(): CompletableFuture<MutableCollection<UUID>> {
        val future = CompletableFuture<MutableCollection<UUID>>()
        val collection = loggedTransaction {
            Account.selectAll().map { UUID.fromString(it[Account.uuid]) }.toMutableList()
        }

        return future.completeAsync { collection }
    }

    override fun depositMoney(uuid: UUID, money: Double) {
        loggedTransaction {
            Account.update({ Account.uuid eq uuid.toString() }) {
                it[Account.money] = Account.money plus money
            }
        }
    }
    override fun withdrawMoney(uuid: UUID, money: Double) {
        loggedTransaction {
            Account.update({ Account.uuid eq uuid.toString() }) {
                it[Account.money] = Account.money minus money
            }
        }
    }
    override fun setMoney(uuid: UUID, money: Double) {
        loggedTransaction {
            Account.update({ Account.uuid eq uuid.toString() }) {
                it[Account.money] = money
            }
        }
    }

    override fun purgeAccounts() {
        loggedTransaction { Account.deleteAll() }
    }

    override fun purgeDefaultAccounts(defaultMoney: Double) {
        loggedTransaction { Account.deleteWhere { money eq defaultMoney } }
    }

    override fun purgeInvalidAccounts() {
        loggedTransaction {
            Account.deleteWhere {
                uuid notInList Bukkit.getOfflinePlayers().map { it.uniqueId.toString() }
            }
        }
    }
}