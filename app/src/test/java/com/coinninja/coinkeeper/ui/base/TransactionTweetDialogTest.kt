package com.coinninja.coinkeeper.ui.base

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.cn.dropbit.DropbitTwitterInviteTweetSuppressionCheck
import com.coinninja.coinkeeper.model.Identity
import com.coinninja.coinkeeper.model.db.enums.IdentityType
import com.coinninja.coinkeeper.ui.twitter.TransactionTweetCallback
import com.coinninja.coinkeeper.ui.twitter.TransactionTweetDialog
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import dagger.Module
import dagger.Provides
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TransactionTweetDialogTest {
    companion object {
        private val inviteId = "--invite-id--"
    }

    private val scenario = ActivityScenario.launch(TestableActivity::class.java)

    private val suppressionCheck: DropbitTwitterInviteTweetSuppressionCheck = mock()

    private fun setupDialog(identity: Identity, callback: TransactionTweetCallback): TransactionTweetDialog {
        val dialog = TransactionTweetDialog.createInstance(inviteId, identity, callback)

        scenario.onActivity {
            dialog.show(it.supportFragmentManager, TransactionTweetDialog::class.java.simpleName)
        }
        scenario.moveToState(Lifecycle.State.RESUMED)
        return dialog
    }

    @After
    fun tearDown() {
        scenario.close()
    }

    @Test
    fun checks_to_see_if_user_should_manually_send_mention() {
        val identity = mock<Identity>()
        whenever(identity.handle).thenReturn("@receiversHandle")
        whenever(identity.avatarUrl).thenReturn("http://avatar/uri")
        val callback = mock<TransactionTweetCallback>()
        val dialog = setupDialog(identity, callback)

        verify(dialog.suppressionCheck).shouldManuallySendTwitterMention(inviteId, dialog.shouldManuallyMentionCallback)
    }

    @Test
    fun configures_for_manual_tweet() {
        val identity = mock<Identity>()
        whenever(identity.handle).thenReturn("@receiversHandle")
        whenever(identity.avatarUrl).thenReturn("http://avatar/uri")
        val callback = mock<TransactionTweetCallback>()
        val dialog = setupDialog(identity, callback)

        dialog.shouldManuallyMentionCallback.onManualShouldManuallyMention(true)

        assertThat(dialog.userSendsTweet!!.text).isEqualTo(dialog.getText(R.string.i_ll_send_the_tweet_myself))
        assertThat(dialog.dropbitSendsTweet!!.text).isEqualTo(dialog.getText(R.string.let_dropbit_send_the_tweet))

        dialog.dropbitSendsTweet!!.performClick()

        verify(dialog.suppressionCheck, times(2)).shouldManuallySendTwitterMention(inviteId, dialog.shouldManuallyMentionCallback)
    }

    @Test
    fun configures_for_no_tweet() {
        val identity = mock<Identity>()
        whenever(identity.handle).thenReturn("@receiversHandle")
        whenever(identity.avatarUrl).thenReturn("http://avatar/uri")
        val callback = mock<TransactionTweetCallback>()
        val dialog = setupDialog(identity, callback)

        dialog.shouldManuallyMentionCallback.onManualShouldManuallyMention(false)

        assertThat(dialog.userSendsTweet!!.text).isEqualTo(dialog.getText(R.string.ill_send_tweet))
        assertThat(dialog.dropbitSendsTweet!!.text).isEqualTo(dialog.getText(R.string.ok))

    }

    @Test
    fun tweet_yourself_sends_mention_to_open_twitter__configuration_no_tweet() {
        val identity = Identity(identityType = IdentityType.TWITTER, value = "", handle = "@receiversHandle", avatarUrl = "http://avatar/uri")
        val callback = mock<TransactionTweetCallback>()
        val dialog = setupDialog(identity, callback)

        dialog.shouldManuallyMentionCallback.onManualShouldManuallyMention(false)

        dialog.userSendsTweet!!.performClick()

        scenario.onActivity {
            verify(dialog.activityNavigationUtil).shareWithTwitter(it,
                    "@receiversHandle I just sent you Bitcoin using DropBit. Download the app to claim it here: https://dropbit.app/download")
        }

    }

    @Test
    fun tweet_yourself_sends_mention_to_open_twitter__configuration_manual_tweet() {
        val identity = Identity(identityType = IdentityType.TWITTER, value = "", handle = "receiversHandle", avatarUrl = "http://avatar/uri")
        val callback = mock<TransactionTweetCallback>()
        val dialog = setupDialog(identity, callback)

        dialog.shouldManuallyMentionCallback.onManualShouldManuallyMention(true)

        dialog.userSendsTweet!!.performClick()

        scenario.onActivity {
            verify(dialog.activityNavigationUtil).shareWithTwitter(it,
                    "@receiversHandle I just sent you Bitcoin using DropBit. Download the app to claim it here: https://dropbit.app/download")
        }

    }

    @Module
    class TestTransactionTweetDialogModule {

        @Provides
        fun suppressionCheck(): DropbitTwitterInviteTweetSuppressionCheck = mock()
    }

}
