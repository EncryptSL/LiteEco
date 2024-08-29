package com.github.encryptsl.lite.eco.api.events

import com.github.encryptsl.lite.eco.api.enums.OperationType
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import java.util.UUID

@Suppress("UNUSED")
class AccountManageEvent(val uuid: UUID, val operationType: OperationType) : Event(), Cancellable {

    private var isCancelled: Boolean = false

    override fun getHandlers(): HandlerList {
        return HANDLERS
    }

    override fun isCancelled(): Boolean {
        return isCancelled
    }

    override fun setCancelled(cancel: Boolean) {
        this.isCancelled = cancel
    }

    companion object {
        private val HANDLERS = HandlerList()
        @JvmStatic
        fun getHandlerList(): HandlerList {
            return HANDLERS
        }
    }
}