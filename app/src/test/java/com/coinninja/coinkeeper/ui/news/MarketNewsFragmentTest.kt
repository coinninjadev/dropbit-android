package com.coinninja.coinkeeper.ui.news

import androidx.fragment.app.testing.FragmentScenario
import androidx.recyclerview.widget.RecyclerView
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.service.client.model.NewsArticle
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MarketNewsFragmentTest {

    private fun createScenario(): FragmentScenario<MarketNewsFragment> {
        return FragmentScenario.launchInContainer(MarketNewsFragment::class.java)
    }

    @Test
    fun `sets up list`() {
        createScenario().onFragment {
            it.findViewById<RecyclerView>(R.id.news)!!.also { view ->
                assertThat(view.adapter).isEqualTo(it.newsAdapter)
                assertThat(view.layoutManager).isNotNull()
            }
        }
    }

    @Test
    fun `fetches first page of news articles`() {
        createScenario().onFragment {
            val orderOperations = inOrder(it.newsAdapter, it.newsViewModel, it.newsViewModel.articles)

            orderOperations.verify(it.newsAdapter).clearArticles()
            orderOperations.verify(it.newsViewModel.articles).observe(it, it.articleChangeObserver)
            orderOperations.verify(it.newsViewModel).fetchNews(0)
        }
    }

    @Test
    fun `fetches second page of news articles`() {
        createScenario().onFragment {
            whenever(it.newsAdapter.itemCount).thenReturn(5)

            it.loadNewsArticles()

            verify(it.newsViewModel).fetchNews(5)
        }
    }

    @Test
    fun `clears news articles when pulling to refresh`() {
        createScenario().onFragment { fragment ->
            fragment.onRefreshNews()

            verify(fragment.newsAdapter, times(2)).clearArticles()
            verify(fragment.newsViewModel, times(2)).fetchNews(0)
            assertThat(fragment.isLoading).isEqualTo(true)
        }
    }

    @Test
    fun `sets page size after pages loaded`() {
        createScenario().onFragment {
            val articles: List<NewsArticle> = listOf(mock(), mock())
            whenever(it.newsAdapter.itemCount).thenReturn(articles.size)

            it.articleChangeObserver.onChanged(articles)

            assertThat(it.pageSize).isEqualTo(articles.size)
            assertThat(it.isLoading).isEqualTo(false)
        }
    }
}