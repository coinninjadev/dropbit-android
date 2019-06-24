package com.coinninja.coinkeeper.service.client.model

import com.google.gson.annotations.SerializedName

data class CNTopicSubscription(
        @SerializedName("topic_ids")
        val topics: List<String>) {

        override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as CNTopicSubscription

                if (topics != other.topics) return false

                return true
        }

        override fun hashCode(): Int {
                return topics.hashCode()
        }
}
