package app.coinninja.cn.thunderdome.client

import app.coinninja.cn.thunderdome.model.*
import app.dropbit.annotations.Mockable
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.IOException

@Mockable
class ThunderDomeApiClient constructor(val client: Client) {


    val account: Response<AccountResponse> get() = executeCall(client.account())
    fun ledger(): Response<LedgerResponse> = executeCall(client.ledger())
    fun withdraw(withdrawalRequest: WithdrawalRequest): Response<WithdrawalResponse> = executeCall(client.withdrawal(withdrawalRequest.forPost()))
    fun estimateWithdraw(withdrawalRequest: WithdrawalRequest): Response<WithdrawalResponse> = executeCall(client.withdrawal(withdrawalRequest.forPost()))
    fun createInvoiceFor(amount: Long, memo: String): Response<CreateInvoiceResponse> = executeCall(client.createInvoice(CreateInvoiceRequest(amount, memo)))
    fun decode(decodeRequest: DecodeRequest): Response<RequestInvoice> = executeCall(client.decode(decodeRequest))
    fun pay(paymentRequest: PaymentRequest): Response<PaymentResponse> = executeCall(client.pay(paymentRequest))

    private fun <T> executeCall(call: Call<T>): Response<T> {
        val response: Response<T>

        response = try {
            call.execute()
        } catch (e: IOException) {
            e.printStackTrace()
            createTeaPotErrorFor(call, e.message)
        }

        return response
    }

    private fun <T> createTeaPotErrorFor(call: Call<T>, message: String?): Response<T> {
        return Response.error(okhttp3.ResponseBody.create(null, ""), okhttp3.Response.Builder()
                .code(errorCode)
                .message(if (message.isNullOrEmpty()) "" else message)
                .protocol(Protocol.HTTP_1_1)
                .request(Request.Builder().url(call.request().url()).build())
                .build())

    }


    companion object {
        private const val errorCode: Int = 418

        fun create(route: String, requestInterceptor: Interceptor? = null): ThunderDomeApiClient {
            val builder = Retrofit.Builder()
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(
                            GsonConverterFactory.create())
                    .baseUrl(route)

            requestInterceptor?.let { interceptor ->
                builder.client(OkHttpClient.Builder()
                        .addInterceptor(requestInterceptor)
                        .build())
            }
            return ThunderDomeApiClient(builder.build().create(Client::class.java))
        }
    }


}