package com.coinninja.coinkeeper.view.activity

import android.content.BroadcastReceiver
import android.content.DialogInterface
import android.content.Intent
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.TestCoinKeeperApplication
import com.coinninja.coinkeeper.model.PhoneNumber
import com.coinninja.coinkeeper.service.SyncDropBitService
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.coinkeeper.util.analytics.Analytics
import com.coinninja.coinkeeper.view.dialog.GenericAlertDialog
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowActivity

@RunWith(AndroidJUnit4::class)
class VerifyPhoneVerificationCodeActivityTest {

    private val phoneNumber: PhoneNumber = PhoneNumber(1, "3305555555")
    private val startingIntent = Intent().also {
        it.putExtra(DropbitIntents.EXTRA_PHONE_NUMBER, phoneNumber)
    }

    private val application
        get() = ApplicationProvider.getApplicationContext<TestCoinKeeperApplication>().also {
            it.dropbitMeConfiguration = mock()
            it.localBroadCastUtil = mock()
            it.activityNavigationUtil = mock()

        }

    private val activityController = Robolectric.buildActivity(VerifyPhoneVerificationCodeActivity::class.java, startingIntent).setup()
    private val activity: VerifyPhoneVerificationCodeActivity = activityController.get()
    private val one: EditText get() = activity.findViewById(R.id.v_one)
    private val two: EditText get() = activity.findViewById(R.id.v_two)
    private val three: EditText get() = activity.findViewById(R.id.v_three)
    private val four: EditText get() = activity.findViewById(R.id.v_four)
    private val five: EditText get() = activity.findViewById(R.id.v_five)
    private val six: EditText get() = activity.findViewById(R.id.v_six)
    private val shadowActivity: ShadowActivity get() = shadowOf(activity)
    private val errorMessage: TextView get() = activity.findViewById(R.id.error_message)
    private val receiver: BroadcastReceiver get() = activity.receiver
    private val mockAnalytics: Analytics get() = activity.analytics


    @Test
    fun navigate_to_home_on_success() {
        receiver.onReceive(activity, Intent(DropbitIntents.ACTION_PHONE_VERIFICATION__SUCCESS))

        verify(activity.dropbitMeConfiguration).setInitialVerification()
        verify(activity.activityNavigationUtil).navigateToHome(activity)
    }

    @Test
    fun check_for_sms_bitcoin_invites_on_success() {
        receiver.onReceive(activity, Intent(DropbitIntents.ACTION_PHONE_VERIFICATION__SUCCESS))

        val intent = shadowActivity.nextStartedService

        assertThat(intent.component.className).isEqualTo(SyncDropBitService::class.java.name)
        verify(mockAnalytics).trackEvent(Analytics.EVENT_PHONE_VERIFICATION_SUCCESSFUL)
    }

    @Test
    fun hides_error_when_user_begins_input() {
        receiver.onReceive(activity, Intent(DropbitIntents.ACTION_PHONE_VERIFICATION__INVALID_CODE))

        one.setText("0")

        assertThat(activity.findViewById<View>(R.id.error_message).visibility).isEqualTo(View.GONE)
    }


    @Test
    fun clears_code_input_on_invalid_code() {
        one.setText("0")
        two.setText("1")
        three.setText("2")
        four.setText("3")
        five.setText("4")
        six.setText("5")


        receiver.onReceive(activity, Intent(DropbitIntents.ACTION_PHONE_VERIFICATION__INVALID_CODE))

        assertThat(one.text.toString()).isEqualTo("")
        assertThat(two.text.toString()).isEqualTo("")
        assertThat(three.text.toString()).isEqualTo("")
        assertThat(four.text.toString()).isEqualTo("")
        assertThat(five.text.toString()).isEqualTo("")
        assertThat(six.text.toString()).isEqualTo("")
    }

    @Test
    fun accepting_expired_code_navigates_to_verify_phone_screen() {
        val dialog = mock<DialogInterface>()

        activity.onClick(dialog, DialogInterface.BUTTON_POSITIVE)

        verify(dialog).dismiss()
    }


