package app.coinninja.cn.thunderdome.model

import app.coinninja.cn.persistance.model.LightningAccount
import app.dropbit.annotations.Mockable
import app.dropbit.commons.currency.BTCCurrency
import com.google.gson.annotations.SerializedName

@Mockable
data class AccountResponse(
        val id: String,
        @SerializedName("created_at")
        val createdAt: String = "",
        @SerializedName("updated_at")
        val updatedAt: String = "",
        val address: String = "",
        val balance: Long = 0,
        @SerializedName("pending_in")
        val pendingIn: Long = 0,
        @SerializedName("pending_out")
        val pendingOut: Long = 0,
        @SerializedName("locked")
        val isLocked: Boolean = true
) {


    fun toLightningAccount(): LightningAccount {
        return LightningAccount(
                serverId = id,
                createdAt = createdAt,
                updatedAt = updatedAt,
                address = address,
                balance = BTCCurrency(balance),
                pendingIn = BTCCurrency(pendingIn),
                pendingOut = BTCCurrency(pendingOut),
                isLocked = isLocked
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AccountResponse

        if (id != other.id) return false
        if (createdAt != other.createdAt) return false
        if (updatedAt != other.updatedAt) return false
        if (address != other.address) return false
        if (balance != other.balance) return false
        if (pendingIn != other.pendingIn) return false
        if (pendingOut != other.pendingOut) return false
        if (isLocked != other.isLocked) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + updatedAt.hashCode()
        result = 31 * result + address.hashCode()
        result = 31 * result + balance.hashCode()
        result = 31 * result + pendingIn.hashCode()
        result = 31 * result + pendingOut.hashCode()
        result = 31 * result + isLocked.hashCode()
        return result
    }
}
