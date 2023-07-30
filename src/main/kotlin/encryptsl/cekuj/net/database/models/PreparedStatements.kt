package encryptsl.cekuj.net.database.models

import encryptsl.cekuj.net.api.interfaces.DatabaseSQLProvider
import encryptsl.cekuj.net.database.tables.Account
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.notInList
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class PreparedStatements : DatabaseSQLProvider {

    override fun createPlayerAccount(uuid: UUID, money: Double) {
            transaction { Account.insert {
                it[Account.uuid] = uuid.toString()
                it[Account.money] = money
            } }
    }

    override fun deletePlayerAccount(uuid: UUID) {
        transaction { Account.deleteWhere { Account.uuid eq uuid.toString() } }
    }

    override fun getExistPlayerAccount(uuid: UUID): Boolean
        = transaction { !Account.select(Account.uuid eq uuid.toString() ).empty() }

    override fun getTopBalance(top: Int): MutableMap<String, Double> {
        val hashMap: HashMap<String, Double> = HashMap()
        transaction {
            Account.selectAll().limit(top).forEach { a ->
                hashMap[a[Account.uuid]] = a[Account.money]
            }
        }

        return hashMap
    }

    override fun getTopBalance(): MutableMap<String, Double> {
        val hashMap: HashMap<String, Double> = HashMap()
        transaction {
            Account.selectAll().forEach { a ->
                hashMap[a[Account.uuid]] = a[Account.money]
            }
        }

        return hashMap
    }

    override fun getBalance(uuid: UUID): Double
        = transaction { Account.select(Account.uuid eq uuid.toString()).first()[Account.money] }

    override fun depositMoney(uuid: UUID, money: Double) {
        transaction { Account.update ({ Account.uuid eq uuid.toString() }) {
            it[Account.money] = money
        } }
    }

    override fun withdrawMoney(uuid: UUID, money: Double) {
        transaction { Account.update ({ Account.uuid eq uuid.toString() }) {
            it[Account.money] = money
        } }
    }

    override fun setMoney(uuid: UUID, money: Double) {
        transaction { Account.update ({ Account.uuid eq uuid.toString() }) {
            it[Account.money] = money
        } }
    }

    override fun purgeAccounts() {
        transaction { Account.deleteAll() }
    }

    override fun purgeDefaultAccounts(defaultMoney: Double) {
        transaction { Account.deleteWhere { money eq defaultMoney } }
    }

    override fun purgeInvalidAccounts() {
        val validUUIDs = transaction {
            Account.slice(Account.uuid).selectAll()
                .mapNotNull { row -> runCatching { UUID.fromString(row[Account.uuid]) }.getOrNull() }
                .map  { it.toString() }
        }
        transaction {
            Account.deleteWhere {
                uuid notInList validUUIDs
            }
        }
    }
}