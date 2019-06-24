package com.coinninja.coinkeeper.service.client.model

import java.util.*

data class CNTopic(
        var id: String = "",
        var name: String = ""
) {
    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val cnTopic = o as CNTopic?
        return id == cnTopic!!.id
    }

    override fun hashCode(): Int {
        return Objects.hash(id)
    }
}
