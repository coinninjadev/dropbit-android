package app.coinninja.cn.thunderdome.repository

import androidx.lifecycle.LiveData
import app.coinninja.cn.persistance.DropbitDatabase
import app.coinninja.cn.persistance.model.LightningAccount
import app.coinninja.cn.persistance.model.LightningInvoice
import app.coinninja.cn.thunderdome.client.ThunderDomeApiClient
import app.coinninja.cn.thunderdome.model.LedgerInvoice
import app.coinninja.cn.thunderdome.model.WithdrawalRequest
import app.dropbit.annotations.Mockable

@Mockable
class ThunderDomeRepository(
        internal val apiClient: ThunderDomeApiClient,
        internal val dropbitDatabase: DropbitDatabase
) {

    val lightningAccount: LightningAccount? get() = dropbitDatabase.lightningAccountDao().getAccount()
    val ledgerInvoices: LiveData<List<LightningInvoice>> get() = dropbitDatabase.lightningInvoiceDao().allVisibleLive()
    val isLocked:Boolean = true

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

    fun estimateWithdrawal(withdrawalRequest: WithdrawalRequest):LedgerInvoice? {
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


}