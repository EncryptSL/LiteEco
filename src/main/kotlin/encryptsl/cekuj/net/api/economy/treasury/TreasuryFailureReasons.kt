package encryptsl.cekuj.net.api.economy.treasury

import me.lokka30.treasury.api.common.response.FailureReason

enum class TreasuryFailureReasons(private val description: String) : FailureReason {

    INVALID_VALUE("Invalid value inputted!"),
    INVALID_CURRENCY("Invalid currency inputted!");
    override fun getDescription(): String {
        return description
    }
}