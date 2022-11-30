package encryptsl.cekuj.net.api.interfaces

interface DatabaseConnectorProvider {
    fun initConnect(jdbc_host: String, user: String, pass: String)
}