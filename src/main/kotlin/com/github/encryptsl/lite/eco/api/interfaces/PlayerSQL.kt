package com.github.encryptsl.lite.eco.api.interfaces

import com.github.encryptsl.lite.eco.common.database.entity.User
import java.math.BigDecimal
import java.util.*

interface PlayerSQL {
    fun createPlayerAccount(username: String, uuid: UUID, currency: String, money: BigDecimal)
    fun updatePlayerName(uuid: UUID, username: String, currency: String)
    fun getUserByUUID(uuid: UUID, currency: String): User?
    fun getBalance(uuid: UUID, currency: String): BigDecimal
    fun deletePlayerAccount(uuid: UUID, currency: String)
    fun getExistPlayerAccount(uuid: UUID, currency: String): Boolean
    fun getTopBalance(currency: String): MutableMap<String, User>
    fun getUUIDNameMap(currency: String): MutableMap<UUID, String>
    fun getPlayersIds(currency: String): MutableCollection<UUID>
    fun deposit(uuid: UUID, currency: String, money: BigDecimal)
    fun withdraw(uuid: UUID, currency: String, money: BigDecimal)
    fun set(uuid: UUID, currency: String, money: BigDecimal)
    fun purgeAccounts(currency: String)
    fun purgeDefaultAccounts(defaultMoney: BigDecimal, currency: String)
    fun purgeInvalidAccounts(currency: String)
}