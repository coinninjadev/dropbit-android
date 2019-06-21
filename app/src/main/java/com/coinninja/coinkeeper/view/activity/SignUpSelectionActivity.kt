package com.coinninja.coinkeeper.view.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import app.dropbit.twitter.ui.login.TwitterLoginActivity
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.ui.account.verify.twitter.TwitterVerificationController
import com.coinninja.coinkeeper.ui.account.verify.twitter.TwitterVerificationController.Companion.TWITTER_LOGIN_REQUEST_CODE
import com.coinninja.coinkeeper.ui.phone.verification.VerificationActivity
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.coinkeeper.view.activity.base.SecuredActivity
import javax.inject.Inject

class SignUpSelectionActivity : SecuredActivity() {

    @Inject
    internal lateinit var twitterVerificationController: TwitterVerificationController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup_selection)
        findViewById<View>(R.id.twitter_invite_button).setOnClickListener({ twitterInviteButtonClicked() })
        findViewById<View>(R.id.text_message_invite_button).setOnClickListener({ textMessageInviteClicked() })
    }

    override fun onStart() {
        super.onStart()
        twitterVerificationController.onStarted(this)
    }

    override fun onStop() {
        twitterVerificationController.onStopped()
        super.onStop()
    }

    private fun textMessageInviteClicked() {
        val signUpSelectionIntent = intent
        var shouldHideSkip = false

        if (signUpSelectionIntent != null && signUpSelectionIntent.hasExtra(DropbitIntents.EXTRA_HIDE_SKIP_BUTTON)) {
            shouldHideSkip = signUpSelectionIntent.getBooleanExtra(DropbitIntents.EXTRA_HIDE_SKIP_BUTTON, false)
        }

        val verifyPhoneNumberActivity = Intent(this, VerificationActivity::class.java)

        verifyPhoneNumberActivity.putExtra(DropbitIntents.EXTRA_HIDE_SKIP_BUTTON, shouldHideSkip)
        navigateTo(verifyPhoneNumberActivity)
    }

    private fun twitterInviteButtonClicked() {
        startActivityForResult(Intent(this, TwitterLoginActivity::class.java), TWITTER_LOGIN_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode.equals(TWITTER_LOGIN_REQUEST_CODE) && resultCode.equals(Activity.RESULT_OK)) {
            twitterVerificationController.onTwitterAuthorized(data)
        }
    }
}
