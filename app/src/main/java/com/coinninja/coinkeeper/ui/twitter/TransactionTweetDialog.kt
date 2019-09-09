package com.coinninja.coinkeeper.ui.twitter

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.cn.dropbit.DropbitTwitterInviteTweetSuppressionCheck
import com.coinninja.coinkeeper.cn.dropbit.ShouldManuallyMentionCallback
import com.coinninja.coinkeeper.model.Identity
import com.coinninja.coinkeeper.ui.base.BaseBottomDialogFragment
import com.coinninja.coinkeeper.util.analytics.Analytics
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil
import com.coinninja.coinkeeper.util.image.CircleTransform
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_transaction_tweet.*
import javax.inject.Inject


class TransactionTweetDialog : BaseBottomDialogFragment() {

    companion object {
        fun createInstance(inviteId: String, identity: Identity, callback: TransactionTweetCallback): TransactionTweetDialog {
            return TransactionTweetDialog().also {
                it.inviteId = inviteId
                it.identity = identity
                it.callback = callback
            }
        }
    }

    @Inject
    internal lateinit var analytics: Analytics
    @Inject
    internal lateinit var picasso: Picasso
    @Inject
    internal lateinit var circleTransform: CircleTransform
    @Inject
    internal lateinit var suppressionCheck: DropbitTwitterInviteTweetSuppressionCheck


    internal lateinit var inviteId: String
    internal lateinit var identity: Identity
    internal lateinit var callback: TransactionTweetCallback

    internal val shouldManuallyMentionCallback: ShouldManuallyMentionCallback = object : ShouldManuallyMentionCallback {
        override fun onManualShouldManuallyMention(shouldBeManual: Boolean) {
            if (shouldBeManual) {
                setupUIForManual()
            } else {
                setupUIForDuplicateTweet()
            }
        }
    }

    internal val avatar: ImageView? get() = findViewById(R.id.twitter_avatar_image_view)
    internal val sendingTweetTextView: TextView? get() = findViewById(R.id.sending_tweet_text_view)
    internal val twitterHandle: TextView? get() = findViewById(R.id.twitter_handle_text_view)
    internal val dropbitSendsTweet: Button? get() = findViewById(R.id.dropbit_tweet_button)
    internal val userSendsTweet: Button? get() = findViewById(R.id.tweet_yourself_button)

    override fun getContentViewLayoutId(): Int {
        return R.layout.fragment_transaction_tweet
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Dialog_Transparent)
    }

    override fun onResume() {
        super.onResume()
        picasso.load(identity.avatarUrl).transform(circleTransform).into(avatar)
        sendingTweetTextView?.text = getString(R.string.tweet_sent_bitcoin, identity.handle)
        twitterHandle?.text = identity.secondaryDisplayName
        userSendsTweet?.setOnClickListener { tweetYourselfButtonClicked() }
        suppressionCheck.shouldManuallySendTwitterMention(inviteId, shouldManuallyMentionCallback)
    }

    private fun setupUIForManual() {
        dropbitSendsTweet?.setOnClickListener { suppressionCheck.shouldManuallySendTwitterMention(inviteId, shouldManuallyMentionCallback) }
    }

    private fun setupUIForDuplicateTweet() {
        sendingTweetTextView?.text = getString(R.string.twitter_reminder, identity.handle)
        dropbitSendsTweet?.text = getString(R.string.ok)
        dropbitSendsTweet?.setOnClickListener { dismiss() }
        tweet_yourself_button.text = getString(R.string.ill_send_tweet)
    }

    private fun tweetYourselfButtonClicked() {
        analytics.trackEvent(Analytics.EVENT_TWEET_MANUALLY)
        activity?.let {
            activityNavigationUtil.shareWithTwitter(it,
                    getString(R.string.just_sent_bitcoin_invite,
                            identity.handle,
                            getString(R.string.dropbit_download_url)
                    )
            )
        }
        dismiss()
    }
}


