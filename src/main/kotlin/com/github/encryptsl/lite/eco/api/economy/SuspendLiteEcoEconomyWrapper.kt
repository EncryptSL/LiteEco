package com.github.encryptsl.lite.eco.api.economy

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.PlayerAccount
import com.github.encryptsl.lite.eco.common.database.entity.User
import kotlinx.coroutines.future.await
import org.bukkit.OfflinePlayer
import java.math.BigDecimal
import java.util.Optional
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

class SuspendLiteEcoEconomyWrapper {

    private val economyImpl: ModernLiteEcoEconomyImpl = ModernLiteEcoEconomyImpl()

    suspend fun getUserByUUID(uuid: UUID, currency: String = "dollars"): Optional<User> {
        return economyImpl.getUserByUUID(uuid, currency).await()
    }

    suspend fun createAccount(player: OfflinePlayer, currency: String = "dollars", startAmount: BigDecimal): Boolean {
        val userOpt = getUserByUUID(player.uniqueId, currency)
        return if (!userOpt.isPresent) {
            LiteEco.instance.databaseEcoModel.createPlayerAccount(player.name.toString(), player.uniqueId, currency, startAmount)
            true
        } else {
            LiteEco.instance.databaseEcoModel.updatePlayerName(player.uniqueId, player.name.toString(), currency)
            false
        }
    }

    suspend fun deleteAccount(uuid: UUID, currency: String = "dollars"): Boolean {
        val user = getUserByUUID(uuid, currency).getOrNull()
        return if (user == null) {
            false
        } else {
            PlayerAccount.clearFromCache(uuid)
            LiteEco.instance.databaseEcoModel.deletePlayerAccount(uuid, currency)
            true
        }
    }

    suspend fun hasAccount(uuid: UUID, currency: String = "dollars"): Boolean {
        return economyImpl.hasAccount(uuid, currency).await()
    }

    suspend fun getBalance(uuid: UUID, currency: String): BigDecimal {
        return economyImpl.getBalance(uuid, currency)
    }
}