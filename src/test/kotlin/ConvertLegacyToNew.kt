import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.PrintWriter
import java.math.BigDecimal
import java.util.*

class ConvertLegacyToNew {

    val map = mutableMapOf<UUID, PlayerBalance>()

    @Test
    fun runScript() {

        val config = HikariDataSource().apply {
            jdbcUrl = "jdbc:sqlite:src/test/resources/database.db"
            username = "root"
            password = "pass"
            poolName = this.javaClass.simpleName
            maximumPoolSize = 10
        }

        Database.connect(config)


        migrateToSQL(getPlayerBalancesDollarsTable(), "lite_eco_migration")
        //print(getPlayerBalancesDollarsTable())
        //convertToNewTable(getPlayerBalances())
    }

    data class PlayerBalance(val id: Int, val username: String?, val money: BigDecimal)

    private fun migrateToSQL(data: MutableMap<UUID, PlayerBalance>, fileName: String, currency: String = "dollars"): Boolean {
        val file = File("src/test/resources/migration", "${fileName}_${currency}_${System.currentTimeMillis()}.sql")

        return try {
            file.parentFile.mkdirs()
            PrintWriter(FileWriter(file)).use { writer ->
                writer.println("DROP TABLE IF EXISTS lite_eco_$currency;")
                writer.println("CREATE TABLE lite_eco_$currency (id INT(11), username VARCHAR(36), uuid BINARY(16), money DECIMAL(18,9));")
                val insertStatements = data.toList().joinToString {
                    "\n('${it.second.username}', 0x${it.first.toString().replace("-", "")}, ${it.second.money})"
                }
                writer.println("INSERT INTO lite_eco_$currency (username, uuid, money) VALUES $insertStatements;")
                writer.println("ALTER TABLE `lite_eco_$currency` ADD PRIMARY KEY(`id`);")
            }
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    private fun convertToNewTable(data: MutableMap<UUID, PlayerBalance>) {
        transaction {
            SchemaUtils.create(AccountTestTable)
            data.forEach { (t, u) ->
                AccountTestTable.insert {
                    it[uuid] = t
                    it[username] = u.username
                    it[money] = u.money
                }
            }
        }
    }

    private fun getPlayerBalancesDollarsTable(): MutableMap<UUID, PlayerBalance> {

        transaction {
            AccountTestTable.selectAll().forEach { resultRow: ResultRow ->
                map[resultRow[AccountTestTable.uuid]] =
                    PlayerBalance(resultRow[AccountTestTable.id], resultRow[AccountTestTable.username], resultRow[AccountTestTable.money])
            }
        }

        return map
    }


    object LegacyAccountTableTest : Table("lite_eco") {
        private val id = integer("id").autoIncrement()
        val username = varchar("username", 36)
        val uuid = varchar("uuid", 36)
        val money = double("money")

        override val primaryKey: PrimaryKey = PrimaryKey(id)
    }

    object AccountTestTable : Table("lite_eco_dollars") {
        val id = integer("id").autoIncrement()
        val username = varchar("username", 36).nullable()
        val uuid = uuid("uuid")
        val money = decimal("money", 18, 9)

        override val primaryKey: PrimaryKey = PrimaryKey(id)
    }

}