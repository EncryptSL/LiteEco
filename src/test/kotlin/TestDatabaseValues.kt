import org.junit.jupiter.api.Test
import java.io.File
import java.net.URL
import java.util.*
import java.util.stream.Collectors
import kotlin.random.Random

class TestDatabaseValues {

    private val databaseConnector: DatabaseConnectorTest by lazy { DatabaseConnectorTest() }
    private val preparedStatementsTest: PreparedStatementsTest by lazy { PreparedStatementsTest() }
    @Test
    fun runTest() {
        val resource: URL = this::class.java.getResource("database.db") as URL
        val path: String = File(resource.toURI()).absolutePath
        databaseConnector.initConnect("jdbc:sqlite:%s".format(path), "root", "root")

        runData()
        println(preparedStatementsTest.getExistPlayerAccount(offlineUUID("Alex")))
    }

    private fun runData() {
        preparedStatementsTest.createPlayerAccount(offlineUUID("EncryptSL"), Random.nextInt(10000, 256500).toDouble())
        //preparedStatementsTest.createPlayerAccount(offlineUUID("Alex"), Random.nextInt(10000, 256500).toDouble())
    }

    private fun offlineUUID(username: String): UUID
    {
        return UUID.nameUUIDFromBytes("OfflinePlayer:$username".toByteArray())
    }

    private fun runList(): LinkedHashMap<String, Double>? {
        return preparedStatementsTest.getTopBalance(100)
            .entries
            .stream()
            .sorted(compareByDescending { o1 -> o1.value })
            .collect(
                Collectors.toMap({ e -> e.key }, { e -> e.value }, { _, e2 -> e2 }) { LinkedHashMap() })
    }
}