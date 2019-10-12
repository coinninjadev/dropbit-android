package com.coinninja.coinkeeper.view.activity

import android.content.DialogInterface
import android.content.Intent
import android.view.View
import android.widget.Button
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.dropbit.commons.util.isNotNull
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.TestCoinKeeperApplication
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.coinkeeper.util.analytics.Analytics
import com.coinninja.coinkeeper.view.dialog.GenericAlertDialog
import com.coinninja.matchers.IntentFilterSubject.Companion.assertThatIntentFilter
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecoverWalletActivityTest {
    companion object {
        private var invalidWords: Array<String> = arrayOf("word1", "word2", "word3", "word4", "word5", "word6",
                "word7", "word8", "word9", "word10", "word11", "word12")
        private var validWords: Array<String> = arrayOf("mickeymouse", "mickeymouse", "mickeymouse", "mickeymouse", "mickeymouse", "mickeymouse",
                "mickeymouse", "mickeymouse", "mickeymouse", "mickeymouse", "mickeymouse", "mickeymouse")
    }


    private fun createScenario(creationIntent: Intent): ActivityScenario<RecoverWalletActivity> {
        val app = ApplicationProvider.getApplicationContext<TestCoinKeeperApplication>()

        app.bitcoinUtil = mock()

        whenever(app.bitcoinUtil.isValidBIP39Words(validWords)).thenReturn(true)
        whenever(app.bitcoinUtil.isValidBIP39Words(invalidWords)).thenReturn(false)
        whenever(app.cnServiceConnection.cnWalletServicesInterface).thenReturn(mock())

        return ActivityScenario.launch(creationIntent)
    }

    private fun createWithValidWords(): ActivityScenario<RecoverWalletActivity> {
        return Intent(ApplicationProvider.getApplicationContext(), RecoverWalletActivity::class.java).let {
            it.putExtra(DropbitIntents.EXTRA_RECOVERY_WORDS, validWords)
            createScenario(it)
        }
    }

    private fun createWithInvalidWords(): ActivityScenario<RecoverWalletActivity> {
        return Intent(ApplicationProvider.getApplicationContext(), RecoverWalletActivity::class.java).let {
            it.putExtra(DropbitIntents.EXTRA_RECOVERY_WORDS, invalidWords)
            createScenario(it)
        }
    }

    @Test
    fun saving_words_shows_success() {
        val scenario = createWithValidWords()

        scenario.onActivity { activity ->
            val nextButton = activity.nextButton

            activity.showSuccess()

            assertThat(nextButton.text).isEqualTo(activity.getString(R.string.recover_wallet_success_button_text))
            assertThat((activity.title).text).isEqualTo(activity.getString(R.string.recover_wallet_success_title))
            assertThat((activity.message).text).isEqualTo(activity.getString(R.string.recover_wallet_success_message))
            assertThat(activity.closeButton.visibility).isEqualTo(View.GONE)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun successful_recovery_clicking_going_to_wallet_goes_to_next() {
        val scenario = createWithValidWords()

        scenario.onActivity { activity ->
            val nextButton = activity.nextButton
            activity.showSuccess()

            nextButton.performClick()

            verify(activity.activityNavigationUtil).showVerificationActivity(activity)
            assertThat(activity.isFinishing).isTrue()
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun close_starts_over_at_start_screen() {
        val scenario = createWithInvalidWords()

        scenario.onActivity { activity ->

            activity.closeButton.performClick()

            assertThat(activity.closeButton.visibility).isEqualTo(View.VISIBLE)
            assertThat(activity.isFinishing).isTrue()

            verify(activity.activityNavigationUtil).navigateToStartActivity(activity)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun starting_over_navigates_back_to_restore_wallet() {
        val scenario = createWithInvalidWords()

        scenario.onActivity { activity ->

            val nextButton: Button = activity.nextButton

            nextButton.performClick()

            assertThat(nextButton.text).isEqualTo(activity.getString(R.string.recover_wallet_error_button_text))
            verify(activity.activityNavigationUtil).navigateToRestoreWallet(activity)
            assertThat(activity.isFinishing).isTrue()
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun words_not_in_list_is_error() {
        val scenario = createWithInvalidWords()

        scenario.onActivity { activity ->

            assertThat(activity.title.text).isEqualTo(activity.getText(R.string.recover_wallet_error_title))
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun error_when_no_words_provided() {
        val scenario =
                createScenario(Intent(ApplicationProvider.getApplicationContext(), RecoverWalletActivity::class.java))

        scenario.onActivity { activity ->

            assertThat(activity.title.text).isEqualTo(activity.getText(R.string.recover_wallet_error_title))
            assertThat(activity.message.text).isEqualTo(activity!!.getText(R.string.recover_wallet_error_message))
            assertThat(activity.closeButton.visibility).isEqualTo(View.VISIBLE)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun on_resume_reg_for_save_recovery_words_local_broadcast_test() {
        val scenario = createWithValidWords()

        scenario.onActivity { activity ->
            assertThatIntentFilter(activity.intentFilter).containsAction(DropbitIntents.ACTION_ON_SERVICE_CONNECTION_BOUNDED)
            assertThatIntentFilter(activity.intentFilter).containsAction(DropbitIntents.ACTION_SAVE_RECOVERY_WORDS)
            assertThatIntentFilter(activity.intentFilter).containsAction(DropbitIntents.ACTION_UNABLE_TO_SAVE_RECOVERY_WORDS)
            assertThatIntentFilter(activity.intentFilter).containsAction(DropbitIntents.ACTION_WALLET_ALREADY_UPGRADED)
            assertThatIntentFilter(activity.intentFilter).containsAction(DropbitIntents.ACTION_WALLET_REQUIRES_UPGRADE)
            assertThatIntentFilter(activity.intentFilter).containsAction(DropbitIntents.ACTION_WALLET_REGISTRATION_COMPLETE)
            verify(activity.localBroadCastUtil).registerReceiver(activity.receiver, activity.intentFilter)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun on_pause_unregister_for_local_broadcast_test() {
        val scenario = createWithValidWords()


        scenario.onActivity { activity ->
            val receiver = activity.receiver
            val localBroadCastUtil = activity.localBroadCastUtil

            scenario.moveToState(Lifecycle.State.DESTROYED)

            verify(localBroadCastUtil).unregisterReceiver(receiver)
        }
        scenario.close()
    }

    @Test
    fun onCreate_binds_to_CNWalletService_test() {
        val scenario = createWithValidWords()

        scenario.onActivity { activity ->
            verify(activity.serviceWorkUtil).bindToCNWalletService(activity.cnServiceConnection)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun onResume_do_not_do_anything_if_not_bonded_test() {
        val scenario = createWithValidWords()

        scenario.onActivity { activity ->
            whenever(activity.cnServiceConnection.isBounded).thenReturn(false)
            whenever(activity.cnServiceConnection.cnWalletServicesInterface).thenReturn(mock())

            scenario.moveToState(Lifecycle.State.RESUMED)

            verify(activity.cnServiceConnection.cnWalletServicesInterface, times(0))!!.saveSeedWords(validWords)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun on_local_broadcast_CONNECTION_BOUNDED_startSaveRecoveryWordsService_test() {
        val scenario = createWithValidWords()

        scenario.onActivity { activity ->
            whenever(activity.cnServiceConnection.isBounded).thenReturn(true)
            whenever(activity.cnServiceConnection.cnWalletServicesInterface).thenReturn(mock())

            activity.receiver.onReceive(activity, Intent(DropbitIntents.ACTION_ON_SERVICE_CONNECTION_BOUNDED))

            verify(activity.cnServiceConnection.cnWalletServicesInterface)!!.saveSeedWords(validWords)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun show_success_when_a_local_broadcast_event_happens_of_save_recovery_success_test() {
        val scenario = createWithValidWords()

        scenario.onActivity { activity ->

            activity.receiver.onReceive(activity, Intent(DropbitIntents.ACTION_SAVE_RECOVERY_WORDS))

            assertThat(activity.icon.tag).isEqualTo(R.drawable.ic_restore_success)
            assertThat(activity.title.text).isEqualTo(activity.getString(R.string.recover_wallet_success_title))
            assertThat(activity.nextButton.text).isEqualTo(activity.getString(R.string.recover_wallet_success_button_text))
            assertThat(activity.message.currentTextColor).isEqualTo(activity.resources.getColor(R.color.font_default))
            assertThat(activity.message.text).isEqualTo(activity.getString(R.string.recover_wallet_success_message))
            assertThat(activity.closeButton.visibility).isEqualTo(View.GONE)
            assertThat(activity.nextButton.isEnabled).isFalse()
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun show_fail_when_a_local_broadcast_event_happens_of_save_recovery_fail_test() {
        val scenario = createWithValidWords()

        scenario.onActivity { activity ->

            activity.receiver.onReceive(activity, Intent(DropbitIntents.ACTION_UNABLE_TO_SAVE_RECOVERY_WORDS))

            assertThat(activity.icon.tag).isEqualTo(R.drawable.ic_restore_fail)
            assertThat(activity.title.text).isEqualTo(activity.getText(R.string.recover_wallet_error_title))
            assertThat(activity.nextButton.text).isEqualTo(activity.getString(R.string.recover_wallet_error_button_text))
            assertThat(activity.message.currentTextColor).isEqualTo(activity.getColor(R.color.color_error))
            assertThat(activity.message.text).isEqualTo(activity.getString(R.string.recover_wallet_error_message))
            assertThat(activity.closeButton.visibility).isEqualTo(View.VISIBLE)
            assertThat(activity.nextButton.isEnabled).isTrue()
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()

    }

    @Test
    fun show_fail_when_recover_words_are_invalid_test() {
        val scenario = createWithInvalidWords()

        scenario.onActivity { activity ->

            activity.receiver.onReceive(activity, Intent(DropbitIntents.ACTION_UNABLE_TO_SAVE_RECOVERY_WORDS))

            assertThat(activity.icon.tag).isEqualTo(R.drawable.ic_restore_fail)
            assertThat(activity.title.text).isEqualTo(activity.getText(R.string.recover_wallet_error_title))
            assertThat(activity.nextButton.text).isEqualTo(activity.getString(R.string.recover_wallet_error_button_text))
            assertThat(activity.message.currentTextColor).isEqualTo(activity.getColor(R.color.color_error))
            assertThat(activity.message.text).isEqualTo(activity.getString(R.string.recover_wallet_error_message))
            assertThat(activity.closeButton.visibility).isEqualTo(View.VISIBLE)
            assertThat(activity.nextButton.isEnabled).isTrue()
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun disables_next_button_until_wallet_registration_completes() {
        val scenario = createWithValidWords()

        scenario.onActivity { activity ->
            assertThat(activity.nextButton.isEnabled).isFalse()

            activity.receiver.onReceive(activity, Intent(DropbitIntents.ACTION_WALLET_REGISTRATION_COMPLETE))

            assertThat(activity.nextButton.isEnabled).isTrue()
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun wipes_wallet_and_notifies_user_that_wallet_has_already_been_upgraded() {
        val scenario = createWithValidWords()

        scenario.onActivity { activity ->
            activity.receiver.onReceive(activity, Intent(DropbitIntents.ACTION_WALLET_ALREADY_UPGRADED))

            verify(activity.serviceWorkUtil).deleteWallet()

            val dialog = activity.supportFragmentManager.findFragmentByTag(RecoverWalletActivity.dialogTag) as GenericAlertDialog
            assertThat(dialog.isNotNull())
            assertThat(dialog.message).isEqualTo(activity.getString(R.string.recovered_wallet_already_upgraded))

            //click on action returns back to start screen
            dialog.alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick()
            verify(activity.activityNavigationUtil).navigateToStartActivity(activity)
            verify(activity.analytics).trackEvent(Analytics.ENTERED_DEACTIVATED_WORDS)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun notifies_user_that_wallet_requires_upgrade_and_navigates_to_perform_upgrade() {
        val scenario = createWithValidWords()

        scenario.onActivity { activity ->
            activity.receiver.onReceive(activity, Intent(DropbitIntents.ACTION_WALLET_REQUIRES_UPGRADE))

            val dialog = activity.supportFragmentManager.findFragmentByTag(RecoverWalletActivity.dialogTag) as GenericAlertDialog
            assertThat(dialog.isNotNull())
            assertThat(dialog.message).isEqualTo(activity.getString(R.string.recovered_wallet_requires_upgrade))

            //click on action returns back to start screen
            dialog.alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick()
            verify(activity.activityNavigationUtil).navigateToUpgradeToSegwit(activity)
            verify(activity.analytics).setUserProperty(Analytics.PROPERTY_LIGHTNING_UPGRADE_FROM_RESTORE, true)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }
}