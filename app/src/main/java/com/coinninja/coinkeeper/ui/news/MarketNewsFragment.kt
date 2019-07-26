package com.coinninja.coinkeeper.ui.news

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.service.client.model.NewsArticle
import com.coinninja.coinkeeper.ui.base.BaseFragment
import javax.inject.Inject

class MarketNewsFragment : BaseFragment() {

    var isLoading: Boolean = true
    var pageSize: Int = 0
    val articleChangeObserver = Observer<List<NewsArticle>> { loadedArticles ->
        findViewById<SwipeRefreshLayout>(R.id.pull_refresh_container)?.apply { isRefreshing = false }
        newsAdapter.addArticles(loadedArticles)
        pageSize = newsAdapter.itemCount
        isLoading = false
    }

    val onScrollChangedObserver = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            findViewById<RecyclerView>(R.id.news)?.also {
                it.layoutManager?.let { layoutManager ->
                    val linearLayoutManager = layoutManager as LinearLayoutManager
                    val visibleItemCount = linearLayoutManager.childCount
                    val totalItemCount = linearLayoutManager.itemCount
                    val firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition()

                    if (!isLoading) {
                        if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                                && firstVisibleItemPosition >= 0
                                && totalItemCount >= pageSize - 10) {
                            loadNewsArticles()
                        }
                    }
                }
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
        }
    }

    internal fun loadNewsArticles() {
        isLoading = true
        newsViewModel.fetchNews(newsAdapter.itemCount)
    }

    @Inject
    lateinit var newsAdapter: NewsAdapter

    @Inject
    lateinit var newsViewModel: NewsViewModel


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_market_news, container, false)
    }

    override fun onResume() {
        super.onResume()
        setupList()
    }

    fun onRefreshNews() {
        newsAdapter.clearArticles()
        pageSize = 0
        loadNewsArticles()
    }

    private fun setupList() {
        findViewById<RecyclerView>(R.id.news)?.apply {
            val layoutManager = LinearLayoutManager(context)
            this.layoutManager = layoutManager
            addItemDecoration(DividerItemDecoration(context, layoutManager.orientation))
            setHasFixedSize(false)
            adapter = newsAdapter
            setOnScrollListener(onScrollChangedObserver)
        }
        findViewById<SwipeRefreshLayout>(R.id.pull_refresh_container)?.apply {
            setOnRefreshListener { onRefreshNews() }
        }

        newsAdapter.clearArticles()
        newsViewModel.articles.observe(this, articleChangeObserver)
        loadNewsArticles()
    }
}