    @Test
    fun observes_code_expired_message() {
        receiver.onReceive(activity, Intent(DropbitIntents.ACTION_PHONE_VERIFICATION__EXPIRED_CODE))

        assertThat(activity.supportFragmentManager.findFragmentByTag(VerifyPhoneVerificationCodeActivity.EXPIRED_CODE_FRAGMENT_TAG)).isNotNull()
    }

    @Test
    fun observes_broadcasts_of_invalid_conf_code() {
        receiver.onReceive(activity, Intent(DropbitIntents.ACTION_PHONE_VERIFICATION__INVALID_CODE))

        assertThat(errorMessage.visibility).isEqualTo(View.VISIBLE)
        assertThat(errorMessage.text.toString()).isEqualTo(activity.resources.getString(R.string.activity_verify_phonecode_error_basic))
    }

    @Test
    fun verifies_code_on_final_input() {
        one.setText("0")
        two.setText("1")
        three.setText("2")
        four.setText("3")
        five.setText("4")
        six.also {
            activity.focusOn(it)
            it.setText("5")
        }

        verify(activity.serviceWorkUtil).validatePhoneNumberConfirmationCode("012345")
    }

    @Test
    fun requests_new_verification_when_clicked() {
        activity.findViewById<View>(R.id.resend_link).performClick()

        verify(activity.serviceWorkUtil).resendPhoneVerification(phoneNumber)
    }

    @Test
    fun shows_retry_phone_entry_on_third_error() {
        val error_message = activity.findViewById<TextView>(R.id.error_message)
        val basic_error = activity.resources.getString(R.string.activity_verify_phonecode_error_basic)

        assertThat(error_message.visibility).isEqualTo(View.GONE)

        receiver.onReceive(activity, Intent(DropbitIntents.ACTION_PHONE_VERIFICATION__INVALID_CODE))
        assertThat(error_message.visibility).isEqualTo(View.VISIBLE)
        assertThat(error_message.text.toString()).isEqualTo(basic_error)

        receiver.onReceive(activity, Intent(DropbitIntents.ACTION_PHONE_VERIFICATION__INVALID_CODE))
        assertThat(error_message.visibility).isEqualTo(View.VISIBLE)
        assertThat(error_message.text.toString()).isEqualTo(basic_error)

        receiver.onReceive(activity, Intent(DropbitIntents.ACTION_PHONE_VERIFICATION__INVALID_CODE))
        assertThat(error_message.visibility).isEqualTo(View.GONE)
        assertThat(activity.supportFragmentManager.findFragmentByTag(VerifyPhoneVerificationCodeActivity.TOO_MANY_ATTEMPTS_FRAGMENT_TAG)).isNotNull()
    }

    @Test
    fun pasting_short_digits_focus_on_next() {
        one.requestFocus()
        assertThat(one.hasFocus()).isTrue()
        one.setText("123")

        assertThat(four.hasFocus()).isTrue()
    }

    @Test
    fun shows_error_when_bad_pin() {
        assertThat(activity.findViewById<View>(R.id.error_message).visibility).isEqualTo(View.GONE)

        activity.onInvalidCode()

        assertThat(activity.findViewById<View>(R.id.error_message).visibility).isEqualTo(View.VISIBLE)
    }

    @Test
    fun verify_code_after_paste() {
        one.requestFocus()
        one.setText("123456")

        assertThat(one.text.toString()).isEqualTo("1")
        assertThat(two.text.toString()).isEqualTo("2")
        assertThat(three.text.toString()).isEqualTo("3")
        assertThat(four.text.toString()).isEqualTo("4")
        assertThat(five.text.toString()).isEqualTo("5")
        assertThat(six.text.toString()).isEqualTo("6")
        verify(activity.serviceWorkUtil).validatePhoneNumberConfirmationCode("123456")
    }

    @Test
    fun paste_in_the_middle() {
        one.setText("1")
        two.setText("2")
        three.requestFocus()
        three.setText("123456")


        assertThat(one.text.toString()).isEqualTo("1")
        assertThat(two.text.toString()).isEqualTo("2")
        assertThat(three.text.toString()).isEqualTo("3")
        assertThat(four.text.toString()).isEqualTo("4")
        assertThat(five.text.toString()).isEqualTo("5")
        assertThat(six.text.toString()).isEqualTo("6")
        verify(activity.serviceWorkUtil).validatePhoneNumberConfirmationCode("123456")
    }

