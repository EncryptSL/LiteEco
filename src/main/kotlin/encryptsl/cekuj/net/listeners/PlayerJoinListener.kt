package encryptsl.cekuj.net.listeners

import encryptsl.cekuj.net.LiteEco
import encryptsl.cekuj.net.api.enums.OperationType
import encryptsl.cekuj.net.api.events.AccountEconomyManageEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PlayerJoinListener(private val liteEco: LiteEco) : Listener {

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val player: Player = event.player
        if (!player.hasPlayedBefore()) {
            liteEco.pluginManger.callEvent(AccountEconomyManageEvent(player, OperationType.CREATE_ACCOUNT))
        }
    }
}