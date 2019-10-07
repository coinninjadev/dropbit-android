package app.coinninja.cn.thunderdome.repository

import androidx.lifecycle.LiveData
import app.coinninja.cn.persistance.DropbitDatabase
import app.coinninja.cn.persistance.model.*
import app.coinninja.cn.thunderdome.client.ThunderDomeApiClient
import app.coinninja.cn.thunderdome.model.*
import app.dropbit.annotations.Mockable
import com.google.gson.Gson
import java.util.*

@Mockable
class ThunderDomeRepository(
        internal val apiClient: ThunderDomeApiClient,
        internal val dropbitDatabase: DropbitDatabase
) {

    val availableBalance: Long get() = dropbitDatabase.lightningAccountDao().availableBalance()
    val lightningAccount: LightningAccount? get() = dropbitDatabase.lightningAccountDao().getAccount()
    val visibleSettlements: LiveData<List<LedgerSettlementDetail>> get() = dropbitDatabase.ledgerSettlementDao.allVisibleLive()
    val isLocked: Boolean get() = lightningAccount?.isLocked ?: true
    val withdrawsFromAccount: Array<LightningInvoice>
        get() =
            dropbitDatabase.lightningInvoiceDao().allByDirectionAndType(LedgerDirection.OUT.id, LedgerType.BTC.id)

    fun sync() {
        syncAccount()
        syncLedger()
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
            return when (response.code()) {
                200 -> {
                    response.body()?.let { body ->
                        body.result.let { ledgerInvoice ->
                            return ledgerInvoice
                        }
                    }
                }
                400 -> {
                    response.errorBody()?.let { body ->
                        try {
                            val error = Gson().fromJson<PayErrorResponse>(body.string(), PayErrorResponse::class.java)
                            LedgerInvoice(error = error.code)
                        } catch (e: Exception) {
                            null
                        }
                    }
                }
                else -> null
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

    fun pay(encodedInvoice: String, amount: Long, isEstimate: Boolean? = null, inviteId: Long? = null): LedgerInvoice? {
        val invoice = apiClient.pay(PaymentRequest(encodedInvoice, amount, isEstimate)).let { response ->
            when (response.code()) {
                200 -> response.body()?.result
                400 -> {
                    response.errorBody()?.let { body ->
                        try {
                            val error = Gson().fromJson<PayErrorResponse>(body.string(), PayErrorResponse::class.java)
                            LedgerInvoice(error = error.message)
                        } catch (e: Exception) {
                            LedgerInvoice()
                        }
                    }
                }
                else -> LedgerInvoice()

            }
        }

        invoice?.let {
            if (it.value > 0L) {
                dropbitDatabase.lightningInvoiceDao().insertOrUpdate(it.toLightningLedger())
                dropbitDatabase.lightningInvoiceDao().ledgerByServerId(it.id)?.let { savedInvoice ->
                    if (inviteId != null) {
                        dropbitDatabase.ledgerSettlementDao.addInvoiceToInvite(inviteId, savedInvoice.id)
                    } else {
                        dropbitDatabase.ledgerSettlementDao.createSettlementFor(savedInvoice)
                    }
                }
            }
        }

        return invoice
    }

    internal fun syncAccount() {
        val response = apiClient.account
        if (response.isSuccessful) {
            response.body()?.let { accountResponse ->
                dropbitDatabase.lightningAccountDao().insertOrUpdate(accountResponse.toLightningAccount())
            }
        }
    }

    internal fun saveLedgerInvoice(ledgerInvoice: LedgerInvoice, account: LightningAccount) {
        val lightningInvoiceDao = dropbitDatabase.lightningInvoiceDao()
        val ledger = ledgerInvoice.toLightningLedger()
        ledger.accountId = account.id
        lightningInvoiceDao.insertOrUpdate(ledger)
        dropbitDatabase.ledgerSettlementDao.createSettlementFor(lightningInvoiceDao.ledgerByServerId(ledger.serverId))
    }

    internal fun syncLedger() {
        lightningAccount?.let { account ->
            val response = apiClient.ledger()
            if (response.isSuccessful) {
                response.body()?.let { ledgerResponse ->
                    ledgerResponse.invoices.forEach { ledgerInvoice ->
                        saveLedgerInvoice(ledgerInvoice, account)
                    }
                }
            }
        }
    }

    fun createSettlementForInvite(inviteId: Long, toUserId: Long, fromUserId: Long, createdMillis: Long) {
        dropbitDatabase.ledgerSettlementDao.createSettlementForInvite(
                inviteId,
                toUserId,
                fromUserId,
                Date(createdMillis)
        )
    }

}