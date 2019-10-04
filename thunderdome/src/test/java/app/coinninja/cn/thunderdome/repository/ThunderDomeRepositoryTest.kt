package app.coinninja.cn.thunderdome.repository

import androidx.lifecycle.LiveData
import app.coinninja.cn.persistance.dao.LightningAccountDao
import app.coinninja.cn.persistance.dao.LightningInvoiceDao
import app.coinninja.cn.persistance.model.LightningAccount
import app.coinninja.cn.persistance.model.LightningInvoice
import app.coinninja.cn.thunderdome.client.Testdata
import app.coinninja.cn.thunderdome.model.*
import app.dropbit.commons.currency.BTCCurrency
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.nhaarman.mockitokotlin2.*
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.Test
import retrofit2.Response

class ThunderDomeRepositoryTest {

    @Test
    fun syncs_account_fetches_then_caches() {
        val repository = createRepository()
        val accountResponse: AccountResponse = mock()
        val response: Response<AccountResponse> = Response.success(200, accountResponse)
        val lightningAccount: LightningAccount = mock()
        val lightningAccountDao: LightningAccountDao = mock()
        whenever(repository.apiClient.account).thenReturn(response)
        whenever(repository.dropbitDatabase.lightningAccountDao()).thenReturn(lightningAccountDao)
        whenever(accountResponse.toLightningAccount()).thenReturn(lightningAccount)

        repository.syncAccount()

        verify(lightningAccountDao).insertOrUpdate(lightningAccount)
    }

    @Test
    fun provides_access_to_lightning_account() {
        val repository = createRepository()
        val lightningAccount: LightningAccount = mock()
        val lightningAccountDao: LightningAccountDao = mock()
        whenever(repository.dropbitDatabase.lightningAccountDao()).thenReturn(lightningAccountDao)
        whenever(repository.dropbitDatabase.lightningAccountDao().getAccount()).thenReturn(lightningAccount)

        assertThat(repository.lightningAccount).isEqualTo(lightningAccount)
    }

    @Test
    fun syncsLedger_then_caches() {
        val repository = createRepository()
        val ledgerResponse: LedgerResponse = Gson().fromJson(Testdata.invoices, LedgerResponse::class.java)
        val ledgerApiResponse = Response.success(200, ledgerResponse)
        whenever(repository.apiClient.ledger()).thenReturn(ledgerApiResponse)
        val accountDao: LightningAccountDao = mock()
        val invoiceDao: LightningInvoiceDao = mock()
        val account: LightningAccount = mock()
        whenever(repository.dropbitDatabase.lightningAccountDao()).thenReturn(accountDao)
        whenever(repository.dropbitDatabase.lightningInvoiceDao()).thenReturn(invoiceDao)
        whenever(accountDao.getAccount()).thenReturn(account)
        whenever(account.id).thenReturn(1)

        repository.syncLedger()

        verify(invoiceDao, times(2)).insertOrUpdate(any())
    }

    @Test
    fun syncs_in_order() {
        val repository = createRepository()
        val ordered = inOrder(repository.apiClient)
        val accountResponse = mock<AccountResponse>()
        whenever(repository.dropbitDatabase.lightningAccountDao()).thenReturn(mock())
        whenever(accountResponse.toLightningAccount()).thenReturn(mock())
        whenever(repository.apiClient.account).thenReturn(Response.success(200, accountResponse))
        whenever(repository.apiClient.ledger()).thenReturn(Response.success(200, mock<LedgerResponse>()))
        val accountDao: LightningAccountDao = mock()
        val invoiceDao: LightningInvoiceDao = mock()
        val account: LightningAccount = mock()
        whenever(repository.dropbitDatabase.lightningAccountDao()).thenReturn(accountDao)
        whenever(repository.dropbitDatabase.lightningInvoiceDao()).thenReturn(invoiceDao)
        whenever(accountDao.getAccount()).thenReturn(account)
        whenever(account.id).thenReturn(1)

        repository.sync()

        ordered.verify(repository.apiClient).account
        ordered.verify(repository.apiClient).ledger()
    }

