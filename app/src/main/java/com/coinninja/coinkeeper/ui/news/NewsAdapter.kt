package com.coinninja.coinkeeper.ui.news

import android.net.Uri
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.dropbit.annotations.Mockable
import app.dropbit.commons.util.makeRelativeTime
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.service.client.model.NewsArticle
import com.coinninja.coinkeeper.util.analytics.Analytics
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil
import com.squareup.picasso.Picasso
import javax.inject.Inject

@Mockable
class NewsAdapter @Inject constructor(val activityNavigationUtil: ActivityNavigationUtil,
                                      val picasso: Picasso,
                                      val newsSourceMap: NewsSourceMap,
                                      val analytics: Analytics
) : RecyclerView.Adapter<NewsAdapter.ViewHolder>() {

    val articles: MutableList<NewsArticle> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_news_article, parent, false))
    }

    override fun getItemCount(): Int = articles.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val article = articles[position]
        holder.reset()
                .link(article.link)
                .thumbnail(article.thumbnail, article.source)
                .textFor(article.pubTime.makeRelativeTime(), R.id.published)
                .textFor(article.source, R.id.source)
                .textFromHtml(article.description, R.id.description)
                .textFor(article.title, R.id.headline)
    }

    fun addArticles(newArticles: List<NewsArticle>) {
        val start = articles.size
        articles.addAll(start, newArticles)
        notifyItemRangeChanged(start, newArticles.size)
    }

    fun clearArticles() = articles.clear().also { notifyDataSetChanged() }

    inner class ViewHolder constructor(val view: View) : RecyclerView.ViewHolder(view) {
        fun reset(): ViewHolder {
            view.apply { tag = null }
            view.findViewById<ImageView>(R.id.thumbnail)?.apply {
                visibility = VISIBLE
                tag = null
            }
            view.findViewById<TextView>(R.id.source)?.apply {
                text = ""
            }
            view.findViewById<TextView>(R.id.published)?.apply {
                text = ""
            }
            view.findViewById<TextView>(R.id.headline)?.apply {
                text = ""
            }
            view.findViewById<TextView>(R.id.description)?.apply {
                text = ""
            }
            return this
        }

        fun link(link: String?): ViewHolder {
            view.tag = link
            view.setOnClickListener { v ->
                if (v.tag != null)
                    activityNavigationUtil.openUrl(v.context, Uri.parse(v.tag.toString())).also {
                        analytics.trackEvent(Analytics.EVENT_NEWS_ARTICLE_OPENED)
                    }
            }
            return this
        }


        fun thumbnail(thumb: String?, source: String?): ViewHolder {
            view.findViewById<ImageView>(R.id.thumbnail)?.also {
                val resourceId = newsSourceMap.provide(source)
                if (resourceId != null) {
                    it.setImageResource(resourceId)
                    it.tag = resourceId
                    it.visibility = VISIBLE
                }

                if (!thumb.isNullOrEmpty() && source != "cointelegraph") {
                    picasso.load(thumb).into(it)
                    it.tag = thumb
                }
            }
            return this
        }

        fun textFor(text: String?, resourceId: Int): ViewHolder {
            view.findViewById<TextView>(resourceId)?.also {
                it.text = text ?: ""
            }
            return this
        }

        fun textFromHtml(text: String?, resourceId: Int): ViewHolder {
            view.findViewById<TextView>(resourceId)?.also {
                it.text = Html.fromHtml(text).toString()
            }
            return this
        }
    }
}
