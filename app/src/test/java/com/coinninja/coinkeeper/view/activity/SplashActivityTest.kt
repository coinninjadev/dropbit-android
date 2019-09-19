package com.coinninja.coinkeeper.view.activity

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.coinkeeper.receiver.StartupCompleteReceiver
import com.coinninja.coinkeeper.util.DropbitIntents
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric

@RunWith(AndroidJUnit4::class)
class SplashActivityTest {

    private fun createScenario(): ActivityScenario<SplashActivity> = ActivityScenario.launch(SplashActivity::class.java)

    @Test
    fun startsStartActivityWhenRecoveryWordsAreNotSaved() {
        val scenario = createScenario()
        scenario.onActivity { activity ->
            Robolectric.flushForegroundThreadScheduler()

            verify(activity.activityNavigationUtil).navigateToStartActivity(activity)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun broadcasts_start_up__with_backed_up_wallet() {
        val scenario = createScenario()
        scenario.onActivity { activity ->
            verify(activity.localBroadCastUtil).sendGlobalBroadcast(StartupCompleteReceiver::class.java,
                    DropbitIntents.ACTION_ON_APPLICATION_FOREGROUND_STARTUP)

        }
        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun starts_wallet_upgrade_process_when_non_segwit_wallet() {
        val scenario = createScenario()
        scenario.onActivity { activity ->
            whenever(activity.cnWalletManager.hasWallet).thenReturn(true)
            whenever(activity.cnWalletManager.isSegwitUpgradeRequired).thenReturn(true)

            Robolectric.flushForegroundThreadScheduler()

            verify(activity.activityNavigationUtil).navigateToUpgradeToSegwit(activity!!)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun starts_home_activity_when_recovery_words_are_saved_with_segwit_wallet() {
        val scenario = createScenario()
        scenario.onActivity { activity ->
            whenever(activity.cnWalletManager.hasWallet).thenReturn(true)
            whenever(activity.cnWalletManager.isSegwitUpgradeRequired).thenReturn(false)

            Robolectric.flushForegroundThreadScheduler()

            verify(activity.activityNavigationUtil).navigateToHome(activity!!)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun clears_auth() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            verify(activity.authentication).forceDeAuthenticate()
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }
}