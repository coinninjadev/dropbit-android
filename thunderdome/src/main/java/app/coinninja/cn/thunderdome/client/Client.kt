package app.coinninja.cn.thunderdome.client

import app.coinninja.cn.thunderdome.model.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface Client {

    @GET("/api/v1/thunderdome/account")
    fun account(): Call<AccountResponse>

    @GET("/api/v1/thunderdome/ledger")
    fun ledger(@Query("limit") limit:Int=1_000): Call<LedgerResponse>

    @POST("/api/v1/thunderdome/withdraw")
    fun withdrawal(@Body withdrawalRequest: WithdrawalRequest.WithdrawalPostRequest): Call<WithdrawalResponse>

    @POST("/api/v1/thunderdome/create")
    fun createInvoice(@Body createRequest: CreateInvoiceRequest): Call<CreateInvoiceResponse>

    @POST("/api/v1/thunderdome/decode")
    fun decode(@Body decodeRequest: DecodeRequest): Call<RequestInvoice>

    @POST("/api/v1/thunderdome/pay")
    fun pay(@Body paymentRequest: PaymentRequest): Call<PaymentResponse>
}