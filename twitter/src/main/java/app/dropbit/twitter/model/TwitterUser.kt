package app.dropbit.twitter.model

import android.os.Parcel
import android.os.Parcelable
import app.dropbit.annotations.Mockable
import com.google.gson.annotations.SerializedName

@Mockable
class TwitterUser() : Parcelable {
    @SerializedName("id")
    var userId: Long? = null

    @SerializedName("screen_name")
    var screenName: String? = null

    var name: String? = null
    var url: String? = null
    var description: String? = null
    var protected: Boolean? = null
    var verified: Boolean? = null

    @SerializedName("followers_count")
    var numFollowers: Int? = null

    @SerializedName("friends_count")
    var numFriends: Int? = null

    @SerializedName("profile_image_url_https")
    var profileImage: String? = null

    constructor(parcel: Parcel) : this() {
        userId = parcel.readValue(Long::class.java.classLoader) as? Long
        screenName = parcel.readString()
        name = parcel.readString()
        url = parcel.readString()
        description = parcel.readString()
        protected = parcel.readValue(Boolean::class.java.classLoader) as? Boolean
        verified = parcel.readValue(Boolean::class.java.classLoader) as? Boolean
        numFollowers = parcel.readValue(Int::class.java.classLoader) as? Int
        numFriends = parcel.readValue(Int::class.java.classLoader) as? Int
        profileImage = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(userId)
        parcel.writeString(screenName)
        parcel.writeString(name)
        parcel.writeString(url)
        parcel.writeString(description)
        parcel.writeValue(protected)
        parcel.writeValue(verified)
        parcel.writeValue(numFollowers)
        parcel.writeValue(numFriends)
        parcel.writeString(profileImage)
    }

    override fun describeContents(): Int {
        return 0
    }

    fun displayScreenName(): String {
        return "@$screenName"
    }

    companion object CREATOR : Parcelable.Creator<TwitterUser> {
        override fun createFromParcel(parcel: Parcel): TwitterUser {
            return TwitterUser(parcel)
        }

        override fun newArray(size: Int): Array<TwitterUser?> {
            return arrayOfNulls(size)
        }
    }
}
