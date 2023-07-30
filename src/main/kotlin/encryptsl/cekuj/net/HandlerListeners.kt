package encryptsl.cekuj.net

import encryptsl.cekuj.net.listeners.*
import kotlin.system.measureTimeMillis

class HandlerListeners(private val liteEco: LiteEco) {

    fun registerListener() {
        val listeners = arrayListOf(
            AccountEconomyManageListener(liteEco),
            PlayerEconomyPayListener(liteEco),
            AdminEconomyGlobalDepositListener(liteEco),
            AdminEconomyGlobalSetListener(liteEco),
            AdminEconomyGlobalWithdrawListener(liteEco),
            AdminEconomyMoneyDepositListener(liteEco),
            AdminEconomyMoneyWithdrawListener(liteEco),
            AdminEconomyMoneySetListener(liteEco),
            PlayerJoinListener(liteEco)
        )
        val timeTaken = measureTimeMillis {
            listeners.forEach { listener -> liteEco.pluginManager.registerEvents(listener, liteEco)
                liteEco.logger.info("Bukkit Listener ${listener.javaClass.simpleName} registered () -> ok")
            }
        }
        liteEco.logger.info("Listeners registered(${listeners.size}) in time $timeTaken ms -> ok")
    }
}