    @Test
    fun provides_access_to_ledger_data() {
        val repository = createRepository()
        val invoiceDao: LightningInvoiceDao = mock()
        val liveData: LiveData<List<LightningInvoice>> = mock()
        whenever(repository.dropbitDatabase.lightningInvoiceDao()).thenReturn(invoiceDao)
        whenever(invoiceDao.allVisibleLive()).thenReturn(liveData)

        assertThat(repository.ledgerInvoices).isEqualTo(liveData)
    }

    @Test
    fun posts_withdrawal_request() {
        val repository = createRepository()
        val withdrawalRequest = WithdrawalRequest(
                BTCCurrency(10_000),
                BTCCurrency(500),
                BTCCurrency(50),
                "--address--"
        )

        val withdrawalResponse = Gson().fromJson<WithdrawalResponse>(Testdata.withdrawalRequest, WithdrawalResponse::class.java)
        val ledgerItem = withdrawalResponse.result.toLightningLedger()
        ledgerItem.accountId = 1
        val response = Response.success(withdrawalResponse)
        val account = LightningAccount(1)
        whenever(repository.apiClient.withdraw(withdrawalRequest)).thenReturn(response)
        val accountDao: LightningAccountDao = mock()
        val invoiceDao: LightningInvoiceDao = mock()
        whenever(repository.dropbitDatabase.lightningAccountDao()).thenReturn(accountDao)
        whenever(repository.dropbitDatabase.lightningInvoiceDao()).thenReturn(invoiceDao)
        whenever(repository.dropbitDatabase.lightningAccountDao().getAccount()).thenReturn(account)

        assertThat(withdrawalRequest.isEstimate).isFalse()
        assertThat(repository.postWithdrawal(withdrawalRequest)).isTrue()
        verify(repository.dropbitDatabase.lightningInvoiceDao()).insertOrUpdate(ledgerItem)
    }

    @Test
    fun posts_withdrawal_request_for_estimate() {
        val repository = createRepository()
        val withdrawalRequest = WithdrawalRequest(
                BTCCurrency(10_000),
                BTCCurrency(500),
                BTCCurrency(50),
                "--address--"
        )

        val withdrawalResponse = Gson().fromJson<WithdrawalResponse>(Testdata.withdrawalRequest, WithdrawalResponse::class.java)
        val ledgerItem = withdrawalResponse.result.toLightningLedger()
        ledgerItem.accountId = 1
        val response = Response.success(withdrawalResponse)
        val account = LightningAccount(1)
        whenever(repository.apiClient.estimateWithdraw(any())).thenReturn(response)
        val accountDao: LightningAccountDao = mock()
        val invoiceDao: LightningInvoiceDao = mock()
        whenever(repository.dropbitDatabase.lightningAccountDao()).thenReturn(accountDao)
        whenever(repository.dropbitDatabase.lightningInvoiceDao()).thenReturn(invoiceDao)
        whenever(repository.dropbitDatabase.lightningAccountDao().getAccount()).thenReturn(account)

        assertThat(repository.estimateWithdrawal(withdrawalRequest)).isEqualTo(withdrawalResponse.result)
        assertThat(withdrawalRequest.isEstimate).isTrue()
        verifyZeroInteractions(repository.dropbitDatabase.lightningInvoiceDao())
    }

    @Test
    fun provides_visibility_on_locked_lightning_account() {
        val repository = createRepository()
        whenever(repository.dropbitDatabase.lightningAccountDao()).thenReturn(mock())
        whenever(repository.dropbitDatabase.lightningAccountDao().getAccount()).thenReturn(null).thenReturn(LightningAccount()).thenReturn(LightningAccount(isLocked = false))

        // true when no account
        assertThat(repository.isLocked).isTrue()

        // true when locked
        assertThat(repository.isLocked).isTrue()

        // false when not locked
        assertThat(repository.isLocked).isFalse()
    }

