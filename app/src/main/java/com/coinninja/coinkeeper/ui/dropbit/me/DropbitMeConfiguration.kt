package com.coinninja.coinkeeper.ui.dropbit.me

import android.net.Uri
import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.di.interfaces.CoinkeeperApplicationScope
import com.coinninja.coinkeeper.di.interfaces.DropbitMeUri
import com.coinninja.coinkeeper.model.db.DropbitMeIdentity
import com.coinninja.coinkeeper.model.helpers.DropbitAccountHelper
import com.coinninja.coinkeeper.twitter.MyTwitterProfile
import java.net.URLDecoder
import javax.inject.Inject

@Mockable
@CoinkeeperApplicationScope
class DropbitMeConfiguration @Inject
internal constructor(@DropbitMeUri internal val dropbitMeUri: Uri,
                     internal val dropbitAccountHelper: DropbitAccountHelper,
                     val myTwitterProfile: MyTwitterProfile) {

    var isNewlyVerified = false
        internal set

    internal var shouldPromptWhenNextAvailable = false
    internal var onViewDropBitMeViewRequestedObserver: OnViewDropBitMeViewRequestedObserver? = null

    val shareUrl: String
        get() {
            val identity: DropbitMeIdentity? = dropbitAccountHelper.preferredIdentity

            identity?.let { identity ->
                val handle = identity.handle
                return URLDecoder.decode(dropbitMeUri.buildUpon().appendPath(handle).toString(), "UTF-8")
            }
            return ""
        }

    val isDisabled: Boolean
        get() = dropbitAccountHelper.hasPrivateAccount

    fun setIsNewlyVerified() {
        isNewlyVerified = true
    }

    fun getAvatar(): String? {
        return myTwitterProfile.myUser?.profileImage
    }

    fun setInitialVerification() {
        isNewlyVerified = true
        shouldPromptWhenNextAvailable = true
    }

    fun shouldShowWhenPossible(): Boolean {
        return shouldPromptWhenNextAvailable
    }

    fun showWhenPossible() {
        shouldPromptWhenNextAvailable = true
        onViewDropBitMeViewRequestedObserver?.onShowDropBitMeRequested()
    }

    fun acknowledge() {
        isNewlyVerified = false
        shouldPromptWhenNextAvailable = false
    }

    fun hasVerifiedAccount(): Boolean = dropbitAccountHelper.hasVerifiedAccount

    fun setOnViewDropBitMeViewRequestedObserver(onViewDropBitMeViewRequestedObserver: OnViewDropBitMeViewRequestedObserver?) {
        this.onViewDropBitMeViewRequestedObserver = onViewDropBitMeViewRequestedObserver
    }
}
