package app.coinninja.cn.thunderdome.client

import app.coinninja.cn.thunderdome.model.AccountResponse
import app.coinninja.cn.thunderdome.model.CreateInvoiceResponse
import app.coinninja.cn.thunderdome.model.WithdrawalRequest
import app.coinninja.cn.thunderdome.model.WithdrawalResponse
import app.dropbit.commons.currency.BTCCurrency
import com.google.common.truth.Truth.assertThat
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ThunderDomeApiClientTest {
    var server: MockWebServer = MockWebServer()

    @Before
    fun setUp() {
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun fetch_account() {
        val client = createClient()
        server.enqueue(MockResponse().setResponseCode(200).setBody(Testdata.account))

        val response: Response<AccountResponse> = client.account
        val takeRequest = server.takeRequest(100, TimeUnit.MILLISECONDS)

        assertThat(takeRequest.requestUrl.encodedPath()).isEqualTo("/api/v1/thunderdome/account")

        val account = response.body()!!
        assertThat(account.id).isEqualTo("--id--")
        assertThat(account.createdAt).isEqualTo("2019-08-16T15:26:10.036Z")
        assertThat(account.updatedAt).isEqualTo("2019-08-16T17:26:10.036Z")
        assertThat(account.address).isEqualTo("--address--")
        assertThat(account.balance).isEqualTo(123456789L)
        assertThat(account.pendingIn).isEqualTo(1000L)
        assertThat(account.pendingOut).isEqualTo(500L)
    }

    @Test
    fun post_withdrawal() {
        val client = createClient()
        val withdrawalRequest = WithdrawalRequest(
                BTCCurrency(10_000),
                BTCCurrency(500),
                BTCCurrency(50),
                "--address--",
                false
        )

        server.enqueue(MockResponse().setResponseCode(200).setBody(Testdata.withdrawalRequest))

        val response: Response<WithdrawalResponse> = client.withdraw(withdrawalRequest)
        val takeRequest = server.takeRequest(100, TimeUnit.MILLISECONDS)

        assertThat(takeRequest.requestUrl.encodedPath()).isEqualTo("/api/v1/thunderdome/withdraw")
        assertThat(takeRequest.method).isEqualTo("POST")
        assertThat(takeRequest.body.readUtf8()).isEqualTo("{\"value\":10000,\"address\":\"--address--\",\"blocks\":0,\"estimate\":false}")

        val invoice = response.body()!!.result
        assertThat(invoice.id).isEqualTo("--txid--")
        assertThat(invoice.direction).isEqualTo("OUT")
        assertThat(invoice.type).isEqualTo("BTC")
    }

    @Test
    fun post_withdrawal_estimate() {
        val client = createClient()
        val withdrawalRequest = WithdrawalRequest(
                BTCCurrency(10_000),
                BTCCurrency(500),
                BTCCurrency(50),
                "--address--",
                true
        )

        server.enqueue(MockResponse().setResponseCode(200).setBody(Testdata.withdrawalRequest))

        val response: Response<WithdrawalResponse> = client.estimateWithdraw(withdrawalRequest)
        val takeRequest = server.takeRequest(100, TimeUnit.MILLISECONDS)

        assertThat(takeRequest.requestUrl.encodedPath()).isEqualTo("/api/v1/thunderdome/withdraw")
        assertThat(takeRequest.method).isEqualTo("POST")
        assertThat(takeRequest.body.readUtf8()).isEqualTo("{\"value\":10000,\"address\":\"--address--\",\"blocks\":0,\"estimate\":true}")

        val invoice = response.body()!!.result
        assertThat(invoice.id).isEqualTo("--txid--")
        assertThat(invoice.direction).isEqualTo("OUT")
        assertThat(invoice.type).isEqualTo("BTC")
    }

    @Test
    fun creates_invoice_with_given_amount_and_memo() {
        val client = createClient()

        server.enqueue(MockResponse().setResponseCode(200).setBody(Testdata.createRequest))

        val response: Response<CreateInvoiceResponse> = client.createInvoiceFor(10_000, "--memo--")
        val takeRequest = server.takeRequest(100, TimeUnit.MILLISECONDS)

        assertThat(takeRequest.requestUrl.encodedPath()).isEqualTo("/api/v1/thunderdome/create")
        assertThat(takeRequest.method).isEqualTo("POST")
        assertThat(takeRequest.body.readUtf8()).isEqualTo("{\"value\":10000,\"memo\":\"--memo--\",\"expires\":86400}")

        val encodedInvoice = response.body()!!.request
        assertThat(encodedInvoice).isEqualTo("ln-encoded-invoice")
    }

    private fun createClient(): ThunderDomeApiClient {
        val retrofit = Retrofit.Builder()
                .addConverterFactory(
                        GsonConverterFactory.create())
                .baseUrl(server.url("").toString())
                .build()

        return ThunderDomeApiClient(retrofit.create(Client::class.java))
    }

}