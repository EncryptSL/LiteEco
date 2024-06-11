package com.github.encryptsl.lite.eco.api.interfaces

import com.github.encryptsl.lite.eco.common.database.entity.User
import java.util.*
import java.util.concurrent.CompletableFuture

interface PlayerSQL {
    fun createPlayerAccount(username: String, uuid: UUID, money: Double)
    fun getUserByUUID(uuid: UUID): CompletableFuture<User>
    fun deletePlayerAccount(uuid: UUID)
    fun getExistPlayerAccount(uuid: UUID): CompletableFuture<Boolean>
    fun getTopBalance(): MutableMap<String, Double>
    fun getPlayersIds(): CompletableFuture<MutableCollection<UUID>>
    fun depositMoney(uuid: UUID, money: Double)
    fun withdrawMoney(uuid: UUID, money: Double)
    fun setMoney(uuid: UUID, money: Double)
    fun purgeAccounts()
    fun purgeDefaultAccounts(defaultMoney: Double)
    fun purgeInvalidAccounts()
}