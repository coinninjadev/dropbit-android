package app.dropbit.twitter.model

import com.google.gson.annotations.SerializedName

class FollowersResponse {
    var users: List<TwitterUser>? = null
    @SerializedName("next_cursor")
    var nextCursor: Long = -1
    @SerializedName("next_cursor_str")
    var nextCursorString: String = "-1"
    var count: Int = 0
    @SerializedName("skip_status")
    var skipStatus: Boolean = false
    @SerializedName("previous_cursor_str")
    var previousCursorString: String = "-1"
    @SerializedName("previous_cursor")
    var previousCursor: Long = -1

}
