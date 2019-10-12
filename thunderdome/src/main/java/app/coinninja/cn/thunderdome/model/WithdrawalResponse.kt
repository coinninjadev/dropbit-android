package app.coinninja.cn.thunderdome.model

import app.dropbit.annotations.Mockable

@Mockable
data class WithdrawalResponse(val result: LedgerInvoice) {

}
