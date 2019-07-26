package com.coinninja.coinkeeper.ui.news

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import app.dropbit.annotations.Mockable
import app.dropbit.commons.util.CoroutineContextProvider
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient
import com.coinninja.coinkeeper.service.client.model.NewsArticle
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Mockable
class NewsViewModel constructor(val contextProvider: CoroutineContextProvider, val apiClient: SignedCoinKeeperApiClient) : ViewModel() {
    companion object {
        const val articleBatchSize: Int = 10
    }

    val articles: MutableLiveData<List<NewsArticle>> = MutableLiveData()

    @Suppress("UNCHECKED_CAST")
    fun fetchNews(offset: Int = 0) {
        GlobalScope.launch(contextProvider.Main) {
            val response = withContext(contextProvider.IO) {
                apiClient.loadNews(articleBatchSize, offset)
            }

            withContext(contextProvider.Main) {
                if (response.isSuccessful) {
                    articles.postValue(response.body() as List<NewsArticle>)
                }
            }
        }
    }
}
