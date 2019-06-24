package com.coinninja.coinkeeper.ui.base

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle

import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.coinninja.android.helpers.Views.withId
import com.coinninja.coinkeeper.CoinKeeperApplication
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.model.Identity
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient
import com.coinninja.coinkeeper.service.client.model.SentInvite
import com.coinninja.coinkeeper.util.TwitterUtil
import com.coinninja.coinkeeper.util.analytics.Analytics
import com.coinninja.coinkeeper.view.util.AlertDialogBuilder
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject

interface TransactionTweetCallback {
    fun tweetWasSuccessful(id: String)
}

class TransactionTweetDialog @Inject internal constructor(var analytics: Analytics, var picasso: Picasso, var twitterUtil: TwitterUtil, var signedCoinKeeperApiClient: SignedCoinKeeperApiClient) : BaseBottomDialogFragment() {

    var receivingIdentity: Identity? = null
    var inviteId: String = ""
    var callbackHandler: TransactionTweetCallback? = null
    var activity: BaseActivity? = null

    override fun getContentViewLayoutId(): Int {
        return R.layout.fragment_transaction_tweet
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Dialog_Transparent)
    }

    override fun onResume() {
        super.onResume()

        picasso.load(receivingIdentity?.avatarUrl).transform(CoinKeeperApplication.appComponent.provideCircleTransform()).into(withId<ImageView>(view, R.id.twitter_avatar_image_view))

        (withId(view, R.id.sending_tweet_text_view) as TextView).text = getString(R.string.tweet_sent_bitcoin, receivingIdentity?.handle)
        (withId(view, R.id.twitter_handle_text_view) as TextView).text = receivingIdentity?.secondaryDisplayName

        (withId(view, R.id.dropbit_tweet_button) as Button).setOnClickListener {
            dropBitTweetClicked()
        }

        (withId(view, R.id.tweet_yourself_button) as Button).setOnClickListener {
            tweetYourselfButtonClicked()
        }
    }

    private fun tweetYourselfButtonClicked() {
        analytics.trackEvent(Analytics.EVENT_TWEET_MANUALLY);
        val intent = twitterUtil.createTwitterIntent(context, getString(R.string.just_sent_bitcoin_invite, receivingIdentity?.handle, getString(R.string.dropbit_download_url)))
        if (intent != null) {
            startActivity(intent)
        }

        dismiss()
    }

    private fun okButtonClicked() {
        dismiss()
    }

    private fun dropBitTweetClicked() {
        analytics.trackEvent(Analytics.EVENT_TWEET_VIA_DROPBIT)
        activity?.showLoading()
        GlobalScope.launch {
            val invite = signedCoinKeeperApiClient.patchSuppressionForWalletAddressRequest(inviteId) as Response<SentInvite>
            withContext(Dispatchers.Main) {
                activity?.removeLoading()

                if (invite.isSuccessful && invite.body()?.delivery_id != "duplicate") {
                    invite.body()?.let {
                        callbackHandler?.tweetWasSuccessful(it.delivery_id)
                    }

                    dismiss()
                } else if (!invite.isSuccessful || invite.body()?.delivery_id == "duplicate") {
                    setupUIForDuplicateTweet()
                }
            }
        }
    }

    private fun setupUIForDuplicateTweet() {
        withId<TextView>(view, R.id.sending_tweet_text_view).text = getString(R.string.twitter_reminder, receivingIdentity?.handle)

        val okButton = withId<Button>(view, R.id.dropbit_tweet_button)
        okButton.text = "Ok"
        okButton.setOnClickListener {
            okButtonClicked()
        }

        withId<Button>(view, R.id.tweet_yourself_button).text = getString(R.string.ill_send_tweet)
    }
}