    @Test
    fun creates_lad_invoice_for_payment_requests() {
        val expectedResponse = "ln--invoice-id--"
        val repository = createRepository()
        whenever(repository.apiClient.createInvoiceFor(150_000, "--memo--")).thenReturn(Response.success(CreateInvoiceResponse(expectedResponse)))

        assertThat(repository.createInvoiceFor(150_000, "--memo--")).isEqualTo(expectedResponse)
    }

    @Test
    fun decodes_an_invoice_request() {
        val encodedInvoice = "ln--encoded-invoice"
        val repository = createRepository()
        val decodeRequest = DecodeRequest(encodedInvoice)
        val requestInvoice = RequestInvoice(
                "--address--",
                "--payment-hash--",
                100_000,
                "2019-08-22T22:34:46.481530Z",
                "2019-08-22T22:34:46.481530Z",
                "--description--",
                "--desc--hash--",
                "--fallback--",
                "2019-08-22T22:34:46.481530Z"
        )
        val expected = RequestInvoice(
                "--address--",
                "--payment-hash--",
                100_000,
                "2019-08-22T22:34:46.481530Z",
                "2019-08-22T22:34:46.481530Z",
                "--description--",
                "--desc--hash--",
                "--fallback--",
                "2019-08-22T22:34:46.481530Z"
        )
        expected.encoded = encodedInvoice
        whenever(repository.apiClient.decode(decodeRequest)).thenReturn(Response.success(requestInvoice))

        assertThat(repository.decode(encodedInvoice)).isEqualTo(expected)
    }

    @Test
    fun processes_a_payment_request__estimate() {
        val encodedInvoice = "ln--encoded-invoice"
        val repository = createRepository()
        val request = PaymentRequest(encodedInvoice, 10_000, true)
        val expectedPaymentResponse = PaymentResponse(result = LedgerInvoice())
        val response = Response.success(expectedPaymentResponse)
        whenever(repository.apiClient.pay(request)).thenReturn(response)

        repository.estimatePayment(encodedInvoice, 10_000)

        assertThat(repository.estimatePayment(encodedInvoice, 10_000)).isEqualTo(expectedPaymentResponse.result)
    }

    @Test
    fun processes_a_payment_request() {
        val repository = createRepository()
        val encodedInvoice = "ln--encoded-invoice"

        val paymentRequest = PaymentRequest(encodedInvoice, 10_000)
        val paymentResponse = PaymentResponse(result = LedgerInvoice())
        val response = Response.success(paymentResponse)
        whenever(repository.apiClient.pay(paymentRequest)).thenReturn(response)

        assertThat(repository.pay(encodedInvoice, 10_000)).isEqualTo(paymentResponse.result)
    }

    @Test
    fun payment_request_with_insufficient_funds() {
        val repository = createRepository()
        val encodedInvoice = "ln--encoded-invoice"

        val paymentRequest = PaymentRequest(encodedInvoice, 10_000)
        val response = Response.error<PaymentResponse>(
                400, ResponseBody.create(
                MediaType.parse("plain/text"), "Insufficient Funds"))
        whenever(repository.apiClient.pay(paymentRequest)).thenReturn(response)

        assertThat(repository.pay(encodedInvoice, 10_000)).isNotNull()

    }

    @Test
    fun payment_request__with_max_limit_reached_error() {
        val repository = createRepository()
        val encodedInvoice = "ln--encoded-invoice"

        val paymentRequest = PaymentRequest(encodedInvoice, 10_000)
        val response = Response.error<PaymentResponse>(
                400, ResponseBody.create(MediaType.parse("application/json"), Testdata.maxRequestError))
        whenever(repository.apiClient.pay(paymentRequest)).thenReturn(response)

        assertThat(repository.pay(encodedInvoice, 10_000)).isEqualTo(LedgerInvoice(error = "Max request value is 500000"))
    }

    private fun createRepository(): ThunderDomeRepository = ThunderDomeRepository(mock(), mock())

}