package encryptsl.cekuj.net

import encryptsl.cekuj.net.listeners.*

class HandlerListeners(private val liteEco: LiteEco) {

    fun registerListener() {
        val start = System.currentTimeMillis()
        val listeners = listOf(
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
        listeners.forEach { listener ->
            liteEco.pluginManager.registerEvents(listener, liteEco)
            liteEco.logger.info("Bukkit Listener ${listener::class.simpleName} registered -> ok")
        }
        liteEco.logger.info("Listeners registered(${listeners.size}) in time ${System.currentTimeMillis() - start} ms -> ok")
    }
}