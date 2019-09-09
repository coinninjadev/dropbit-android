package com.coinninja.coinkeeper.ui.base

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.viewpager.widget.ViewPager
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.TestCoinKeeperApplication
import com.coinninja.coinkeeper.cn.wallet.CNWalletManager
import com.coinninja.coinkeeper.interfaces.Authentication
import com.coinninja.coinkeeper.interfaces.PinEntry
import com.coinninja.coinkeeper.model.db.TransactionsInvitesSummary
import com.coinninja.coinkeeper.receiver.AuthenticationCompleteReceiver
import com.coinninja.coinkeeper.ui.home.HomeActivity
import com.coinninja.coinkeeper.ui.phone.verification.VerificationActivity
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.coinkeeper.view.activity.*
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.Assert.assertTrue
import org.greenrobot.greendao.query.LazyList
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowActivity

@RunWith(AndroidJUnit4::class)
class SecuredActivityTest {

    private lateinit var activity: BaseActivity
    private val shadowActivity: ShadowActivity get() = shadowOf(activity)
    private val application: TestCoinKeeperApplication get() = ApplicationProvider.getApplicationContext()

    private val pinEntry: PinEntry = mock()
    private val cnWalletManager: CNWalletManager = mock()
    private val authentication: Authentication = mock()

    internal var transactions: LazyList<TransactionsInvitesSummary>? = null

    @Before
    fun setUp() {
        application.authentication = authentication
        application.pinEntry = pinEntry
        application.cnWalletManager = cnWalletManager
        whenever(application.authentication.isAuthenticated).thenReturn(false)
    }

    @Test
    fun create_pint_activity_does_not_require_a_pin_or_auth() {
        activity = Robolectric.setupActivity(CreatePinActivity::class.java)

        assertNull(shadowActivity.nextStartedActivity)
    }

    @Test
    fun recover_wallet_does_not_require_a_pin_or_auth() {
        activity = Robolectric.setupActivity(RecoverWalletActivity::class.java)

        assertNull(shadowActivity.nextStartedActivity)
    }

    @Test
    fun restore_wallet_does_not_require_a_pin_or_auth() {
        val activityController = Robolectric.buildActivity(RestoreWalletActivity::class.java)
        activity = activityController.get()
        activityController.create()
        (activity.findViewById<View>(R.id.recovery_words_pager) as ViewPager).adapter = mock()
        activityController.start().resume().visible()
        assertNull(shadowActivity.nextStartedActivity)
    }

    @Test
    fun start_activity_excluded_from_no_pin_redirect() {
        activity = Robolectric.setupActivity(StartActivity::class.java)

        assertNull(shadowActivity.nextStartedActivity)
    }

    @Test
    fun on_auth_successful_send_ON_USER_AUTH_SUCCESSFUL_broadcast() {
        whenever(authentication.isAuthenticated).thenReturn(true)
        activity = Robolectric.setupActivity(HomeActivity::class.java)

        activity.onAuthenticationResult(AppCompatActivity.RESULT_OK)

        verify(activity.localBroadCastUtil).sendGlobalBroadcast(AuthenticationCompleteReceiver::class.java, DropbitIntents.ACTION_ON_USER_AUTH_SUCCESSFULLY)
    }

    @Test
    fun authenticates_user_on_session_resume() {
        whenever(authentication.isAuthenticated).thenReturn(false)
        whenever(application.cnWalletManager.hasWallet).thenReturn(true)
        whenever(application.pinEntry.hasExistingPin()).thenReturn(true)
        activity = Robolectric.setupActivity(HomeActivity::class.java)

        val intent = shadowActivity.nextStartedActivityForResult

        assertThat(intent.requestCode, equalTo(BaseActivity.AUTHENTICATION_REQUEST_CODE))
        assertThat(intent.intent.component.className,
                equalTo(AuthenticateActivity::class.java.name))
    }

    @Test
    fun successful_authentication_does_not_reauth() {
        whenever(application.authentication.isAuthenticated).thenReturn(false).thenReturn(true)
        whenever(application.pinEntry.hasExistingPin()).thenReturn(true)
        whenever(application.cnWalletManager.hasWallet).thenReturn(true)

        activity = Robolectric.setupActivity(HomeActivity::class.java)

        val intent: ShadowActivity.IntentForResult = shadowActivity.nextStartedActivityForResult
        assertThat(intent.requestCode, equalTo(BaseActivity.AUTHENTICATION_REQUEST_CODE))
        assertThat(intent.intent.component.className,
                equalTo(AuthenticateActivity::class.java.name))

        activity.onActivityResult(BaseActivity.AUTHENTICATION_REQUEST_CODE, AppCompatActivity.RESULT_OK, null)

        assertNull(shadowActivity.nextStartedActivityForResult)
    }

    @Test
    fun failed_authentication_requthenticates() {
        whenever(authentication.isAuthenticated).thenReturn(false)
        whenever(application.pinEntry.hasExistingPin()).thenReturn(true)
        whenever(application.cnWalletManager.hasWallet).thenReturn(true)

        activity = Robolectric.setupActivity(HomeActivity::class.java)

        var intent: ShadowActivity.IntentForResult = shadowActivity.nextStartedActivityForResult
        assertThat(intent.requestCode, equalTo(BaseActivity.AUTHENTICATION_REQUEST_CODE))
        assertThat(intent.intent.component.className,
                equalTo(AuthenticateActivity::class.java.name))

        activity.onActivityResult(BaseActivity.AUTHENTICATION_REQUEST_CODE, AppCompatActivity.RESULT_OK, null)

        intent = shadowActivity.nextStartedActivityForResult


        assertThat(intent.requestCode, equalTo(BaseActivity.AUTHENTICATION_REQUEST_CODE))
        assertThat(intent.intent.component.className,
                equalTo(AuthenticateActivity::class.java.name))
    }