    @Test
    fun will_not_paste_more_than_6_characters() {
        one.requestFocus()
        one.setText("1234567")

        assertThat(one.text.toString()).isEqualTo("1")
        assertThat(two.text.toString()).isEqualTo("2")
        assertThat(three.text.toString()).isEqualTo("3")
        assertThat(four.text.toString()).isEqualTo("4")
        assertThat(five.text.toString()).isEqualTo("5")
        assertThat(six.text.toString()).isEqualTo("6")
        verify(activity.serviceWorkUtil).validatePhoneNumberConfirmationCode("123456")
    }

    @Test
    fun clears_all_on_stop() {
        one.setText("012345")

        activityController.stop()

        verifyAllCleared()
    }

    private fun verifyAllCleared() {
        assertThat(one.text.toString()).isEqualTo("")
        assertThat(two.text.toString()).isEqualTo("")
        assertThat(three.text.toString()).isEqualTo("")
        assertThat(four.text.toString()).isEqualTo("")
        assertThat(five.text.toString()).isEqualTo("")
        assertThat(six.text.toString()).isEqualTo("")
    }

    @Test
    fun clears_all_text_ahead_of_newly_focused_input_field() {
        one.setText("0")
        two.setText("1")
        three.setText("2")
        four.setText("3")
        five.setText("4")
        six.setText("5")


        activity.onFocusChange(three, true)

        assertThat(one.text.toString()).isEqualTo("0")
        assertThat(two.text.toString()).isEqualTo("1")
        assertThat(three.text.toString()).isEqualTo("")
        assertThat(four.text.toString()).isEqualTo("")
        assertThat(five.text.toString()).isEqualTo("")
        assertThat(six.text.toString()).isEqualTo("")
    }

    @Test
    fun clears_text_of_focused_field_on_focus() {
        one.setText("0")
        two.setText("1")
        three.setText("2")

        activity.onFocusChange(three, true)

        assertThat(three.text.toString()).isEqualTo("")
        assertThat(two.text.toString()).isEqualTo("1")
        assertThat(one.text.toString()).isEqualTo("0")
    }

    @Test
    fun observes_broadcasts_of_expired_code_test() {
        one.setText("123456")

        receiver.onReceive(activity, Intent(DropbitIntents.ACTION_PHONE_VERIFICATION__EXPIRED_CODE))
        val dialog = activity.supportFragmentManager.findFragmentByTag(VerifyPhoneVerificationCodeActivity.EXPIRED_CODE_FRAGMENT_TAG) as DialogFragment
        val messageDisplay = dialog.dialog!!.findViewById<TextView>(android.R.id.message)

        assertThat(messageDisplay.text.toString()).isEqualTo("Your 6 digit code has expired please try again.")
        verifyAllCleared()
    }

    @Test
    fun observes_broadcasts_of_rate_limit_error_test() {
        one.setText("123456")

        receiver.onReceive(activity, Intent(DropbitIntents.ACTION_PHONE_VERIFICATION__RATE_LIMIT_ERROR))
        val dialog = activity.supportFragmentManager.findFragmentByTag(VerifyPhoneVerificationCodeActivity.TOO_FAST_SERVER_ATTEMPTS_FRAGMENT_TAG) as DialogFragment
        val messageDisplay = dialog.dialog!!.findViewById<TextView>(android.R.id.message)

        assertThat(messageDisplay.text.toString()).isEqualTo("Verification codes can only be requested every 30 seconds.")
        verifyAllCleared()
    }

