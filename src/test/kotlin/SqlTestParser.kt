import encryptsl.cekuj.net.extensions.positionIndexed
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.random.Random

class SqlTestParser {

    @Test
    fun onParser() {
        val tempList: HashMap<UUID, Double> = HashMap()
        for (i in 1..10) {
            tempList[UUID.randomUUID()] = Random.nextDouble(100.0, 999.0)
        }
        val sqlData = tempList.toList().positionIndexed { index, entry ->  MigrationDataTest(index, entry.first.toString(), entry.second)}

        println("DROP TABLE IF EXIST lite_eco;")
        println("INSERT INTO lite_eco (id, uuid, money) VALUES " + sqlData.joinToString {"\n(${it.id} ${it.uuid} ${it.money})" } + ";")
    }

}

data class MigrationDataTest(val id: Int, val uuid: String, val money: Double)