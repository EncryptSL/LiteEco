import org.junit.jupiter.api.Test
import java.io.File
import java.net.URL
import java.util.*
import java.util.stream.Collectors
import kotlin.random.Random

class TestDatabaseValues {

    val databaseConnector: DatabaseConnectorTest by lazy { DatabaseConnectorTest() }
    private val preparedStatementsTest: PreparedStatementsTest by lazy { PreparedStatementsTest(this) }
    @Test
    fun runTest() {
        val resource: URL = this::class.java.getResource("database.db") as URL
        val path: String = File(resource.toURI()).absolutePath
        databaseConnector.initConnect("jdbc:sqlite:%s".format(path), "root", "root")
        preparedStatementsTest.createTable("jdbc:sqlite")
        runData()
        var a = 0
        runList()?.forEach { (it, i) -> println("${++a} $it - $i") }
    }

    private fun runData() {
        for (i in 1..100) {
            preparedStatementsTest.createPlayerAccount(UUID.randomUUID(), Random.nextInt(10000, 256500).toDouble())
        }
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