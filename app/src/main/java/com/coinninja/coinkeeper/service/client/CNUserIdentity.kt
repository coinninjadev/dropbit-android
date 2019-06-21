package com.coinninja.coinkeeper.service.client

import app.dropbit.annotations.Mockable
import com.google.gson.annotations.SerializedName

@Mockable
data class CNUserIdentity(
        var id: String? = null,
        @SerializedName("created_at")
        var createdAt: String? = null,
        @SerializedName("updated_at")
        var updatedAt: String? = null,
        var type: String? = null,
        var identity: String? = null,
        var hash: String? = null,
        var handle: String? = null,
        var status: String? = null,
        var code: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CNUserIdentity

        if (id != other.id) return false
        if (createdAt != other.createdAt) return false
        if (updatedAt != other.updatedAt) return false
        if (type != other.type) return false
        if (identity != other.identity) return false
        if (hash != other.hash) return false
        if (handle != other.handle) return false
        if (status != other.status) return false
        if (code != other.code) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + (createdAt?.hashCode() ?: 0)
        result = 31 * result + (updatedAt?.hashCode() ?: 0)
        result = 31 * result + (type?.hashCode() ?: 0)
        result = 31 * result + (identity?.hashCode() ?: 0)
        result = 31 * result + (hash?.hashCode() ?: 0)
        result = 31 * result + (handle?.hashCode() ?: 0)
        result = 31 * result + (status?.hashCode() ?: 0)
        result = 31 * result + (code?.hashCode() ?: 0)
        return result
    }
}
