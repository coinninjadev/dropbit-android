package com.coinninja.coinkeeper.service.tasks

import android.os.AsyncTask

import com.coinninja.coinkeeper.service.client.CoinKeeperApiClient

import javax.inject.Inject

class CNHealthCheckTask @Inject internal constructor(
        internal val apiClient: CoinKeeperApiClient
) : AsyncTask<Void, Void, Boolean>() {

    var callback: HealthCheckCallback? = null

    constructor(apiClient: CoinKeeperApiClient, callback: HealthCheckCallback?): this(apiClient) {
        this.callback = callback
    }

    public override fun doInBackground(vararg voids: Void): Boolean? {
        return apiClient.checkHealth().isSuccessful
    }

    public override fun onPostExecute(isSuccss: Boolean) {
        if (isSuccss)
            callback!!.onHealthSuccess()
        else
            callback!!.onHealthFail()
    }

    fun clone(): CNHealthCheckTask {
        return CNHealthCheckTask(apiClient, callback)
    }

    interface HealthCheckCallback {
        fun onHealthSuccess()

        fun onHealthFail()
    }
}
