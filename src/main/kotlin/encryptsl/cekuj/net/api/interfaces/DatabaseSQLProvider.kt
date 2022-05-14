package encryptsl.cekuj.net.api.interfaces

import java.util.*
import kotlin.collections.HashMap

interface DatabaseSQLProvider {
    fun createTable(mode: String)
    fun createPlayerAccount(uuid: UUID, money: Double)
    fun deletePlayerAccount(uuid: UUID)
    fun getExistPlayerAccount(uuid: UUID): Boolean
    fun getTopBalance(top: Int): HashMap<String, Double>
    fun getBalance(uuid: UUID): Double
    fun depositMoney(uuid: UUID, money: Double)
    fun withdrawMoney(uuid: UUID, money: Double)
    fun setMoney(uuid: UUID, money: Double)
}