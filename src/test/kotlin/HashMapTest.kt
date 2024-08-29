import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.*

class HashMapTest {

    private val currencies = listOf("dollars", "credits")
    private val cache = mutableMapOf<UUID, MutableMap<String, BigDecimal>>()

    @Test
    fun test() {
        val table = mutableMapOf(
            "dollars" to UserAccount(UUID.fromString("192a0ce2-0f1f-3c7f-a6af-7023c1bd79f3"), BigDecimal.valueOf(100)),
            "credits" to UserAccount(UUID.fromString("192a0ce2-0f1f-3c7f-a6af-7023c1bd79f3"), BigDecimal.valueOf(30))
        )
        for ((currency, account) in table) {
            cacheAccount(account.uuid, currency, account.balance)
        }

        println(cache)
    }

    private fun cacheAccount(uuid: UUID, currency: String, balance: BigDecimal) {
        //cache.computeIfAbsent(uuid) { mutableMapOf() } [currency] = balance
        //cache[uuid]?.computeIfPresent(currency) { _, _ -> balance }

        if (!isAccountCached(uuid, currency)) {
            cache.computeIfAbsent(uuid) { mutableMapOf() } [currency] = balance
        } else {
            //cache[uuid]?.computeIfPresent(currency) { _, _ -> value }
        }
    }

    //private fun cacheAccount(uuid: UUID, currency: String, value: BigDecimal) {
    //    if (!isAccountCached(uuid)) {
    //        cache[uuid] = mutableMapOf(currency to value)
    //    } else {
    //        cache[uuid]?.computeIfPresent(currency) { _, _ -> value }
    //    }
    //}

    private fun isAccountCached(uuid: UUID, currency: String): Boolean {
        return cache.containsKey(uuid) && cache.get(uuid)?.containsKey(currency) == true
    }

    data class UserAccount(val uuid: UUID, val balance: BigDecimal)

}