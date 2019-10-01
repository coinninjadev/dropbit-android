package app.coinninja.cn.thunderdome.repository

import androidx.lifecycle.LiveData
import app.coinninja.cn.persistance.DropbitDatabase
import app.coinninja.cn.persistance.model.LightningAccount
import app.coinninja.cn.persistance.model.LightningInvoice
import app.coinninja.cn.thunderdome.client.ThunderDomeApiClient
import app.coinninja.cn.thunderdome.model.*
import app.dropbit.annotations.Mockable

@Mockable
class ThunderDomeRepository(
        internal val apiClient: ThunderDomeApiClient,
        internal val dropbitDatabase: DropbitDatabase
) {

    val lightningAccount: LightningAccount? get() = dropbitDatabase.lightningAccountDao().getAccount()
    val ledgerInvoices: LiveData<List<LightningInvoice>> get() = dropbitDatabase.lightningInvoiceDao().allVisibleLive()
    val isLocked: Boolean get() = lightningAccount?.isLocked ?: true

    fun sync() {
        syncAccount()
        syncLedger()
    }

    internal fun syncAccount() {
        val response = apiClient.account
        if (response.isSuccessful) {
            response.body()?.let { accountResponse ->
                dropbitDatabase.lightningAccountDao().insertOrUpdate(accountResponse.toLightningAccount())
            }
        }
    }

    internal fun syncLedger() {
        lightningAccount?.let { account ->
            val response = apiClient.ledger()
            if (response.isSuccessful) {
                response.body()?.let { ledgerResponse ->
                    ledgerResponse.invoices.forEach { ledgerInvoice ->
                        val ledger = ledgerInvoice.toLightningLedger()
                        ledger.accountId = account.id
                        dropbitDatabase.lightningInvoiceDao().insertOrUpdate(ledger)
                    }
                }
            }
        }
    }

    fun postWithdrawal(withdrawalRequest: WithdrawalRequest): Boolean {
        lightningAccount?.let { account ->
            val response = apiClient.withdraw(withdrawalRequest)
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    body.result.let { ledgerInvoice ->
                        val invoice = ledgerInvoice.toLightningLedger()
                        invoice.accountId = account.id
                        dropbitDatabase.lightningInvoiceDao().insertOrUpdate(invoice)
                        return true
                    }
                }
            }
        }

        return false

    }

    fun estimateWithdrawal(withdrawalRequest: WithdrawalRequest): LedgerInvoice? {
        lightningAccount?.let { account ->
            withdrawalRequest.isEstimate = true
            val response = apiClient.estimateWithdraw(withdrawalRequest)
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    body.result.let { ledgerInvoice ->
                        return ledgerInvoice
                    }
                }
            }
        }

        return null
    }

    fun createInvoiceFor(amount: Long, memo: String? = null): String? {
        apiClient.createInvoiceFor(amount, memo ?: "").let { response ->
            return if (response.isSuccessful) {
                response.body()?.request
            } else {
                null
            }
        }
    }

    fun decode(encodedInvoice: String): RequestInvoice? {
        apiClient.decode(DecodeRequest(encodedInvoice)).let { response ->
            return if (response.isSuccessful) {
                val response = response.body()
                response?.let {
                    response.encoded = encodedInvoice
                }
                return response
            } else {
                null
            }
        }
    }

    fun estimatePayment(encodedInvoice: String, amount: Long): LedgerInvoice? {
        return pay(encodedInvoice, amount, true)
    }

    fun pay(encodedInvoice: String, amount: Long, isEstimate: Boolean? = null): LedgerInvoice? {
        apiClient.pay(PaymentRequest(encodedInvoice, amount, isEstimate)).let { response ->
            return when (response.code()) {
                200 -> response.body()?.result
                400 -> {
                    LedgerInvoice()
                }
                else -> null

            }
        }
    }


}