package com.coinninja.coinkeeper.service.client.model

import app.dropbit.annotations.Mockable
import com.google.gson.annotations.SerializedName

@Mockable
data class NewsArticle(
        val id: String,
        val title: String?,
        val link: String?,
        val description: String?,
        val source: String?,
        val author: String?,
        val thumbnail: String?,
        @SerializedName("pub_time")
        val pubTime: Long,
        val added: Long)
