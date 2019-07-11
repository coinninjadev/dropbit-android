package com.coinninja.coinkeeper.ui.account.verify

import android.content.DialogInterface
import android.content.Intent
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.testing.FragmentScenario
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.android.helpers.Resources.getString
import com.coinninja.android.helpers.Views.clickOn
import com.coinninja.android.helpers.Views.withId
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.TestCoinKeeperApplication
import com.coinninja.coinkeeper.model.db.DropbitMeIdentity
import com.coinninja.coinkeeper.model.db.enums.IdentityType
import com.coinninja.coinkeeper.model.helpers.DropbitAccountHelper
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.Assert.assertNull
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.robolectric.shadows.ShadowDialog
import org.robolectric.shadows.ShadowToast

@RunWith(AndroidJUnit4::class)
class PhoneIdentityFragmentTest {

    private val application = ApplicationProvider.getApplicationContext<TestCoinKeeperApplication>().also { app ->
        app.dropbitAccountHelper = mock(DropbitAccountHelper::class.java)
        app.activityNavigationUtil = mock(ActivityNavigationUtil::class.java)
    }

    private fun startScenario(): FragmentScenario<PhoneIdentityFragment> {
        return FragmentScenario.launch(PhoneIdentityFragment::class.java, null,
                androidx.appcompat.R.style.Base_Theme_AppCompat, null)
    }

    @Test
    fun `configures for phone verification when user is verified but phone is not`() {
        whenever(application.dropbitAccountHelper.isPhoneVerified).thenReturn(false)
        val scenario = startScenario()

        scenario.onFragment { fragment ->
            val verificationButton = fragment.getVerificationButton()!!
            assertThat(verificationButton.text.toString(), equalTo(fragment.getString(R.string.verify_phone_button_title)))
            assertThat(verificationButton.visibility, equalTo(View.VISIBLE))

            val identityView = fragment.getIdentityView()!!
            assertThat(identityView.visibility, equalTo(View.GONE))

            val removeVerificationView = fragment.getRemoveVerificationView()!!
            assertThat(removeVerificationView.visibility, equalTo(View.GONE))
        }
    }

    @Test
    fun `shows verified when phone is verified`() {
        whenever(application.dropbitAccountHelper.isPhoneVerified).thenReturn(true)
        val identity = mock(DropbitMeIdentity::class.java)
        whenever(identity.identity).thenReturn("+13305551111")
        whenever(identity.type).thenReturn(IdentityType.PHONE)
        whenever(application.dropbitAccountHelper.phoneIdentity()).thenReturn(identity)
        val scenario = startScenario()

        scenario.onFragment { fragment ->
            val verificationButton = fragment.getVerificationButton()!!
            val identityView = fragment.getIdentityView()!!
            val removeVerificationView = fragment.getRemoveVerificationView()!!
            assertThat(verificationButton.text.toString(), equalTo(fragment.getString(R.string.verify_phone_button_title)))
            assertThat(verificationButton.visibility, equalTo(View.GONE))
            assertThat(identityView.getVerifiedAccountName(), equalTo("(330) 555-1111"))
            assertThat(identityView.visibility, equalTo(View.VISIBLE))
            assertThat(removeVerificationView.visibility, equalTo(View.VISIBLE))
        }

    }

    @Test
    fun `stops observing receiver calls when stopped`() {
        val scenario = startScenario()

        scenario.onFragment { fragment ->
            val receiver = fragment.receiver

            scenario.moveToState(Lifecycle.State.DESTROYED)

            verify(application.localBroadCastUtil).unregisterReceiver(receiver)
        }
    }

    @Test
    fun `navigates to verify phone when verify clicked`() {
        whenever(application.dropbitAccountHelper.isPhoneVerified).thenReturn(false)
        val scenario = startScenario()

        scenario.onFragment { fragment ->
            clickOn(withId(fragment.view, R.id.verify_button))
        }

        verify(application.activityNavigationUtil).navigateToRegisterPhone(any())
    }

    @Test
    fun `prompts to deVerify phone`() {
        whenever(application.dropbitAccountHelper.isPhoneVerified).thenReturn(true)
        val identity = mock(DropbitMeIdentity::class.java)
        whenever(identity.identity).thenReturn("+13305551111")
        whenever(identity.type).thenReturn(IdentityType.PHONE)
        whenever(application.dropbitAccountHelper.phoneIdentity()).thenReturn(identity)
        val scenario = startScenario()


        scenario.onFragment { fragment ->
            clickOn(withId(fragment.view, R.id.remove_verification))
        }

        val dialog = ShadowDialog.getLatestDialog() as AlertDialog
        assertThat(dialog.findViewById<TextView>(R.id.warning)!!.text.toString(),
                equalTo(application.getString(R.string.deverification_dialog_pending_dropbit_canceled_warning_message)))
        assertThat(dialog.findViewById<TextView>(R.id.message)!!.text.toString(),
                equalTo(application.getString(R.string.deverification_dialog_pending_dropbit_canceled_message)))

    }

