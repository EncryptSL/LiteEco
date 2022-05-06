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

        if (event.operationType == OperationType.CREATE_ACCOUNT) {
            liteEco.econ?.createPlayerAccount(player)
        }

        if (event.operationType == OperationType.REMOVE_ACCOUNT) {
            liteEco.preparedStatements.deletePlayerAccount(player.uniqueId)
        }
    }
}