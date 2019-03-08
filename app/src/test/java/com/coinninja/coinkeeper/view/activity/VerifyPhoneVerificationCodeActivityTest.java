package com.coinninja.coinkeeper.view.activity;

import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.model.db.PhoneNumber;
import com.coinninja.coinkeeper.service.ResendPhoneVerificationService;
import com.coinninja.coinkeeper.service.SyncDropBitService;
import com.coinninja.coinkeeper.service.UserPhoneConfirmationService;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;
import com.coinninja.coinkeeper.view.dialog.GenericAlertDialog;
import com.google.i18n.phonenumbers.Phonenumber;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class VerifyPhoneVerificationCodeActivityTest {

    private VerifyPhoneVerificationCodeActivity activity;
    private EditText one;
    private EditText two;
    private EditText three;
    private EditText four;
    private EditText five;
    private EditText six;
    private ActivityController<VerifyPhoneVerificationCodeActivity> activityController;
    private ShadowActivity shadowActivity;
    private TextView error_message;
    private PhoneNumber phoneNumber;
    private LocalBroadCastUtil localBroadCastUtil;
    private BroadcastReceiver receiver;
    private Analytics mockAnalytics;

    @Before
    public void setUp() {
        localBroadCastUtil = mock(LocalBroadCastUtil.class);
        phoneNumber = new PhoneNumber(1, "3305555555");
        Intent startingIntent = new Intent();
        startingIntent.putExtra(Intents.EXTRA_PHONE_NUMBER, phoneNumber);
        activityController = Robolectric.buildActivity(VerifyPhoneVerificationCodeActivity.class,
                startingIntent);
        activity = activityController.get();
        shadowActivity = shadowOf(activity);
        activityController.create();
        receiver = activity.receiver;
        activity.localBroadCastUtil = localBroadCastUtil;
        activityController.resume().start().visible();
        one = activity.findViewById(R.id.v_one);
        two = activity.findViewById(R.id.v_two);
        three = activity.findViewById(R.id.v_three);
        four = activity.findViewById(R.id.v_four);
        five = activity.findViewById(R.id.v_five);
        six = activity.findViewById(R.id.v_six);
        error_message = activity.findViewById(R.id.error_message);
        mockAnalytics = activity.analytics;
    }

    @After
    public void tearDown() {
        activity = null;
        one = null;
        two = null;
        three = null;
        four = null;
        five = null;
        six = null;
        activityController = null;
        shadowActivity = null;
        error_message = null;
        phoneNumber = null;
        localBroadCastUtil = null;
        receiver = null;
        mockAnalytics = null;
    }

    @Test
    public void navigate_to_calculator_on_success() {
        receiver.onReceive(activity, new Intent(Intents.ACTION_PHONE_VERIFICATION__SUCCESS));

        Intent intent = shadowActivity.getNextStartedActivity();

        assertThat(intent.getComponent().getClassName(), equalTo(CalculatorActivity.class.getName()));
        assertThat(intent.getFlags(), equalTo(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));

    }

    @Test
    public void check_for_sms_bitcoin_invites_on_success() {
        receiver.onReceive(activity, new Intent(Intents.ACTION_PHONE_VERIFICATION__SUCCESS));

        Intent intent = shadowActivity.getNextStartedService();

        assertThat(intent.getComponent().getClassName(), equalTo(SyncDropBitService.class.getName()));
        verify(mockAnalytics).trackEvent(Analytics.EVENT_PHONE_VERIFICATION_SUCCESSFUL);
    }

    @Test
    public void hides_error_when_user_begins_input() {
        receiver.onReceive(activity, new Intent(Intents.ACTION_PHONE_VERIFICATION__INVALID_CODE));

        one.setText("0");

        assertThat(activity.findViewById(R.id.error_message).getVisibility(), equalTo(View.GONE));
    }


    @Test
    public void clears_code_input_on_invalid_code() {
        one.setText("0");
        two.setText("1");
        three.setText("2");
        four.setText("3");
        five.setText("4");
        six.setText("5");


        receiver.onReceive(activity, new Intent(Intents.ACTION_PHONE_VERIFICATION__INVALID_CODE));

        assertThat(one.getText().toString(), equalTo(""));
        assertThat(two.getText().toString(), equalTo(""));
        assertThat(three.getText().toString(), equalTo(""));
        assertThat(four.getText().toString(), equalTo(""));
        assertThat(five.getText().toString(), equalTo(""));
        assertThat(six.getText().toString(), equalTo(""));
    }

    @Test
    public void accepting_expired_code_navigates_to_verify_phone_screen() {
        DialogInterface dialog = mock(DialogInterface.class);

        activity.onClick(dialog, DialogInterface.BUTTON_POSITIVE);

        verify(dialog).dismiss();
    }


    @Test
    public void observes_code_expired_message() {
        receiver.onReceive(activity, new Intent(Intents.ACTION_PHONE_VERIFICATION__EXPIRED_CODE));

        assertNotNull(activity.getFragmentManager().
                findFragmentByTag(VerifyPhoneVerificationCodeActivity.EXPIRED_CODE_FRAGMENT_TAG));
    }

    @Test
    public void observes_broadcasts_of_invalid_conf_code() {
        receiver.onReceive(activity, new Intent(Intents.ACTION_PHONE_VERIFICATION__INVALID_CODE));

        assertThat(error_message.getVisibility(), equalTo(View.VISIBLE));
        assertThat(error_message.getText().toString(), equalTo(activity.getResources().
                getString(R.string.activity_verify_phonecode_error_basic)));
    }

    @Test
    public void verifies_code_on_final_input() {
        one.setText("0");
        two.setText("1");
        three.setText("2");
        four.setText("3");
        five.setText("4");
        six.setText("5");

        Intent intent = shadowActivity.peekNextStartedService();
        assertThat(intent.getComponent().getClassName(), equalTo(UserPhoneConfirmationService.class.getName()));
        assertThat(intent.getExtras().getString(Intents.EXTRA_PHONE_NUMBER_CODE), equalTo("012345"));
    }

    @Test
    public void requests_new_verifiction_when_clicked() {

        activity.findViewById(R.id.resend_link).performClick();


        Intent intent = shadowActivity.peekNextStartedService();
        assertThat(intent.getComponent().getClassName(), equalTo(ResendPhoneVerificationService.class.getName()));
        PhoneNumber phoneNumber = intent.getExtras().getParcelable(Intents.EXTRA_PHONE_NUMBER);
        assertThat(phoneNumber.getNationalNumber(), equalTo(this.phoneNumber.getNationalNumber()));
        assertThat(phoneNumber.getCountryCode(), equalTo(this.phoneNumber.getCountryCode()));
    }

    @Test
    public void shows_retry_phone_entry_on_third_error() {
        TextView error_message = activity.findViewById(R.id.error_message);
        String basic_error = activity.getResources().getString(R.string.activity_verify_phonecode_error_basic);

        assertThat(error_message.getVisibility(), equalTo(View.GONE));

        receiver.onReceive(activity, new Intent(Intents.ACTION_PHONE_VERIFICATION__INVALID_CODE));
        assertThat(error_message.getVisibility(), equalTo(View.VISIBLE));
        assertThat(error_message.getText().toString(), equalTo(basic_error));

        receiver.onReceive(activity, new Intent(Intents.ACTION_PHONE_VERIFICATION__INVALID_CODE));
        assertThat(error_message.getVisibility(), equalTo(View.VISIBLE));
        assertThat(error_message.getText().toString(), equalTo(basic_error));

        receiver.onReceive(activity, new Intent(Intents.ACTION_PHONE_VERIFICATION__INVALID_CODE));
        assertThat(error_message.getVisibility(), equalTo(View.GONE));
        assertNotNull(activity.getFragmentManager().findFragmentByTag(VerifyPhoneVerificationCodeActivity.TOO_MANY_ATTEMPTS_FRAGMENT_TAG));
    }

    @Test
    public void shows_error_when_bad_pin() {
        assertThat(activity.findViewById(R.id.error_message).getVisibility(), equalTo(View.GONE));

        activity.onInvalidCode();

        assertThat(activity.findViewById(R.id.error_message).getVisibility(), equalTo(View.VISIBLE));
    }

    @Test
    public void clears_all_on_stop() {
        one.setText("012345");

        activityController.stop();

        assertThat(one.getText().toString(), equalTo(""));
        assertThat(two.getText().toString(), equalTo(""));
        assertThat(three.getText().toString(), equalTo(""));
        assertThat(four.getText().toString(), equalTo(""));
        assertThat(five.getText().toString(), equalTo(""));
        assertThat(six.getText().toString(), equalTo(""));
    }

    @Test
    public void can_paste_in_middle() {
        one.setText("0");
        two.setText("1");
        three.setText("2");
        four.setText("345");

        assertThat(one.getText().toString(), equalTo("0"));
        assertThat(two.getText().toString(), equalTo("1"));
        assertThat(three.getText().toString(), equalTo("2"));
        assertThat(four.getText().toString(), equalTo("3"));
        assertThat(five.getText().toString(), equalTo("4"));
        assertThat(six.getText().toString(), equalTo("5"));
        assertTrue(six.isFocused());
    }

    @Test
    public void sets_focus_on_field_after_end_of_paste() {
        one.setText("01234");

        assertThat(one.getText().toString(), equalTo("0"));
        assertThat(two.getText().toString(), equalTo("1"));
        assertThat(three.getText().toString(), equalTo("2"));
        assertThat(four.getText().toString(), equalTo("3"));
        assertThat(five.getText().toString(), equalTo("4"));

        assertTrue(six.isFocused());
        assertThat(six.getText().toString(), equalTo(""));
    }

    @Test
    public void fills_span_on_input() {
        one.setText("012345");

        assertThat(one.getText().toString(), equalTo("0"));
        assertThat(two.getText().toString(), equalTo("1"));
        assertThat(three.getText().toString(), equalTo("2"));
        assertThat(four.getText().toString(), equalTo("3"));
        assertThat(five.getText().toString(), equalTo("4"));
        assertThat(six.getText().toString(), equalTo("5"));
    }

    @Test
    public void clears_all_text_ahead_of_newly_focused_input_field() {
        one.setText("0");
        two.setText("1");
        three.setText("2");
        four.setText("3");
        five.setText("4");
        six.setText("5");


        activity.onFocusChange(three, true);

        assertThat(one.getText().toString(), equalTo("0"));
        assertThat(two.getText().toString(), equalTo("1"));
        assertThat(three.getText().toString(), equalTo(""));
        assertThat(four.getText().toString(), equalTo(""));
        assertThat(five.getText().toString(), equalTo(""));
        assertThat(six.getText().toString(), equalTo(""));
    }

    @Test
    public void clears_text_of_focused_field_on_focus() {
        one.setText("0");
        two.setText("1");
        three.setText("2");

        activity.onFocusChange(three, true);

        assertThat(three.getText().toString(), equalTo(""));
        assertThat(two.getText().toString(), equalTo("1"));
        assertThat(one.getText().toString(), equalTo("0"));
    }

    @Test
    public void changes_current_on_input() {
        one.setText("0");

        assertThat(one.getText().toString(), equalTo("0"));
        assertThat(two.hasFocus(), equalTo(true));
    }

    @Test
    public void focuses_on_current_input_on_start() {
        assertThat(one.hasFocus(), equalTo(true));
        assertThat(two.hasFocus(), equalTo(false));
    }


    @Test
    public void observes_broadcasts_of_expired_code_test() {

        receiver.onReceive(activity, new Intent(Intents.ACTION_PHONE_VERIFICATION__EXPIRED_CODE));
        DialogFragment dialog = (DialogFragment) activity.getFragmentManager().findFragmentByTag(VerifyPhoneVerificationCodeActivity.EXPIRED_CODE_FRAGMENT_TAG);
        TextView messageDisplay = dialog.getDialog().findViewById(android.R.id.message);

        assertThat(messageDisplay.getText().toString(), equalTo("Your 6 digit code has expired please try again."));
    }

    @Test
    public void observes_broadcasts_of_rate_limit_error_test() {

        receiver.onReceive(activity, new Intent(Intents.ACTION_PHONE_VERIFICATION__RATE_LIMIT_ERROR));
        DialogFragment dialog = (DialogFragment) activity.getFragmentManager().findFragmentByTag(VerifyPhoneVerificationCodeActivity.TOO_FAST_SERVER_ATTEMPTS_FRAGMENT_TAG);
        TextView messageDisplay = dialog.getDialog().findViewById(android.R.id.message);

        assertThat(messageDisplay.getText().toString(), equalTo("Verification codes can only be requested every 30 seconds."));
    }

    @Test
    public void observes_broadcasts_of_http_error_test() {

        receiver.onReceive(activity, new Intent(Intents.ACTION_PHONE_VERIFICATION__CN_HTTP_ERROR));
        DialogFragment dialog = (DialogFragment) activity.getFragmentManager().findFragmentByTag(VerifyPhoneVerificationCodeActivity.SERVER_ERROR_FRAGMENT_TAG);
        TextView messageDisplay = dialog.getDialog().findViewById(android.R.id.message);

        assertThat(messageDisplay.getText().toString(), equalTo("Please try again."));
    }

    @Test
    public void observes_broadcasts_of_sms_code_sent_ok_to_user_test() {

        receiver.onReceive(activity, new Intent(Intents.ACTION_PHONE_VERIFICATION__CODE_SENT));

        DialogFragment dialog = (DialogFragment) activity.getFragmentManager().findFragmentByTag(VerifyPhoneVerificationCodeActivity.VERIFICATION_CODE_SENT);
        TextView messageDisplay = dialog.getDialog().findViewById(android.R.id.message);

        assertThat(messageDisplay.getText().toString(), equalTo("You will receive a verification code SMS shortly."));
    }

    @Test
    public void sets_focus_on_first_child_once_user_acknowleges_code_message() {
        receiver.onReceive(activity, new Intent(Intents.ACTION_PHONE_VERIFICATION__CODE_SENT));

        GenericAlertDialog dialog = (GenericAlertDialog) activity.getFragmentManager().findFragmentByTag(VerifyPhoneVerificationCodeActivity.VERIFICATION_CODE_SENT);
        dialog.getOnClickListener().onClick(dialog.getDialog(), DialogInterface.BUTTON_POSITIVE);

        assertTrue(one.isFocused());
    }

    @Test
    public void unregisters_receiver_when_paused() {
        activityController.pause().stop();

        verify(localBroadCastUtil).unregisterReceiver(receiver);
    }

    @Test
    public void registers_for_broadcasts() {
        ArgumentCaptor<IntentFilter> argumentCaptor = ArgumentCaptor.forClass(IntentFilter.class);
        verify(localBroadCastUtil).registerReceiver(any(BroadcastReceiver.class), argumentCaptor.capture());

        IntentFilter filter = argumentCaptor.getValue();

        assertThat(filter.getAction(0), equalTo(Intents.ACTION_PHONE_VERIFICATION__INVALID_CODE));
        assertThat(filter.getAction(1), equalTo(Intents.ACTION_PHONE_VERIFICATION__EXPIRED_CODE));
        assertThat(filter.getAction(2), equalTo(Intents.ACTION_PHONE_VERIFICATION__SUCCESS));
        assertThat(filter.getAction(3), equalTo(Intents.ACTION_PHONE_VERIFICATION__RATE_LIMIT_ERROR));
        assertThat(filter.getAction(4), equalTo(Intents.ACTION_PHONE_VERIFICATION__CN_HTTP_ERROR));
        assertThat(filter.getAction(5), equalTo(Intents.ACTION_PHONE_VERIFICATION__CODE_SENT));
    }

    @Test
    public void headline_message_has_formatted_string_test() {
        String expectedPhoneNumberInMessage = "2162626262";
        Intent startingIntent = new Intent();
        startingIntent.putExtra(Intents.EXTRA_PHONE_NUMBER, new PhoneNumber(1, expectedPhoneNumberInMessage));
        activity = Robolectric.buildActivity(VerifyPhoneVerificationCodeActivity.class, startingIntent).create().resume().start().visible().get();


        TextView headline = activity.findViewById(R.id.headline);


        assertThat(headline.getText(), equalTo("We’ve sent a six digit verification code to (216-262-6262). It may take up to 30 seconds to receive the text."));
    }
}
