package com.coinninja.coinkeeper.ui.settings

import android.content.DialogInterface
import android.content.Intent
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
import com.coinninja.coinkeeper.util.analytics.Analytics
import com.coinninja.coinkeeper.view.activity.AuthorizedActionActivity
import com.coinninja.coinkeeper.view.activity.BackupActivity
import com.coinninja.coinkeeper.view.activity.LicensesActivity
import com.coinninja.coinkeeper.view.activity.SplashActivity
import com.coinninja.coinkeeper.view.dialog.GenericAlertDialog
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.robolectric.Shadows.shadowOf

@Suppress("UNCHECKED_CAST")
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

    fun createScenario(): ActivityScenario<SettingsActivity> {
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
        val scenario = createScenario()
        scenario.moveToState(Lifecycle.State.RESUMED)

        scenario.onActivity { activity ->
            assertThat(activity.findViewById<View>(R.id.not_backed_up_message).visibility).isEqualTo(View.VISIBLE)
        }
        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun hides_not_backed_up_when_words_backedUp() {
        val scenario = createScenario()
        scenario.onActivity { activity ->
            whenever(activity.cnWalletManager.hasSkippedBackup()).thenReturn(false)
        }
        scenario.moveToState(Lifecycle.State.RESUMED)

        scenario.onActivity { activity ->
            assertThat(activity.findViewById<View>(R.id.not_backed_up_message).visibility).isEqualTo(View.GONE)
        }
        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun shows_recovery_words_help_screen() {
        val scenario = createScenario()
        scenario.moveToState(Lifecycle.State.RESUMED)

        scenario.onActivity { activity ->
            activity.findViewById<View>(R.id.recover_wallet).performClick()
            val shadowActivity = shadowOf(activity)
            val startedActivity = shadowActivity.nextStartedActivity
            assertThat(startedActivity.component!!.className).isEqualTo(BackupRecoveryWordsStartActivity::class.java.name)
        }
        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun shows_licenses() {
        val scenario = createScenario()
        scenario.moveToState(Lifecycle.State.RESUMED)

        scenario.onActivity { activity ->
            activity.findViewById<View>(R.id.open_source).performClick()
            val shadowActivity = shadowOf(activity)
            val startedActivity = shadowActivity.nextStartedActivity
            assertThat(startedActivity.component!!.className).isEqualTo(LicensesActivity::class.java.name)
        }
        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun navigates_to_splash_activity_on_Delete() {
        val scenario = createScenario()
        scenario.moveToState(Lifecycle.State.RESUMED)

        scenario.onActivity { activity ->
            activity.onDeleted()
            val shadowActivity = shadowOf(activity)
            val startedActivity = shadowActivity.nextStartedActivity
            assertThat(startedActivity.component!!.className).isEqualTo(SplashActivity::class.java.name)
            assertThat(startedActivity.flags).isEqualTo(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun performs_authorization_for_deleting_wallet() {
        val scenario = createScenario()

        scenario.moveToState(Lifecycle.State.RESUMED)

        scenario.onActivity { activity ->
            activity.findViewById<View>(R.id.delete_wallet).performClick()
            val dialog = activity.supportFragmentManager.findFragmentByTag(SettingsActivity.TAG_CONFIRM_DELETE_WALLET) as GenericAlertDialog
            val shadowActivity = shadowOf(activity)

            activity.onClick(dialog.alertDialog, DialogInterface.BUTTON_POSITIVE)

            val activityForResult = shadowActivity.nextStartedActivityForResult
            assertThat(activityForResult.requestCode).isEqualTo(SettingsActivity.DELETE_WALLET_REQUEST_CODE)
            assertThat(activityForResult.intent.component!!.className).isEqualTo(AuthorizedActionActivity::class.java.name)
            activity.onDeleted()
        }
        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun performs_delete() {
        val scenario = createScenario()

        scenario.moveToState(Lifecycle.State.RESUMED)

        scenario.onActivity { activity ->
            activity.onActivityResult(SettingsActivity.DELETE_WALLET_REQUEST_CODE, AuthorizedActionActivity.RESULT_AUTHORIZED, null)

            verify(activity.deleteWalletPresenter).onDelete()
        }
    }

    @Test
    fun negative_delete_confirmation_dismisses_only() {
        val scenario = createScenario()

        scenario.moveToState(Lifecycle.State.RESUMED)

        scenario.onActivity { activity ->
            activity.findViewById<View>(R.id.delete_wallet).performClick()
            val dialog = activity.supportFragmentManager.findFragmentByTag(SettingsActivity.TAG_CONFIRM_DELETE_WALLET) as GenericAlertDialog

            activity.onClick(dialog.alertDialog, DialogInterface.BUTTON_NEGATIVE)

            assertNull(activity.supportFragmentManager.findFragmentByTag(SettingsActivity.TAG_CONFIRM_DELETE_WALLET))
        }
        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun prompts_to_confirm_delete() {
        val scenario = createScenario()

        scenario.moveToState(Lifecycle.State.RESUMED)

        scenario.onActivity { activity ->
            activity.findViewById<View>(R.id.delete_wallet).performClick()

            verify(activity.deleteWalletPresenter).setCallback(ArgumentMatchers.any())
            assertNotNull(activity.supportFragmentManager.findFragmentByTag(SettingsActivity.TAG_CONFIRM_DELETE_WALLET))
        }
        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun show_auth_delete_with_message() {
        val expectedAuthMessage = "Enter pin to confirm deletion of your wallet."
        val scenario = createScenario()

        scenario.moveToState(Lifecycle.State.RESUMED)
        scenario.onActivity { activity ->
            activity.authorizeDelete()

            val intent = shadowOf(activity).nextStartedActivity
            val extras = intent.extras
            val authorizedActionMessage = extras!!.getString(DropbitIntents.EXTRA_AUTHORIZED_ACTION_MESSAGE)
            assertThat(authorizedActionMessage).isEqualTo(expectedAuthMessage)
        }
        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun renders_state_of_dust_protection_preference() {
        whenever(ApplicationProvider.getApplicationContext<TestCoinKeeperApplication>().dustProtectionPreference.isDustProtectionEnabled).thenReturn(true)
        val scenario = createScenario()

        scenario.moveToState(Lifecycle.State.RESUMED)
        scenario.onActivity { activity ->
            val view = withId<Switch>(activity, R.id.dust_protection_toggle)
            assertTrue(view!!.isChecked)
        }
        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun observes_switch_toggle_dust_protection() {
        whenever(ApplicationProvider.getApplicationContext<TestCoinKeeperApplication>().dustProtectionPreference.isDustProtectionEnabled).thenReturn(true)
        val scenario = createScenario()

        scenario.moveToState(Lifecycle.State.RESUMED)
        scenario.onActivity { activity ->
            clickOn(withId(activity, R.id.dust_protection_toggle))

            verify(activity.dustProtectionPreference).setProtection(false)
        }
        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun clicking_on_tooltip_for_dust_protection_navigates_to_website() {
        val scenario = createScenario()
        scenario.onActivity { activity ->
            whenever(activity.dustProtectionPreference.isDustProtectionEnabled).thenReturn(true)
        }

        scenario.moveToState(Lifecycle.State.RESUMED)
        scenario.onActivity { activity ->
            clickOn(withId(activity, R.id.dust_protection_tooltip))

            val intent = shadowOf(activity).nextStartedActivity
            assertThat(intent.action).isEqualTo(Intent.ACTION_VIEW)
            assertThat(intent.data.toString()).isEqualTo("https://dropbit.app/tooltips/dustprotection")
        }
        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun clicking_yearly_high_subscription_toggles_subscription() {
        val scenario = createScenario()

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
        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun loads_yearly_high_subscription_state_when_resumed() {
        val scenario = createScenario()

        scenario.moveToState(Lifecycle.State.RESUMED)
        scenario.onActivity { activity ->
            verify(activity.yearlyHighViewModel.isSubscribedToYearlyHigh).observe(activity, activity.yearlyHighObserver)
        }
        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun updates_yearly_high_when_subscription_changes() {
        val scenario = createScenario()

        scenario.moveToState(Lifecycle.State.RESUMED)
        scenario.onActivity { activity ->
            activity.yearlyHighObserver.onChanged(true)

            assertThat(withId<Switch>(activity, R.id.yearly_high_subscription)!!.isChecked).isTrue()
        }
        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun shows_legacy_words_when_user_has_segwit_and_legacy() {
        val scenario = createScenario()
        scenario.onActivity { activity ->
            // Gone when no legacy wallet
            assertThat(activity.legacyWalletButton.visibility).isEqualTo(View.GONE)
            assertThat(activity.legacyWalletDivider.visibility).isEqualTo(View.GONE)

            // Visible when legacy wallet exists
            whenever(activity.cnWalletManager.hasLegacyWallet).thenReturn(true)
            activity.setupLegacyWallet()

            assertThat(activity.legacyWalletButton.visibility).isEqualTo(View.VISIBLE)
            assertThat(activity.legacyWalletDivider.visibility).isEqualTo(View.VISIBLE)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun clicking_on_legacy_wallet_shows_authroizes_action() {
        val scenario = createScenario()
        scenario.onActivity { activity ->
            whenever(activity.cnWalletManager.hasLegacyWallet).thenReturn(true)
            activity.setupLegacyWallet()

            activity.legacyWalletButton.performClick()
            val intentForResult = shadowOf(activity).peekNextStartedActivityForResult()

            assertThat(intentForResult).isNotNull()
            assertThat(intentForResult.requestCode).isEqualTo(SettingsActivity.legacyWalletRequestCode)
            assertThat(intentForResult.intent.component.className).isEqualTo(AuthorizedActionActivity::class.java.name)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun shows_legacy_words_once_authorized() {
        val scenario = createScenario()
        scenario.onActivity { activity ->
            val words: Array<String> = arrayOf("word", "word", "word", "word", "word", "word", "word", "word", "word", "word", "word", "word")
            whenever(activity.cnWalletManager.legacyWords).thenReturn(words)

            activity.onActivityResult(SettingsActivity.legacyWalletRequestCode, AuthorizedActionActivity.RESULT_AUTHORIZED, null)

            val intent = shadowOf(activity).peekNextStartedActivity()
            assertThat(intent.component.className).isEqualTo(BackupActivity::class.java.name)
            assertThat(intent.getStringArrayExtra(DropbitIntents.EXTRA_RECOVERY_WORDS)).isEqualTo(words)
            assertThat(intent.getIntExtra(DropbitIntents.EXTRA_VIEW_STATE, -1)).isEqualTo(DropbitIntents.EXTRA_VIEW)
            verify(activity.analytics).trackEvent(Analytics.EVENT_VIEW_LEGACY_WORDS)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }
}