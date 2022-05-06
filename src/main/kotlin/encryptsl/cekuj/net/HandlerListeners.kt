package encryptsl.cekuj.net

import encryptsl.cekuj.net.listeners.*
import org.bukkit.event.Listener

class HandlerListeners(private val liteEco: LiteEco) {

    fun registerListener() {
        val start = System.currentTimeMillis()
        val list: List<Listener> = arrayListOf(
            AccountEconomyManageListener(liteEco),
            PlayerEconomyPayListener(liteEco),
            AdminEconomyTransactionListener(liteEco),
            ConsoleEconomyTransactionListener(liteEco),
            PlayerJoinListener(liteEco)
        )
        list.forEach { listener -> liteEco.pluginManger.registerEvents(listener, liteEco)
           liteEco.logger.info("Bukkit Listener ${listener.javaClass.simpleName} registered () -> ok")
        }
        liteEco.logger.info("Listeners registered(${list.size}) in time ${System.currentTimeMillis() - start} ms -> ok")
    }
}