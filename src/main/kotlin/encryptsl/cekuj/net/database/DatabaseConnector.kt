package encryptsl.cekuj.net.database

import com.zaxxer.hikari.HikariDataSource
import encryptsl.cekuj.net.api.interfaces.DatabaseConnectorProvider
import java.sql.Connection

class DatabaseConnector : DatabaseConnectorProvider {

    private var hikari = HikariDataSource()

    override fun initConnect(jdbc_host: String, user: String, pass: String) {
        hikari = HikariDataSource()
        hikari.maximumPoolSize = 10
        hikari.jdbcUrl = jdbc_host
        hikari.username = user
        hikari.password = pass
    }

    override fun getDatabase(): Connection? {
        return hikari.connection
    }

}