package com.coinninja.coinkeeper.ui.segwit

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UpgradeToSegwitCompleteActivityTest {

    private fun createScenario():ActivityScenario<UpgradeToSegwitCompleteActivity>
            = ActivityScenario.launch(UpgradeToSegwitCompleteActivity::class.java)

    @Test
    fun back_button_navigates_user_to_home() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            activity.onBackPressed()

            verify(activity.activityNavigationUtil).navigateToHome(activity)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun view_wallet_navigates_user_to_home() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            activity.viewWalletButton.performClick()

            verify(activity.activityNavigationUtil).navigateToHome(activity)

        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun get_new_words_shows_words_for_backup() {
        val scenario = createScenario()

        scenario.onActivity { activity ->

            activity.viewRecoveryWords.performClick()

            verify(activity.activityNavigationUtil).navigateToBackupRecoveryWords(activity)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }


}