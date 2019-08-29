package com.coinninja.coinkeeper.service.client

import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.BuildConfig
import com.coinninja.coinkeeper.service.client.CoinKeeperClient.Companion.ADDRESSES_RESULT_LIMIT
import com.coinninja.coinkeeper.service.client.model.CNPricing
import com.coinninja.coinkeeper.service.client.model.GsonAddress
import com.coinninja.coinkeeper.service.client.model.TransactionDetail
import com.coinninja.coinkeeper.service.client.model.TransactionStats
import com.google.gson.JsonObject
import retrofit2.Response

@Mockable
open class CoinKeeperApiClient(protected val client: CoinKeeperClient) : NetworkingApiClient() {

    val currentState: Response<CurrentState>
        get() = executeCall(client.checkIn())

    fun queryAddressesFor(inAddresses: Array<String>, page: Int): Response<List<GsonAddress>> {
        val query = createQuery("address", inAddresses)
        return executeCall(client.getAddresses(query, page, ADDRESSES_RESULT_LIMIT))
    }

    fun getTransactions(txids: Array<String>): Response<List<TransactionDetail>> {
        val query = createQuery("txid", txids)

        return executeCall(client.queryTransactions(query))
    }

    fun getHistoricPrice(id: String): Response<CNPricing> {
        return executeCall(client.getHistoricPrice(id))
    }

    fun getTransactionStats(transactionId: String): Response<TransactionStats> {
        return executeCall(client.getTransactionStats(transactionId))
    }

    fun checkHealth(): Response<JsonObject> {
        return executeCall(client.checkHealth())
    }

    companion object {
        private const val URI_BASE = BuildConfig.COIN_NINJA_API_BASE
        private const val API_VERSION = "v1"
        const val API_BASE_ROUTE = "$URI_BASE/api/$API_VERSION/"
    }

}
