package encryptsl.cekuj.net.listeners

import encryptsl.cekuj.net.LiteEco
import encryptsl.cekuj.net.api.enums.OperationType
import encryptsl.cekuj.net.api.events.AccountManageEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PlayerJoinListener(private val liteEco: LiteEco) : Listener {

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val player: Player = event.player
        liteEco.pluginManager.callEvent(AccountManageEvent(player, OperationType.CREATE_ACCOUNT))
        liteEco.pluginManager.callEvent(AccountManageEvent(player, OperationType.CACHING_ACCOUNT))
    }
}