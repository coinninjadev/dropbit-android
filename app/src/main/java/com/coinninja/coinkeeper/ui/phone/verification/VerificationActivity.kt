package com.coinninja.coinkeeper.ui.phone.verification

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import app.dropbit.twitter.ui.login.TwitterLoginActivity
import com.coinninja.android.helpers.Views.withId
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.model.PhoneNumber
import com.coinninja.coinkeeper.ui.account.verify.twitter.TwitterVerificationController
import com.coinninja.coinkeeper.ui.account.verify.twitter.TwitterVerificationController.Companion.TWITTER_LOGIN_REQUEST_CODE
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.coinkeeper.util.analytics.Analytics
import com.coinninja.coinkeeper.util.android.ServiceWorkUtil
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil
import com.coinninja.coinkeeper.view.activity.base.SecuredActivity
import com.coinninja.coinkeeper.view.widget.phonenumber.CountryCodeLocale
import com.coinninja.coinkeeper.view.widget.phonenumber.CountryCodeLocaleGenerator
import com.coinninja.coinkeeper.view.widget.phonenumber.PhoneVerificationView
import com.google.i18n.phonenumbers.Phonenumber
import javax.inject.Inject

class VerificationActivity : SecuredActivity() {

    var countryCodeLocales: List<CountryCodeLocale> = emptyList()

    @Inject
    internal lateinit var countryCodeLocaleGenerator: CountryCodeLocaleGenerator
    @Inject
    internal lateinit var activityNavigationUtil: ActivityNavigationUtil
    @Inject
    internal lateinit var serviceWorkUtil: ServiceWorkUtil
    @Inject
    internal lateinit var twitterVerificationController: TwitterVerificationController

    internal lateinit var phoneVerificationView: PhoneVerificationView

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode.equals(TWITTER_LOGIN_REQUEST_CODE) && resultCode.equals(Activity.RESULT_OK)) {
            twitterVerificationController.onTwitterAuthorized(data)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val intent = intent
        if (intent != null && intent.getBooleanExtra(DropbitIntents.EXTRA_HIDE_SKIP_BUTTON, false)) {
            setTheme(R.style.CoinKeeperTheme_UpOn)
        }

        super.onCreate(savedInstanceState)
        countryCodeLocales = countryCodeLocaleGenerator.generate()
        setContentView(R.layout.activity_verify_phone)
        phoneVerificationView = withId(this, R.id.verification_view)

        if (intent != null && intent.getBooleanExtra(DropbitIntents.EXTRA_SHOW_TWITTER_VERIFY_BUTTON, false)) {
            setupTwitterVerificationButton()
        }
    }

    private fun setupTwitterVerificationButton() {
        findViewById<View>(R.id.verify_twitter_group).visibility = View.VISIBLE
        findViewById<View>(R.id.verify_twitter_button).setOnClickListener { verifyTwitterButtonClicked() }
    }

    private fun verifyTwitterButtonClicked() {
        startActivityForResult(Intent(this, TwitterLoginActivity::class.java), TWITTER_LOGIN_REQUEST_CODE)
    }

    override fun onStart() {
        super.onStart()
        twitterVerificationController.onStarted(this, null)
        phoneVerificationView.setOnValidPhoneNumberObserver { this.onPhoneNumberValid(it) }
    }

    override fun onResume() {
        super.onResume()
        phoneVerificationView.setCountryCodeLocals(countryCodeLocales)
    }

    override fun onPause() {
        super.onPause()
        phoneVerificationView.resetView()
        twitterVerificationController.onStopped()
    }


    override fun onSkipClicked() {
        if (analytics != null) {
            analytics.trackEvent(Analytics.EVENT_PHONE_VERIFICATION_SKIPPED)
        }
        activityNavigationUtil.navigateToHome(this)
    }

    internal fun onPhoneNumberValid(phoneNumber: Phonenumber.PhoneNumber) {
        val number = PhoneNumber(phoneNumber)
        serviceWorkUtil.registerUsersPhone(number)
        activityNavigationUtil.navigateToVerifyPhoneNumberCode(this, number)
    }

}
