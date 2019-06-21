package com.coinninja.coinkeeper.ui.account.verify

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.Toast
import app.dropbit.twitter.ui.login.TwitterLoginActivity
import com.coinninja.android.helpers.Resources.getDrawable
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.model.helpers.DropbitAccountHelper
import com.coinninja.coinkeeper.twitter.MyTwitterProfile
import com.coinninja.coinkeeper.ui.account.verify.twitter.TwitterVerificationController
import com.coinninja.coinkeeper.ui.account.verify.twitter.TwitterVerificationController.Companion.TWITTER_LOGIN_REQUEST_CODE
import com.coinninja.coinkeeper.ui.dropbit.me.DropbitMeConfiguration
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.coinkeeper.util.analytics.Analytics
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil
import com.coinninja.coinkeeper.util.android.ServiceWorkUtil
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil
import javax.inject.Inject


class TwitterIdentityFragment : BaseIdentityFragment() {
    @Inject
    lateinit var dropbitAccountHelper: DropbitAccountHelper

    @Inject
    lateinit var serviceWorkUtil: ServiceWorkUtil

    @Inject
    lateinit var localBroadCastUtil: LocalBroadCastUtil

    @Inject
    lateinit var dropbitMeConfiguration: DropbitMeConfiguration

    @Inject
    lateinit var activityNavigationUtil: ActivityNavigationUtil

    @Inject
    lateinit var twitterVerificationController: TwitterVerificationController

    @Inject
    lateinit var analytics: Analytics

    @Inject
    lateinit var myTwitterProfile: MyTwitterProfile

    internal val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                DropbitIntents.ACTION_DEVERIFY_TWITTER_COMPLETED -> {
                    configureNotVerified()
                    activity?.let {
                        if (it is UserAccountVerificationActivity) {
                            it.invalidateCacheView()
                        }
                    }
                }
                DropbitIntents.ACTION_DEVERIFY_TWITTER_FAILED -> Toast.makeText(context, getString(R.string.deverification_twitter_failed), Toast.LENGTH_LONG).show()
            }
        }
    }


    internal val intentFilter = IntentFilter(DropbitIntents.ACTION_DEVERIFY_TWITTER_COMPLETED)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode.equals(TWITTER_LOGIN_REQUEST_CODE) && resultCode.equals(Activity.RESULT_OK)) {
            twitterVerificationController.onTwitterAuthorized(data)
        }
    }

    override fun primaryWarning(): String =
            context?.getString(R.string.deverification_twitter_removed_warning_message) ?: ""

    override fun primaryMessage(): String =
            context?.getString(R.string.deverification_twitter_removed_message) ?: ""

    override fun followUpMessage(): String =
            context?.getString(R.string.deverification_twitter_removed_message_follow_up) ?: ""

    override fun onVerify() {
        analytics.trackEvent(Analytics.EVENT_TWITTER_VERIFIED)
        analytics.setUserProperty(Analytics.PROPERTY_TWITTER_VERIFIED, true);
        startActivityForResult(Intent(context, TwitterLoginActivity::class.java), TWITTER_LOGIN_REQUEST_CODE)
    }

    override fun onDeVerify() {
        analytics.setUserProperty(Analytics.PROPERTY_TWITTER_VERIFIED, false);
        localBroadCastUtil.registerReceiver(receiver, intentFilter)
        serviceWorkUtil.deVerifyTwitter()
        myTwitterProfile.clear()
        activity?.let { it ->
            if (it is UserAccountVerificationActivity) {
                it.invalidateCacheView()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intentFilter.addAction(DropbitIntents.ACTION_DEVERIFY_TWITTER_FAILED)
        intentFilter.addAction(DropbitIntents.ACTION_VERIFY_TWITTER_COMPLETED)
    }


    override fun onStart() {
        super.onStart()
        configureForTwitter()
        activity?.let { it ->
            twitterVerificationController.onStarted(it, null)
        }
    }

    override fun onStop() {
        super.onStop()
        localBroadCastUtil.unregisterReceiver(receiver)
        twitterVerificationController.onStopped()
    }

    override fun onResume() {
        super.onResume()
        if (dropbitAccountHelper.isTwitterVerified) {
            val identity = dropbitAccountHelper.twitterIdentity()
            configureVerified("${identity?.handle}")
        }
    }

    internal fun configureForTwitter() {
        getVerificationButton()?.apply {
            text = getString(R.string.verify_twitter_account)
            setCompoundDrawables(getDrawable(context, R.drawable.twitter_icon), null, null, null)
            background = getDrawable(context, R.drawable.primary_button)
            setOnClickListener { onVerify() }
        }

        getIdentityView()?.apply {
            setImage(R.drawable.twitter_icon)
        }
    }
}
