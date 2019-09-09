package app.coinninja.cn.thunderdome.model

import app.dropbit.annotations.Mockable
import com.google.gson.annotations.SerializedName

@Mockable
data class LedgerResponse(
        @SerializedName("ledger")
        val invoices: List<LedgerInvoice> = mutableListOf()
)
