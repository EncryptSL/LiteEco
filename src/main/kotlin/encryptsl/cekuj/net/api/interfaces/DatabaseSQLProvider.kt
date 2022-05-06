package encryptsl.cekuj.net.api.interfaces

import java.util.*

interface DatabaseSQLProvider {
    fun createTable()
    fun createPlayerAccount(uuid: UUID, money: Double)
    fun deletePlayerAccount(uuid: UUID)
    fun getExistPlayerAccount(uuid: UUID): Boolean
    fun getBalance(uuid: UUID): Double
    fun depositMoney(uuid: UUID, money: Double)
    fun withdrawMoney(uuid: UUID, money: Double)
    fun setMoney(uuid: UUID, money: Double)
}