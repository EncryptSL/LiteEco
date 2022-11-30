import com.zaxxer.hikari.HikariDataSource
import encryptsl.cekuj.net.database.tables.Money
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

class DatabaseConnectorTest {

    fun initConnect(jdbc_host: String, user: String, pass: String) {

        val config = HikariDataSource().apply {
            maximumPoolSize = 10
            jdbcUrl = jdbc_host
            username = user
            password = pass
        }

        Database.connect(config)

        transaction {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(Money)
        }

    }
}