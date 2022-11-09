import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection

class DatabaseConnectorTest {

    private var hikari = HikariDataSource()

    fun initConnect(jdbc_host: String, user: String, pass: String) {
        hikari = HikariDataSource()
        hikari.driverClassName = "org.sqlite.JDBC"
        hikari.maximumPoolSize = 10
        hikari.jdbcUrl = jdbc_host
        hikari.username = user
        hikari.password = pass
    }

    fun getDatabase(): Connection? {
        return hikari.connection
    }
}