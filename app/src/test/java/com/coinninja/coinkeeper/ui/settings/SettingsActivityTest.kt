package com.coinninja.coinkeeper.ui.settings

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.Switch
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.android.helpers.Views.clickOn
import com.coinninja.android.helpers.Views.withId
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.TestCoinKeeperApplication
import com.coinninja.coinkeeper.cn.service.YearlyHighViewModel
import com.coinninja.coinkeeper.cn.wallet.dust.DustProtectionPreference
import com.coinninja.coinkeeper.ui.backup.BackupRecoveryWordsStartActivity
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.coinkeeper.view.activity.AuthorizedActionActivity
import com.coinninja.coinkeeper.view.activity.LicensesActivity
import com.coinninja.coinkeeper.view.activity.SplashActivity
import com.coinninja.coinkeeper.view.dialog.GenericAlertDialog
import com.coinninja.matchers.IntentMatcher
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertNotNull
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.robolectric.Shadows.shadowOf

@RunWith(AndroidJUnit4::class)
class SettingsActivityTest {

    @Before
    fun configureDI() {
        val coinKeeperApplication = ApplicationProvider.getApplicationContext<TestCoinKeeperApplication>()
        coinKeeperApplication.dustProtectionPreference = mock(DustProtectionPreference::class.java)
        coinKeeperApplication.yearlyHighViewModel = mock(YearlyHighViewModel::class.java)
        val liveData: MutableLiveData<Boolean> = mock(MutableLiveData::class.java) as MutableLiveData<Boolean>
        whenever(coinKeeperApplication.yearlyHighViewModel.isSubscribedToYearlyHigh).thenReturn(liveData)
        whenever(coinKeeperApplication.authentication.isAuthenticated).thenReturn(true)
    }

    fun setUp(): ActivityScenario<SettingsActivity> {
        val scenario = ActivityScenario.launch(SettingsActivity::class.java)
        scenario.onActivity { activity ->
            whenever(activity.dustProtectionPreference.isDustProtectionEnabled).thenReturn(false)
        }
        return scenario
    }

    @Test
    fun shows_not_backed_up_when_user_skipped_backup() {
        val coinKeeperApplication = ApplicationProvider.getApplicationContext<TestCoinKeeperApplication>()
        whenever(coinKeeperApplication.cnWalletManager.hasSkippedBackup()).thenReturn(true)
        val scenario = setUp()
        scenario.moveToState(Lifecycle.State.RESUMED)

        scenario.onActivity { activity ->
            assertThat(activity.findViewById<View>(R.id.not_backed_up_message).visibility,
                    equalTo(View.VISIBLE))
        }
    }

    @Test
    fun hides_not_backed_up_when_words_backedUp() {
        val scenario = setUp()
        scenario.onActivity { activity ->
            whenever(activity.cnWalletManager.hasSkippedBackup()).thenReturn(false)
        }
        scenario.moveToState(Lifecycle.State.RESUMED)

        scenario.onActivity { activity ->
            assertThat(activity.findViewById<View>(R.id.not_backed_up_message).visibility,
                    equalTo(View.GONE))
        }
    }

    @Test
    fun shows_recovery_words_help_screen() {
        val scenario = setUp()
        scenario.moveToState(Lifecycle.State.RESUMED)

        scenario.onActivity { activity ->
            activity.findViewById<View>(R.id.recover_wallet).performClick()
            val shadowActivity = shadowOf(activity)
            val startedActivity = shadowActivity.nextStartedActivity
            assertThat(startedActivity.component!!.className,
                    equalTo(BackupRecoveryWordsStartActivity::class.java.name))
        }
    }

    @Test
    fun shows_licenses() {
        val scenario = setUp()
        scenario.moveToState(Lifecycle.State.RESUMED)

        scenario.onActivity { activity ->
            activity.findViewById<View>(R.id.open_source).performClick()
            val shadowActivity = shadowOf(activity)
            val startedActivity = shadowActivity.nextStartedActivity
            assertThat(startedActivity.component!!.className,
                    equalTo(LicensesActivity::class.java.name))
        }
    }

