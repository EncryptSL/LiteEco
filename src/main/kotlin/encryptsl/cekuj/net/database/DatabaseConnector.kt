package encryptsl.cekuj.net.database

import com.zaxxer.hikari.HikariDataSource
import encryptsl.cekuj.net.api.interfaces.DatabaseConnectorProvider
import encryptsl.cekuj.net.database.tables.Account
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

class DatabaseConnector : DatabaseConnectorProvider {

    override fun initConnect(jdbcHost: String, user: String, pass: String) {
        val config = HikariDataSource().apply {
            maximumPoolSize = 10
            jdbcUrl = jdbcHost
            username = user
            password = pass
        }

        Database.connect(config)

        transaction {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(Account)
        }
    }
}