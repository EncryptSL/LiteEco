package com.github.encryptsl.lite.eco.common.database.models

import com.github.encryptsl.lite.eco.api.interfaces.PlayerSQLProvider
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

class DatabaseEcoModel : PlayerSQLProvider {

    override fun createPlayerAccount(username: String, uuid: UUID, money: Double) {
        loggedTransaction {
            Account.insertIgnore {
                it[Account.username] = username
                it[Account.uuid] = uuid.toString()
                it[Account.money] = money
            }
        }
    }

    override fun deletePlayerAccount(uuid: UUID) {
        loggedTransaction {
            Account.deleteWhere { Account.uuid eq uuid.toString() }
        }
    }

    override fun getExistPlayerAccount(uuid: UUID): CompletableFuture<Boolean> {
        val future = CompletableFuture<Boolean>()
        val boolean = loggedTransaction { !Account.select(Account.uuid).where(Account.uuid eq uuid.toString()).empty() }
        future.completeAsync { boolean }
        return future
    }

    override fun getTopBalance(top: Int): MutableMap<String, Double> = loggedTransaction {
        Account.selectAll().limit(top).orderBy(Account.money, SortOrder.DESC).associate {
            it[Account.uuid] to it[Account.money]
        }.toMutableMap()
    }

    override fun getTopBalance(): MutableMap<String, Double> = loggedTransaction {
        Account.selectAll().orderBy(Account.money, SortOrder.DESC).associate {
            it[Account.uuid] to it[Account.money]
        }.toMutableMap()
    }

    override fun getPlayersIds(): MutableCollection<UUID> = loggedTransaction {
        Account.selectAll().map { UUID.fromString(it[Account.uuid]) }.toMutableList()
    }

    override fun getBalance(uuid: UUID): CompletableFuture<Double> {
        val future = CompletableFuture<Double>()
        val balance = loggedTransaction {
            Account.select(Account.uuid, Account.money).where(Account.uuid eq uuid.toString()).first()[Account.money]
        }
        future.completeAsync { balance }
        return future
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
        val validPlayerUUIDs = Bukkit.getOfflinePlayers().mapNotNull { runCatching { it.uniqueId }.getOrNull() }.map { it.toString() }
        loggedTransaction {
            Account.deleteWhere {
                uuid notInList validPlayerUUIDs
            }
        }
    }
}