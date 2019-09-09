package app.coinninja.cn.thunderdome.client

import app.coinninja.cn.thunderdome.model.AccountResponse
import app.coinninja.cn.thunderdome.model.LedgerResponse
import app.coinninja.cn.thunderdome.model.WithdrawalRequest
import app.coinninja.cn.thunderdome.model.WithdrawalResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface Client {

    @GET("/api/v1/thunderdome/account")
    fun account(): Call<AccountResponse>

    @GET("/api/v1/thunderdome/ledger")
    fun ledger(): Call<LedgerResponse>

    @POST("/api/v1/thunderdome/withdraw")
    fun withdrawal(@Body withdrawalRequest: WithdrawalRequest.WithdrawalPostRequest): Call<WithdrawalResponse>

}