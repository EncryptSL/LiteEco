package com.github.encryptsl.lite.eco.common.hook.treasury.wrapper

import me.lokka30.treasury.api.economy.account.PlayerAccount
import java.util.UUID

class TreasuryPlayerAccount(private val uuid: UUID) : TreasuryAccount(uuid), PlayerAccount {
    override fun identifier(): UUID {
        return uuid
    }
}