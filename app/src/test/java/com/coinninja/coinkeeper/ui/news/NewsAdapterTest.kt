package com.coinninja.coinkeeper.ui.news

import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.dropbit.commons.util.DAY_MILLIS
import app.dropbit.commons.util.HOUR_MILLIS
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.TestCoinKeeperApplication
import com.coinninja.coinkeeper.service.client.model.NewsArticle
import com.coinninja.coinkeeper.util.analytics.Analytics
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.squareup.picasso.Picasso
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NewsAdapterTest {

    private val application: TestCoinKeeperApplication get() = ApplicationProvider.getApplicationContext()
    private fun createAdapter(): NewsAdapter {
        val picasso = mock<Picasso>()
        whenever(picasso.load(any<String>())).thenReturn(mock())
        return NewsAdapter(mock(), picasso, NewsSourceMap(), mock())
    }

    @Test
    fun updates_list_with_articles() {
        val adapter = createAdapter()
        val adapterDataObserver = mock<RecyclerView.AdapterDataObserver>()
        adapter.registerAdapterDataObserver(adapterDataObserver)
        assertThat(adapter.itemCount).isEqualTo(0)

        adapter.addArticles(listOf(mock(), mock()))
        assertThat(adapter.itemCount).isEqualTo(2)
        verify(adapterDataObserver).onItemRangeChanged(0, 2, null)
    }

    @Test
    fun clears_all_articles_from_adapter() {
        val adapter = createAdapter()
        adapter.addArticles(listOf(mock(), mock()))
        val adapterDataObserver = mock<RecyclerView.AdapterDataObserver>()
        adapter.registerAdapterDataObserver(adapterDataObserver)
        assertThat(adapter.itemCount).isEqualTo(2)

        adapter.clearArticles()

        assertThat(adapter.itemCount).isEqualTo(0)
        verify(adapterDataObserver).onChanged()
    }

    @Test
    fun binds_to_view() {
        val adapter = createAdapter()
        val parent = LinearLayout(application)
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.addArticles(listOf(
                NewsArticle("--id-1--", "--title-1--", "--link-1--",
                        "--description-1--", "cn",
                        "--author-1--", null, System.currentTimeMillis() - HOUR_MILLIS * 1, 1562770795),
                NewsArticle("--id-2--", "--title-2--", "--link-2--",
                        "--description-2--", "--source-2--",
                        "--author-2--", "--thumb-2--", System.currentTimeMillis() - DAY_MILLIS * 5, 1562770795)
        ))

        adapter.onBindViewHolder(holder, 0)
        assertThat(holder.view.tag).isEqualTo("--link-1--")
        assertThat(holder.view.findViewById<ImageView>(R.id.thumbnail).visibility).isEqualTo(View.VISIBLE)
        assertThat(holder.view.findViewById<ImageView>(R.id.thumbnail).tag).isEqualTo(R.drawable.cn)
        assertThat(holder.view.findViewById<TextView>(R.id.source).text).isEqualTo("cn")
        assertThat(holder.view.findViewById<TextView>(R.id.published).text).isEqualTo("1 hour ago")
        assertThat(holder.view.findViewById<TextView>(R.id.headline).text).isEqualTo("--title-1--")
        assertThat(holder.view.findViewById<TextView>(R.id.description).text).isEqualTo("--description-1--")

        adapter.onBindViewHolder(holder, 1)
        assertThat(holder.view.tag).isEqualTo("--link-2--")
        assertThat(holder.view.findViewById<ImageView>(R.id.thumbnail).visibility).isEqualTo(View.VISIBLE)
        assertThat(holder.view.findViewById<ImageView>(R.id.thumbnail).tag).isEqualTo("--thumb-2--")
        verify(adapter.picasso).load("--thumb-2--")
        verify(adapter.picasso.load("--thumb-2--")).into(holder.view.findViewById<ImageView>(R.id.thumbnail)!!)
        assertThat(holder.view.findViewById<TextView>(R.id.source).text).isEqualTo("--source-2--")
        assertThat(holder.view.findViewById<TextView>(R.id.published).text).isEqualTo("5 days ago")
        assertThat(holder.view.findViewById<TextView>(R.id.headline).text).isEqualTo("--title-2--")
        assertThat(holder.view.findViewById<TextView>(R.id.description).text).isEqualTo("--description-2--")
    }

    @Test
    fun clicking_on_view_follows_link() {
        val adapter = createAdapter()
        val parent = LinearLayout(application)
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.addArticles(listOf(
                NewsArticle("--id-1--", "--title-1--", "http://www.example.com",
                        "--description-1--", "cn",
                        "--author-1--", null, System.currentTimeMillis() - HOUR_MILLIS * 1, 1562770795)))


        adapter.onBindViewHolder(holder, 0)
        holder.view.performClick()

        verify(adapter.activityNavigationUtil).openUrl(parent.context, Uri.parse("http://www.example.com"))
        verify(adapter.analytics).trackEvent(Analytics.EVENT_NEWS_ARTICLE_OPENED)
    }
}