package com.coinninja.coinkeeper.ui.segwit

import android.content.DialogInterface
import android.content.Intent
import android.widget.CheckBox
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.coinninja.cn.libbitcoin.model.DerivationPath
import app.coinninja.cn.libbitcoin.model.TransactionData
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.bitcoin.isFunded
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.coinkeeper.util.analytics.Analytics
import com.coinninja.coinkeeper.view.dialog.GenericAlertDialog
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import dagger.Module
import dagger.Provides
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PerformSegwitUpgradeActivityTest {

    private val creationIntent: Intent get() = Intent(ApplicationProvider.getApplicationContext(), PerformSegwitUpgradeActivity::class.java)

    private fun createScenario(intent: Intent = creationIntent)
            : ActivityScenario<PerformSegwitUpgradeActivity> = ActivityScenario.launch(intent)

    private fun createScenarioWithTransaction(): ActivityScenario<PerformSegwitUpgradeActivity> = creationIntent.let {
        it.putExtra(DropbitIntents.EXTRA_TRANSACTION_DATA, TransactionData(arrayOf(mock()),
                100_000_000, 1_000, 0,
                DerivationPath.from("M/84/0/0/1/0"), "--segwit-address--")
        )
        return createScenario(it)
    }

    private fun when_step_completes(activity: PerformSegwitUpgradeActivity, state: UpgradeState) {
        activity.onStatChangeObserver.onChanged(state)
    }

    private fun and_steps_are_unchecked(vararg steps: CheckBox) {
        steps.forEach {
            assertWithMessage("Is Checkbox checked? ${it.isChecked}").that(it.isChecked).isFalse()
        }
    }

    private fun then_steps_are_checked(vararg steps: CheckBox) {
        steps.forEach {
            assertWithMessage("Is Checkbox checked? ${it.isChecked}").that(it.isChecked).isTrue()
        }
    }

    @Test
    fun starts_appropriately() {
        val scenario = createScenario()
        scenario.onActivity { activity ->
            assertThat(activity.walletUpgradeViewModel).isNotNull()
            assertThat(activity.state).isEqualTo(UpgradeState.Started)
            verify(activity.walletUpgradeViewModel.upgradeState).observe(activity, activity.onStatChangeObserver)
            verify(activity.walletUpgradeViewModel).performStepOne()
            and_steps_are_unchecked(activity.stepOneCheckBox, activity.stepTwoCheckBox, activity.stepThreeCheckBox)
            verify(activity.analytics).setUserProperty(Analytics.PROPERTY_LIGHTNING_UPGRADE_STARTED, true)
            verify(activity.analytics).setUserProperty(Analytics.PROPERTY_LIGHTNING_UPGRADE_COMPLETED, false)
        }
        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun updates_checkbox_and_starts_next_step__step_one_complete() {
        val scenario = createScenario()
        scenario.onActivity { activity ->
            when_step_completes(activity, UpgradeState.StepOneCompleted)

            then_steps_are_checked(activity.stepOneCheckBox)
            and_steps_are_unchecked(activity.stepTwoCheckBox, activity.stepThreeCheckBox)
            verify(activity.walletUpgradeViewModel).performStepTwo()
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun updates_checkbox_and_starts_next_step__step_two_complete__without_transaction_data() {
        val scenario = createScenario()
        scenario.onActivity { activity ->
            assertThat(activity.transactionData).isNull()
            when_step_completes(activity, UpgradeState.StepTwoCompleted)
            then_steps_are_checked(activity.stepOneCheckBox, activity.stepTwoCheckBox)
            and_steps_are_unchecked(activity.stepThreeCheckBox)
            verify(activity.walletUpgradeViewModel).performStepThree(null)
            verify(activity.analytics).setUserProperty(Analytics.PROPERTY_LIGHTNING_UPGRADE_WITH_FUNDS, false)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun updates_checkbox_and_starts_next_step__step_two_complete__with_transaction_data() {
        val scenario = createScenarioWithTransaction()
        scenario.onActivity { activity ->
            assertThat(activity.transactionData!!.isFunded()).isTrue()
            when_step_completes(activity, UpgradeState.StepTwoCompleted)

            then_steps_are_checked(activity.stepOneCheckBox, activity.stepTwoCheckBox)
            and_steps_are_unchecked(activity.stepThreeCheckBox)
            verify(activity.walletUpgradeViewModel).performStepThree(activity.transactionData)
            verify(activity.analytics).setUserProperty(Analytics.PROPERTY_LIGHTNING_UPGRADE_WITH_FUNDS, true)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun updates_checkbox_and_starts_next_step__step_three_complete() {
        val scenario = createScenario()
        scenario.onActivity { activity ->
            when_step_completes(activity, UpgradeState.StepThreeCompleted)

            then_steps_are_checked(activity.stepOneCheckBox, activity.stepTwoCheckBox, activity.stepThreeCheckBox)
            verify(activity.walletUpgradeViewModel).cleanUp()
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun navigates_to_success_when_completed() {
        val scenario = createScenario()
        scenario.onActivity { activity ->
            when_step_completes(activity, UpgradeState.Finished)

            verify(activity.activityNavigationUtil).navigateToUpgradeToSegwitSuccess(activity)
            verify(activity.analytics).setUserProperty(Analytics.PROPERTY_LIGHTNING_UPGRADE_COMPLETED, true)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun shows_error_message_when_process_errors() {
        val scenario = createScenario()
        scenario.onActivity { activity ->
            when_step_completes(activity, UpgradeState.Error)

            val dialog = activity.supportFragmentManager.findFragmentByTag(PerformSegwitUpgradeActivity.errorDialogTag) as GenericAlertDialog

            assertThat(dialog).isNotNull()
            assertThat(dialog.message).isEqualTo(activity.getString(R.string.upgrade_failed_message))

            dialog.alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick()

            assertThat(activity.isFinishing).isTrue()
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Module
    class PerformSegwitUpgradeActivityTestModule {

        @Provides
        fun walletUpgradeModelProvider(): WalletUpgradeModelProvider {
            val walletUpgradeViewModel: WalletUpgradeViewModel = mock()
            val provider: WalletUpgradeModelProvider = mock()
            whenever(provider.provide(any())).thenReturn(walletUpgradeViewModel)
            whenever(walletUpgradeViewModel.upgradeState).thenReturn(mock())
            return provider
        }
    }
}
