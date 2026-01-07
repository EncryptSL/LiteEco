package com.github.encryptsl.lite.eco.common.hook.economy.playerpoints

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.common.hook.HookListener
import com.github.encryptsl.lite.eco.utils.ClassUtil
import org.black_ixx.playerpoints.PlayerPoints
import org.black_ixx.playerpoints.PlayerPointsAPI
import java.util.*

class PlayerPointsHook(
    private val liteEco: LiteEco
) : HookListener(
    PLUGIN_NAME,
    "You can now export economy from plugin PlayerPoints to LiteEco with /eco database import PlayerPoints <your_currency>"
) {
    private var playerPointsAPI: PlayerPointsAPI? = null

    companion object {
        const val PLUGIN_NAME = "PlayerPoints"
        fun isPlayerPointsPresent(): Boolean
                = ClassUtil.isValidClasspath("org.black_ixx.playerpoints.PlayerPoints")
    }

    override fun canRegister(): Boolean {
        val plugin = liteEco.pluginManager.getPlugin(PLUGIN_NAME)
        return !registered && plugin != null && isPlayerPointsPresent()
    }

    override fun register() {
        if (!isPlayerPointsPresent()) {
            registered = true
        }
    }

    override fun unregister() {}

    fun getBalance(uuid: UUID): Int {
        return if (isPlayerPointsPresent()) {
            playerPointsAPI = PlayerPoints.getInstance().api
            playerPointsAPI?.look(uuid) ?: 0
        } else 0
    }
}