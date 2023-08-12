package encryptsl.cekuj.net.listeners

import encryptsl.cekuj.net.LiteEco
import encryptsl.cekuj.net.api.enums.OperationType
import encryptsl.cekuj.net.api.events.AccountEconomyManageEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class AccountEconomyManageListener(private val liteEco: LiteEco) : Listener {

    @EventHandler
    fun onEconomyManage(event: AccountEconomyManageEvent) {
        val player: Player = event.player

        when (event.operationType) {
            OperationType.CREATE_ACCOUNT -> { liteEco.api.createAccount(player, liteEco.config.getDouble("economy.starting_balance")) }
            OperationType.CACHING_ACCOUNT -> { liteEco.api.cacheAccount(player, liteEco.api.getBalance(player) )}
            OperationType.SYNC_ACCOUNT -> liteEco.api.syncAccount(player)
            OperationType.REMOVE_ACCOUNT -> liteEco.api.deleteAccount(player)
        }
    }
}