    @Test
    fun `accepting idea of deVerifing prompts to confirm`() {
        whenever(application.dropbitAccountHelper.isPhoneVerified).thenReturn(true)
        val identity = mock(DropbitMeIdentity::class.java)
        whenever(identity.identity).thenReturn("+13305551111")
        whenever(identity.type).thenReturn(IdentityType.PHONE)
        whenever(application.dropbitAccountHelper.phoneIdentity()).thenReturn(identity)
        val scenario = startScenario()
        scenario.onFragment { fragment ->
            clickOn(withId(fragment.view, R.id.remove_verification))
        }

        val dialog = ShadowDialog.getLatestDialog() as AlertDialog
        clickOn(withId(dialog, R.id.ok))

        val confirmation = ShadowDialog.getLatestDialog() as AlertDialog

        assertThat(confirmation.findViewById<TextView>(android.R.id.message)!!.text.toString(),
                equalTo(application.getString(R.string.deverification_message_are_you_sure)))
    }

    @Test
    fun `accepting follow up performs deVerification`() {
        whenever(application.dropbitAccountHelper.isPhoneVerified).thenReturn(true)
        val identity = mock(DropbitMeIdentity::class.java)
        whenever(identity.identity).thenReturn("+13305551111")
        whenever(identity.type).thenReturn(IdentityType.PHONE)
        whenever(application.dropbitAccountHelper.phoneIdentity()).thenReturn(identity)
        val scenario = startScenario()
        scenario.onFragment { fragment ->
            clickOn(withId(fragment.view, R.id.remove_verification))
        }

        val dialog = ShadowDialog.getLatestDialog() as AlertDialog
        clickOn(withId(dialog, R.id.ok))

        val confirmation = ShadowDialog.getLatestDialog() as AlertDialog
        confirmation.getButton(DialogInterface.BUTTON_POSITIVE).performClick()

        scenario.onFragment { fragment ->
            assertNull(fragment.activity?.supportFragmentManager?.findFragmentByTag("CONFIRM_DEVERIFICATION_NOTICE"))
            assertNull(fragment.activity?.supportFragmentManager?.findFragmentByTag("CONFIRM_DEVERIFICATION_CONFIRMATION_NOTICE"))
        }

        verify(application.serviceWorkUtil).deVerifyPhoneNumber()
        scenario.onFragment { fragment ->
            verify(application.localBroadCastUtil).registerReceiver(fragment.receiver, fragment.intentFilter)
        }
    }

    @Test
    fun `dismissing follow up dismisses dialog`() {
        whenever(application.dropbitAccountHelper.isPhoneVerified).thenReturn(true)
        val identity = mock(DropbitMeIdentity::class.java)
        whenever(identity.identity).thenReturn("+13305551111")
        whenever(identity.type).thenReturn(IdentityType.PHONE)
        whenever(application.dropbitAccountHelper.phoneIdentity()).thenReturn(identity)
        val scenario = startScenario()
        scenario.onFragment { fragment ->
            clickOn(withId(fragment.view, R.id.remove_verification))
        }

        val dialog = ShadowDialog.getLatestDialog() as AlertDialog
        clickOn(withId(dialog, R.id.ok))

        val confirmation = ShadowDialog.getLatestDialog() as AlertDialog
        confirmation.getButton(DialogInterface.BUTTON_NEGATIVE).performClick()

        scenario.onFragment { fragment ->
            assertNull(fragment.activity?.supportFragmentManager?.findFragmentByTag("CONFIRM_DEVERIFICATION_NOTICE"))
            assertNull(fragment.activity?.supportFragmentManager?.findFragmentByTag("CONFIRM_DEVERIFICATION_CONFIRMATION_NOTICE"))
        }
    }


    @Test
    fun `observing deVerification updates ui`() {
        whenever(application.dropbitAccountHelper.isPhoneVerified).thenReturn(true)
        val identity = mock(DropbitMeIdentity::class.java)
        whenever(identity.identity).thenReturn("+13305551111")
        whenever(identity.type).thenReturn(IdentityType.PHONE)
        whenever(application.dropbitAccountHelper.phoneIdentity()).thenReturn(identity)
        val scenario = startScenario()

        scenario.onFragment { fragment ->
            fragment.receiver.onReceive(fragment.context, Intent(DropbitIntents.ACTION_DEVERIFY_PHONE_NUMBER_COMPLETED))


            val verificationButton = fragment.getVerificationButton()!!
            assertThat(verificationButton.text.toString(), equalTo(fragment.getString(R.string.verify_phone_button_title)))
            assertThat(verificationButton.visibility, equalTo(View.VISIBLE))

            val identityView = fragment.getIdentityView()!!
            assertThat(identityView.visibility, equalTo(View.GONE))

            val removeVerificationView = fragment.getRemoveVerificationView()!!
            assertThat(removeVerificationView.visibility, equalTo(View.GONE))
        }

    }

    @Test
    fun `failing to deVerify shows toast`() {
        whenever(application.dropbitAccountHelper.isPhoneVerified).thenReturn(true)
        val identity = mock(DropbitMeIdentity::class.java)
        whenever(identity.identity).thenReturn("+13305551111")
        whenever(identity.type).thenReturn(IdentityType.PHONE)
        whenever(application.dropbitAccountHelper.phoneIdentity()).thenReturn(identity)
        val scenario = startScenario()

        scenario.onFragment { fragment ->
            fragment.receiver.onReceive(fragment.context, Intent(DropbitIntents.ACTION_DEVERIFY_PHONE_NUMBER_FAILED))
        }

        assertThat(ShadowToast.getTextOfLatestToast(), equalTo(getString(application, R.string.deverification_phone_failed)))
    }
}