import com.github.encryptsl.lite.eco.common.extensions.getRandomString
import kotlin.random.Random
import kotlin.test.Test

class TopSortTest {

    @Test
    fun topSorting() {
        val tempList: HashMap<String, Int> = HashMap()
        for (i in 1..10) {
            tempList[getRandomString(15)] = Random.nextDouble(100.0, 10000.0).toInt()
        }

       val sorted = tempList.toList().sortedByDescending { (_, e) -> e }.toMap()

       for(o in sorted) {
           println("${o.key} : $${o.value}")
       }
    }
}