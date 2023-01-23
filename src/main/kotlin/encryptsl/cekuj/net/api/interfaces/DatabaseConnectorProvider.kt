package encryptsl.cekuj.net.api.interfaces

interface DatabaseConnectorProvider {
    fun initConnect(jdbcHost: String, user: String, pass: String)
}