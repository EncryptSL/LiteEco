package encryptsl.cekuj.net.api.interfaces

import java.util.*

interface AccountAPI {
    fun cacheAccount(uuid: UUID, value: Double)

    fun syncAccount(uuid: UUID)

    fun syncAccounts()

    fun removeAccount(uuid: UUID)

    fun isAccountCached(uuid: UUID): Boolean

    fun isPlayerOnline(uuid: UUID): Boolean

    fun getAccount(): MutableMap<UUID, Double>
}