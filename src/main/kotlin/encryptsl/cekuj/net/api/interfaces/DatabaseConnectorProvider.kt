package encryptsl.cekuj.net.api.interfaces

import java.sql.Connection

interface DatabaseConnectorProvider {
    fun initConnect(jdbc_host: String, user: String, pass: String)
    fun getDatabase(): Connection?
}