    @Test
    fun observes_broadcasts_of_http_error_test() {
        one.setText("123456")

        receiver.onReceive(activity, Intent(DropbitIntents.ACTION_PHONE_VERIFICATION__CN_HTTP_ERROR))
        val dialog = activity.supportFragmentManager.findFragmentByTag(VerifyPhoneVerificationCodeActivity.SERVER_ERROR_FRAGMENT_TAG) as DialogFragment
        val messageDisplay = dialog.dialog!!.findViewById<TextView>(android.R.id.message)

        assertThat(messageDisplay.text.toString()).isEqualTo("The verification code could not be sent. Please try again later.")
        verifyAllCleared()
    }

    @Test
    fun observes_broadcasts_of_blacklist_error_test() {
        one.setText("123456")
        receiver.onReceive(activity, Intent(DropbitIntents.ACTION_PHONE_VERIFICATION__CN_BLACKLIST_ERROR))
        val dialog = activity.supportFragmentManager.findFragmentByTag(VerifyPhoneVerificationCodeActivity.SERVER_ERROR_FRAGMENT_TAG) as DialogFragment
        val messageDisplay = dialog.dialog!!.findViewById<TextView>(android.R.id.message)

        assertThat(messageDisplay.text.toString()).isEqualTo("DropBit does not currently support phone numbers in your country. You can still use DropBit as a Bitcoin wallet, but some features will be limited. Please skip the phone verification process above to continue.")
        verifyAllCleared()
    }

    @Test
    fun observes_broadcasts_of_sms_code_sent_ok_to_user_test() {
        one.setText("123456")
        receiver.onReceive(activity, Intent(DropbitIntents.ACTION_PHONE_VERIFICATION__CODE_SENT))

        val dialog = activity.supportFragmentManager.findFragmentByTag(VerifyPhoneVerificationCodeActivity.VERIFICATION_CODE_SENT) as DialogFragment
        val messageDisplay = dialog.dialog!!.findViewById<TextView>(android.R.id.message)

        assertThat(messageDisplay.text.toString()).isEqualTo("You will receive a verification code SMS shortly.")
        verifyAllCleared()
    }

    @Test
    fun sets_focus_on_first_child_once_user_acknowledges_code_message() {
        one.setText("123456")
        receiver.onReceive(activity, Intent(DropbitIntents.ACTION_PHONE_VERIFICATION__CODE_SENT))

        val dialog = activity.supportFragmentManager.findFragmentByTag(VerifyPhoneVerificationCodeActivity.VERIFICATION_CODE_SENT) as GenericAlertDialog
        dialog.onClickListener.onClick(dialog.dialog, DialogInterface.BUTTON_POSITIVE)

        assertThat(one.isFocused).isTrue()
        verifyAllCleared()
    }

    @Test
    fun unregisters_receiver_when_paused() {
        activityController.pause().stop()

        verify(activity.localBroadCastUtil).unregisterReceiver(receiver)
    }

    @Test
    fun registers_for_broadcasts() {
        val filter = activity.intentFilter

        assertThat(filter.getAction(0)).isEqualTo(DropbitIntents.ACTION_PHONE_VERIFICATION__INVALID_CODE)
        assertThat(filter.getAction(1)).isEqualTo(DropbitIntents.ACTION_PHONE_VERIFICATION__EXPIRED_CODE)
        assertThat(filter.getAction(2)).isEqualTo(DropbitIntents.ACTION_PHONE_VERIFICATION__SUCCESS)
        assertThat(filter.getAction(3)).isEqualTo(DropbitIntents.ACTION_PHONE_VERIFICATION__RATE_LIMIT_ERROR)
        assertThat(filter.getAction(4)).isEqualTo(DropbitIntents.ACTION_PHONE_VERIFICATION__CN_HTTP_ERROR)
        assertThat(filter.getAction(5)).isEqualTo(DropbitIntents.ACTION_PHONE_VERIFICATION__CN_BLACKLIST_ERROR)
        assertThat(filter.getAction(6)).isEqualTo(DropbitIntents.ACTION_PHONE_VERIFICATION__CODE_SENT)
    }

    @Test
    fun headline_message_has_formatted_string_test() {
        val headline = activity.findViewById<TextView>(R.id.headline)
        assertThat(headline.text).isEqualTo(activity.resources.getString(R.string.activity_verify_phone_code_headline, phoneNumber.toInternationalDisplayText()))
    }
}
