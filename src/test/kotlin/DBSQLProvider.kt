import java.util.*

interface DBSQLProvider {
    fun createPlayerAccount(uuid: UUID, money: Double)
    fun deletePlayerAccount(uuid: UUID)
    fun getExistPlayerAccount(uuid: UUID): Boolean
    fun getTopBalance(top: Int): MutableMap<String, Double>

    fun getTopBalance(): MutableMap<String, Double>

    fun getBalance(uuid: UUID): Double
    fun depositMoney(uuid: UUID, money: Double)
    fun withdrawMoney(uuid: UUID, money: Double)
    fun setMoney(uuid: UUID, money: Double)
}