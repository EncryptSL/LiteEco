package encryptsl.cekuj.net.database.models

import encryptsl.cekuj.net.api.interfaces.DatabaseSQLProvider
import encryptsl.cekuj.net.database.tables.Money
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class PreparedStatements : DatabaseSQLProvider {

    override fun createPlayerAccount(uuid: UUID, money: Double) {
            transaction { Money.insert {
                it[Money.uuid] = uuid.toString()
                it[Money.money] = money
            } }
    }

    override fun deletePlayerAccount(uuid: UUID) {
        transaction { Money.deleteWhere { Money.uuid eq uuid.toString() } }
    }

    override fun getExistPlayerAccount(uuid: UUID): Boolean
        = transaction { !Money.select(Money.uuid eq uuid.toString() ).empty() }

    override fun getTopBalance(top: Int): MutableMap<String, Double> {
        val hashMap: HashMap<String, Double> = HashMap()
        transaction {
            Money.selectAll().limit(top).forEach { a ->
                hashMap[a[Money.uuid]] = a[Money.money]
            }
        }

        return hashMap
    }

    override fun getTopBalance(): MutableMap<String, Double> {
        val hashMap: HashMap<String, Double> = HashMap()
        transaction {
            Money.selectAll().forEach { a ->
                hashMap[a[Money.uuid]] = a[Money.money]
            }
        }

        return hashMap
    }

    override fun getBalance(uuid: UUID): Double
        = transaction { Money.select(Money.uuid eq uuid.toString()).first()[Money.money] }

    override fun depositMoney(uuid: UUID, money: Double) {
        transaction { Money.update ({ Money.uuid eq uuid.toString() }) {
            it[Money.money] = money
        } }
    }

    override fun withdrawMoney(uuid: UUID, money: Double) {
        transaction { Money.update ({ Money.uuid eq uuid.toString() }) {
            it[Money.money] = money
        } }
    }

    override fun setMoney(uuid: UUID, money: Double) {
        transaction { Money.update ({ Money.uuid eq uuid.toString() }) {
            it[Money.money] = money
        } }
    }
}