    @Test
    fun navigates_to_splash_activity_on_Delete() {
        val scenario = setUp()
        scenario.moveToState(Lifecycle.State.RESUMED)

        scenario.onActivity { activity ->
            activity.onDeleted()
            val shadowActivity = shadowOf(activity)
            val startedActivity = shadowActivity.nextStartedActivity
            assertThat(startedActivity.component!!.className, equalTo(SplashActivity::class.java.name))
            assertThat(startedActivity.flags, equalTo(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
        }
    }

    @Test
    fun performs_authorization_for_deleting_wallet() {
        val scenario = setUp()

        scenario.moveToState(Lifecycle.State.RESUMED)

        scenario.onActivity { activity ->
            activity.findViewById<View>(R.id.delete_wallet).performClick()
            val dialog = activity.supportFragmentManager.findFragmentByTag(SettingsActivity.TAG_CONFIRM_DELETE_WALLET) as GenericAlertDialog
            val shadowActivity = shadowOf(activity)

            activity.onClick(dialog.alertDialog, DialogInterface.BUTTON_POSITIVE)

            val activityForResult = shadowActivity.nextStartedActivityForResult
            assertThat(activityForResult.requestCode, equalTo(SettingsActivity.DELETE_WALLET_REQUEST_CODE))
            assertThat(activityForResult.intent.component!!.className, equalTo(AuthorizedActionActivity::class.java.name))
            activity.onDeleted()
        }
    }

    @Test
    fun performs_delete() {
        val scenario = setUp()

        scenario.moveToState(Lifecycle.State.RESUMED)

        scenario.onActivity { activity ->
            activity.onActivityResult(SettingsActivity.DELETE_WALLET_REQUEST_CODE, AuthorizedActionActivity.RESULT_AUTHORIZED, null)

            verify(activity.deleteWalletPresenter).onDelete()
        }
    }

    @Test
    fun negative_delete_confirmation_dismisses_only() {
        val scenario = setUp()

        scenario.moveToState(Lifecycle.State.RESUMED)

        scenario.onActivity { activity ->
            activity.findViewById<View>(R.id.delete_wallet).performClick()
            val dialog = activity.supportFragmentManager.findFragmentByTag(SettingsActivity.TAG_CONFIRM_DELETE_WALLET) as GenericAlertDialog

            activity.onClick(dialog.alertDialog, DialogInterface.BUTTON_NEGATIVE)

            assertNull(activity.supportFragmentManager.findFragmentByTag(SettingsActivity.TAG_CONFIRM_DELETE_WALLET))
        }
    }

    @Test
    fun prompts_to_confirm_delete() {
        val scenario = setUp()

        scenario.moveToState(Lifecycle.State.RESUMED)

        scenario.onActivity { activity ->
            activity.findViewById<View>(R.id.delete_wallet).performClick()

            verify(activity.deleteWalletPresenter).setCallback(ArgumentMatchers.any())
            assertNotNull(activity.supportFragmentManager.findFragmentByTag(SettingsActivity.TAG_CONFIRM_DELETE_WALLET))
        }
    }

    @Test
    fun show_auth_delete_with_message() {
        val expectedAuthMessage = "Enter pin to confirm deletion of your wallet."
        val scenario = setUp()

        scenario.moveToState(Lifecycle.State.RESUMED)
        scenario.onActivity { activity ->
            activity.authorizeDelete()

            val intent = shadowOf(activity).nextStartedActivity
            val extras = intent.extras
            val authorizedActionMessage = extras!!.getString(DropbitIntents.EXTRA_AUTHORIZED_ACTION_MESSAGE)
            assertThat(authorizedActionMessage, equalTo(expectedAuthMessage))
        }
    }

    @Test
    fun renders_state_of_dust_protection_preference() {
        whenever(ApplicationProvider.getApplicationContext<TestCoinKeeperApplication>().dustProtectionPreference.isDustProtectionEnabled).thenReturn(true)
        val scenario = setUp()

        scenario.moveToState(Lifecycle.State.RESUMED)
        scenario.onActivity { activity ->
            val view = withId<Switch>(activity, R.id.dust_protection_toggle)
            assertTrue(view.isChecked)
        }
    }

    @Test
    fun observes_switch_toggle_dust_protection() {
        whenever(ApplicationProvider.getApplicationContext<TestCoinKeeperApplication>().dustProtectionPreference.isDustProtectionEnabled).thenReturn(true)
        val scenario = setUp()

        scenario.moveToState(Lifecycle.State.RESUMED)
        scenario.onActivity { activity ->
            clickOn(withId(activity, R.id.dust_protection_toggle))

            verify(activity.dustProtectionPreference).setProtection(false)
        }
    }

    @Test
    fun clicking_on_tooltip_for_dust_protection_navigates_to_website() {
        val scenario = setUp()
        scenario.onActivity { activity ->
            whenever(activity.dustProtectionPreference.isDustProtectionEnabled).thenReturn(true)
        }

        scenario.moveToState(Lifecycle.State.RESUMED)
        scenario.onActivity { activity ->
            clickOn(withId(activity, R.id.dust_protection_tooltip))

            val intent = shadowOf(activity).nextStartedActivity
            val expectedIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://dropbit.app/tooltips/dustprotection"))
            assertThat(intent, IntentMatcher.equalTo(expectedIntent))
        }
    }

    @Test
    fun `clicking yearly high subscription toggles subscription`() {
        val scenario = setUp()

        scenario.moveToState(Lifecycle.State.RESUMED)
        scenario.onActivity { activity ->
            activity.yearlyHighObserver.onChanged(true)
            clickOn(activity.findViewById<Switch>(R.id.yearly_high_subscription))
            verify(activity.yearlyHighViewModel).toggleSubscription(true)

            scenario.recreate()

            activity.yearlyHighObserver.onChanged(false)
            clickOn(activity.findViewById<Switch>(R.id.yearly_high_subscription))
            verify(activity.yearlyHighViewModel).toggleSubscription(false)
        }
    }

    @Test
    fun `loads yearly high subscription state when resumed`() {
        val scenario = setUp()

        scenario.moveToState(Lifecycle.State.RESUMED)
        scenario.onActivity { activity ->
            verify(activity.yearlyHighViewModel.isSubscribedToYearlyHigh).observe(activity, activity.yearlyHighObserver)
        }
    }

    @Test
    fun `updates yearly high when subscription changes`() {
        val scenario = setUp()

        scenario.moveToState(Lifecycle.State.RESUMED)
        scenario.onActivity { activity ->
            activity.yearlyHighObserver.onChanged(true)

            assertTrue(withId<Switch>(activity, R.id.yearly_high_subscription).isChecked)
        }
    }
}