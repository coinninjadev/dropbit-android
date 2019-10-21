package app.coinninja.cn.thunderdome.model

import app.dropbit.annotations.Mockable

@Mockable
data class CreateInvoiceResponse(
        val request: String? = null,
        var errorMessage: String? = null
)