    @Test
    fun canceling_authentication_finishes_activity() {
        whenever(authentication.isAuthenticated).thenReturn(false)
        whenever(application.pinEntry.hasExistingPin()).thenReturn(true)
        whenever(application.cnWalletManager.hasWallet).thenReturn(true)
        activity = Robolectric.setupActivity(HomeActivity::class.java)

        activity.onActivityResult(BaseActivity.AUTHENTICATION_REQUEST_CODE, AppCompatActivity.RESULT_CANCELED, null)

        assertTrue(shadowActivity.isFinishing)
    }

    @Test
    fun does_not_inturupt_users_screen_when_authenticated() {
        whenever(authentication.isAuthenticated).thenReturn(true)
        whenever(application.pinEntry.hasExistingPin()).thenReturn(true)

        activity = Robolectric.setupActivity(HomeActivity::class.java)

        assertNull(shadowActivity.nextStartedActivity)
    }

    @Test
    fun does_not_navigate_to_create_pin_with_verify_phone_when_isinstance_of_create_pin() {
        whenever(application.cnWalletManager.hasWallet).thenReturn(true)

        activity = Robolectric.setupActivity(CreatePinActivity::class.java)

        val intent = shadowActivity.nextStartedActivity

        assertNull(intent)
    }

    @Test
    fun navigates_to_create_pin_with_verify_phone_next_when_no_pin_and_backup() {
        whenever(application.pinEntry.hasExistingPin()).thenReturn(false)
        whenever(application.cnWalletManager.hasWallet).thenReturn(true)

        activity = Robolectric.setupActivity(HomeActivity::class.java)

        val intent = shadowActivity.nextStartedActivity
        assertThat(intent.component.className,
                equalTo(CreatePinActivity::class.java.name))
        assertThat(intent.getStringExtra(DropbitIntents.EXTRA_NEXT),
                equalTo(VerificationActivity::class.java.name))
    }

    @Test
    fun redirects_user_to_start_actiivty_when_pin_is_empty_and_no_backup() {
        whenever(application.pinEntry.hasExistingPin()).thenReturn(false)
        whenever(application.cnWalletManager.hasWallet).thenReturn(false)

        activity = Robolectric.setupActivity(HomeActivity::class.java)

        assertThat(shadowActivity.nextStartedActivity.component.className,
                equalTo(StartActivity::class.java.name))
    }

    @Test
    fun forwards_bundle_to_next_activity() {
        whenever(application.pinEntry.hasExistingPin()).thenReturn(true)
        val intent = Intent()
        intent.putExtra(DropbitIntents.EXTRA_NEXT, HomeActivity::class.java.name)
        val bundle_to_forward = Bundle()
        val recovery_words = arrayOf("foo", "bar")
        bundle_to_forward.putStringArray(DropbitIntents.EXTRA_RECOVERY_WORDS, recovery_words)
        intent.putExtra(DropbitIntents.EXTRA_NEXT_BUNDLE, bundle_to_forward)


        val activityController = Robolectric.buildActivity(CreatePinActivity::class.java, intent)
        val activity = activityController.get()
        activityController.newIntent(intent).create()
        activityController.start().resume().visible()
        val shadowActivity = shadowOf(activity)


        val startedIntent = shadowActivity.nextStartedActivity
        assertThat(startedIntent.component.className, equalTo(HomeActivity::class.java.name))
        assertThat(startedIntent.extras.getStringArray(DropbitIntents.EXTRA_RECOVERY_WORDS),
                equalTo(recovery_words))
        assertTrue(shadowActivity.isFinishing)
    }

    @Test
    fun shows_next_activity_from_creation_intent() {
        whenever(application.pinEntry.hasExistingPin()).thenReturn(true)
        val intent = Intent()
        intent.putExtra(DropbitIntents.EXTRA_NEXT, HomeActivity::class.java.name)
        val activityController = Robolectric.buildActivity(CreatePinActivity::class.java, intent)
        val activity = activityController.get()
        activityController.newIntent(intent).create()
        activityController.start().resume().visible()
        val shadowActivity = shadowOf(activity)


        val startedIntent = shadowActivity.nextStartedActivity
        assertThat(startedIntent.component.className, equalTo(HomeActivity::class.java.name))
        assertTrue(shadowActivity.isFinishing)
    }

    @Test
    fun pull_data_out_of_Bundle_using_key_then_add_to_root_intent() {
        whenever(application.pinEntry.hasExistingPin()).thenReturn(true)
        val extraData = Bundle()
        val intent = Intent()
        extraData.putString("random key", "random arbitrary data")
        intent.putExtra(DropbitIntents.EXTRA_NEXT_BUNDLE, extraData)

        val activityController = Robolectric.buildActivity(HomeActivity::class.java, intent).create()
        val activity = activityController.get()

        activity.showNext()
        val shadowActivity = shadowOf(activity)


        val startedIntent = shadowActivity.nextStartedActivity
        activity.startActivity(startedIntent)

        val bundle = startedIntent.extras
        assertThat(bundle.getString("random key"), equalTo("random arbitrary data"))
        assertTrue(shadowActivity.isFinishing)
    }

}