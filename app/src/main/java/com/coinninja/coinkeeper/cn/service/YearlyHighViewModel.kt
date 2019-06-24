package com.coinninja.coinkeeper.cn.service

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import app.dropbit.annotations.Mockable
import app.dropbit.commons.util.CoroutineContextProvider
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@Mockable
class YearlyHighViewModel @Inject constructor(internal val yearlyHighSubscription: YearlyHighSubscription,
                                              internal val coroutineContextProvider: CoroutineContextProvider) : ViewModel() {

    val isSubscribedToYearlyHigh: MutableLiveData<Boolean> = MutableLiveData()

    init {
        fetchIsSubscribed()
    }

    fun toggleSubscription() {
        if (isSubscribedToYearlyHigh.value ?: false)
            unsubscribe()
        else
            subscribe()
    }

    private fun subscribe() {
        GlobalScope.launch(coroutineContextProvider.Main) {
            withContext(coroutineContextProvider.IO) { yearlyHighSubscription.subscribe() }
            fetchIsSubscribed()
        }
    }

    private fun unsubscribe() {
        GlobalScope.launch(coroutineContextProvider.Main) {
            withContext(coroutineContextProvider.IO) { yearlyHighSubscription.unsubscribe() }
            fetchIsSubscribed()
        }
    }

    internal fun fetchIsSubscribed() {
        GlobalScope.launch(coroutineContextProvider.Main) {
            val subscribed = withContext(coroutineContextProvider.IO) {
                yearlyHighSubscription.isSubscribed()
            }
            isSubscribedToYearlyHigh.value = subscribed
        }
    }

}


