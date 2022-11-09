import java.util.*

interface DBSQLProvider {
    fun createTable(mode: String)
    fun createPlayerAccount(uuid: UUID, money: Double)
    fun deletePlayerAccount(uuid: UUID)
    fun getExistPlayerAccount(uuid: UUID): Boolean
    fun getTopBalance(top: Int): MutableMap<String, Double>
    fun getBalance(uuid: UUID): Double
    fun depositMoney(uuid: UUID, money: Double)
    fun withdrawMoney(uuid: UUID, money: Double)
    fun setMoney(uuid: UUID, money: Double)
}