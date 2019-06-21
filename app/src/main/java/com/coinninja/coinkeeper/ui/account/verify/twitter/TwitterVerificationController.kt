package com.coinninja.coinkeeper.ui.account.verify.twitter

import android.content.*
import app.dropbit.annotations.Mockable
import app.dropbit.twitter.TwitterIntents
import app.dropbit.twitter.model.TwitterUser
import com.coinninja.coinkeeper.ui.dropbit.me.DropbitMeConfiguration
import com.coinninja.coinkeeper.ui.util.CallbackHandler
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil
import com.coinninja.coinkeeper.util.android.ServiceWorkUtil
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil
import javax.inject.Inject

@Mockable
class TwitterVerificationController @Inject constructor(internal val serviceWorkUtil: ServiceWorkUtil,
                                                        internal val activityNavigationUtil: ActivityNavigationUtil,
                                                        internal val dropbitMeConfiguration: DropbitMeConfiguration,
                                                        internal val localBroadCastUtil: LocalBroadCastUtil) : BroadcastReceiver() {
    companion object {
        val TWITTER_LOGIN_REQUEST_CODE = 10101
    }

    internal val intentFilter = IntentFilter(DropbitIntents.ACTION_VERIFY_TWITTER_COMPLETED)

    internal var activityContext: Context? = null
    internal var authenticationCallback: CallbackHandler? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.action?.let { action ->
            when (action) {
                DropbitIntents.ACTION_VERIFY_TWITTER_COMPLETED -> authenticationCallback?.callback() ?: onTwitterVerified()
            }
        }

    }

    fun onStarted(context: Context, authenticationCallback: CallbackHandler? = null) {
        activityContext = context
        this.authenticationCallback = authenticationCallback
        localBroadCastUtil.registerReceiver(this, intentFilter)
    }

    fun onStopped() {
        activityContext = null
        localBroadCastUtil.unregisterReceiver(this)
    }

    fun onTwitterAuthorized(data: Intent?) {
        data?.let {
            if (data.hasExtra(TwitterIntents.TWITTER_USER)) {
                val twitterUser: TwitterUser = data.getParcelableExtra(TwitterIntents.TWITTER_USER)
                twitterUser.userId?.let { userId ->
                    serviceWorkUtil.addVerifiedTwitterAccount(userId)
                }
            }
        }
    }

    internal fun onTwitterVerified() {
        activityContext?.let { context ->
            dropbitMeConfiguration.setInitialVerification()
            activityNavigationUtil.navigateToHome(context)
        }
    }
}

