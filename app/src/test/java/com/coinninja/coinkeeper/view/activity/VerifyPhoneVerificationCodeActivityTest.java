package com.coinninja.coinkeeper.view.activity;

import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.model.PhoneNumber;
import com.coinninja.coinkeeper.service.SyncDropBitService;
import com.coinninja.coinkeeper.ui.dropbit.me.DropbitMeConfiguration;
import com.coinninja.coinkeeper.util.DropbitIntents;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil;
import com.coinninja.coinkeeper.view.dialog.GenericAlertDialog;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowActivity;

import static com.coinninja.matchers.TextViewMatcher.hasText;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

@RunWith(AndroidJUnit4.class)
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
    private BroadcastReceiver receiver;
    private Analytics mockAnalytics;

    @Mock
    private ActivityNavigationUtil activityNavigationUtil;
    @Mock
    private LocalBroadCastUtil localBroadCastUtil;
    @Mock
    private DropbitMeConfiguration dropbitMeConfiguration;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        TestCoinKeeperApplication application = ApplicationProvider.getApplicationContext();
        application.dropbitMeConfiguration = dropbitMeConfiguration;
        application.localBroadCastUtil = localBroadCastUtil;
        application.activityNavigationUtil = activityNavigationUtil;

        phoneNumber = new PhoneNumber(1, "3305555555");
        Intent startingIntent = new Intent();
        startingIntent.putExtra(DropbitIntents.EXTRA_PHONE_NUMBER, phoneNumber);
        activityController = Robolectric.buildActivity(VerifyPhoneVerificationCodeActivity.class,
                startingIntent);
        activity = activityController.get();
        shadowActivity = shadowOf(activity);
        activityController.create();
        receiver = activity.receiver;
        activityController.start().resume().visible();
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
        activityNavigationUtil = null;
        dropbitMeConfiguration = null;
    }

    @Test
    public void navigate_to_home_on_success() {
        receiver.onReceive(activity, new Intent(DropbitIntents.ACTION_PHONE_VERIFICATION__SUCCESS));

        verify(dropbitMeConfiguration).setInitialVerification();
        verify(activityNavigationUtil).navigateToHome(activity);
    }

    @Test
    public void check_for_sms_bitcoin_invites_on_success() {
        receiver.onReceive(activity, new Intent(DropbitIntents.ACTION_PHONE_VERIFICATION__SUCCESS));

        Intent intent = shadowActivity.getNextStartedService();

        assertThat(intent.getComponent().getClassName(), equalTo(SyncDropBitService.class.getName()));
        verify(mockAnalytics).trackEvent(Analytics.EVENT_PHONE_VERIFICATION_SUCCESSFUL);
    }

    @Test
    public void hides_error_when_user_begins_input() {
        receiver.onReceive(activity, new Intent(DropbitIntents.ACTION_PHONE_VERIFICATION__INVALID_CODE));

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


        receiver.onReceive(activity, new Intent(DropbitIntents.ACTION_PHONE_VERIFICATION__INVALID_CODE));

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
        receiver.onReceive(activity, new Intent(DropbitIntents.ACTION_PHONE_VERIFICATION__EXPIRED_CODE));

        assertNotNull(activity.getSupportFragmentManager().
                findFragmentByTag(VerifyPhoneVerificationCodeActivity.EXPIRED_CODE_FRAGMENT_TAG));
    }

    @Test
    public void observes_broadcasts_of_invalid_conf_code() {
        receiver.onReceive(activity, new Intent(DropbitIntents.ACTION_PHONE_VERIFICATION__INVALID_CODE));

        assertThat(error_message.getVisibility(), equalTo(View.VISIBLE));
        assertThat(error_message.getText().toString(), equalTo(activity.getResources().
                getString(R.string.activity_verify_phonecode_error_basic)));
    }

    @Ignore
    @Test
    public void verifies_code_on_final_input() {
        one.setText("0");
        two.setText("1");
        three.setText("2");
        four.setText("3");
        five.setText("4");
        activity.onFocusChange(five, true);
        six.setText("5");

        verify(activity.serviceWorkUtil).validatePhoneNumberConfirmationCode("012345");
    }

    @Test
    public void requests_new_verification_when_clicked() {
        activity.findViewById(R.id.resend_link).performClick();

        verify(activity.serviceWorkUtil).resendPhoneVerification(phoneNumber);
    }

    @Test
    public void shows_retry_phone_entry_on_third_error() {
        TextView error_message = activity.findViewById(R.id.error_message);
        String basic_error = activity.getResources().getString(R.string.activity_verify_phonecode_error_basic);

        assertThat(error_message.getVisibility(), equalTo(View.GONE));

        receiver.onReceive(activity, new Intent(DropbitIntents.ACTION_PHONE_VERIFICATION__INVALID_CODE));
        assertThat(error_message.getVisibility(), equalTo(View.VISIBLE));
        assertThat(error_message.getText().toString(), equalTo(basic_error));

        receiver.onReceive(activity, new Intent(DropbitIntents.ACTION_PHONE_VERIFICATION__INVALID_CODE));
        assertThat(error_message.getVisibility(), equalTo(View.VISIBLE));
        assertThat(error_message.getText().toString(), equalTo(basic_error));

        receiver.onReceive(activity, new Intent(DropbitIntents.ACTION_PHONE_VERIFICATION__INVALID_CODE));
        assertThat(error_message.getVisibility(), equalTo(View.GONE));
        assertNotNull(activity.getSupportFragmentManager().findFragmentByTag(VerifyPhoneVerificationCodeActivity.TOO_MANY_ATTEMPTS_FRAGMENT_TAG));
    }

    @Test
    public void shows_error_when_bad_pin() {
        assertThat(activity.findViewById(R.id.error_message).getVisibility(), equalTo(View.GONE));

        activity.onInvalidCode();

        assertThat(activity.findViewById(R.id.error_message).getVisibility(), equalTo(View.VISIBLE));
    }

    @Test
    public void verify_code_after_paste() {
        one.requestFocus();
        one.setText("123456");

        verify(activity.serviceWorkUtil).validatePhoneNumberConfirmationCode("123456");
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
    public void observes_broadcasts_of_expired_code_test() {

        receiver.onReceive(activity, new Intent(DropbitIntents.ACTION_PHONE_VERIFICATION__EXPIRED_CODE));
        DialogFragment dialog = (DialogFragment) activity.getSupportFragmentManager().findFragmentByTag(VerifyPhoneVerificationCodeActivity.EXPIRED_CODE_FRAGMENT_TAG);
        TextView messageDisplay = dialog.getDialog().findViewById(android.R.id.message);

        assertThat(messageDisplay.getText().toString(), equalTo("Your 6 digit code has expired please try again."));
    }

    @Test
    public void observes_broadcasts_of_rate_limit_error_test() {

        receiver.onReceive(activity, new Intent(DropbitIntents.ACTION_PHONE_VERIFICATION__RATE_LIMIT_ERROR));
        DialogFragment dialog = (DialogFragment) activity.getSupportFragmentManager().findFragmentByTag(VerifyPhoneVerificationCodeActivity.TOO_FAST_SERVER_ATTEMPTS_FRAGMENT_TAG);
        TextView messageDisplay = dialog.getDialog().findViewById(android.R.id.message);

        assertThat(messageDisplay.getText().toString(), equalTo("Verification codes can only be requested every 30 seconds."));
    }

    @Test
    public void observes_broadcasts_of_http_error_test() {

        receiver.onReceive(activity, new Intent(DropbitIntents.ACTION_PHONE_VERIFICATION__CN_HTTP_ERROR));
        DialogFragment dialog = (DialogFragment) activity.getSupportFragmentManager().findFragmentByTag(VerifyPhoneVerificationCodeActivity.SERVER_ERROR_FRAGMENT_TAG);
        TextView messageDisplay = dialog.getDialog().findViewById(android.R.id.message);

        assertThat(messageDisplay.getText().toString(), equalTo("The verification code could not be sent. Please try again later."));
    }

    @Test
    public void observes_broadcasts_of_blacklist_error_test() {

        receiver.onReceive(activity, new Intent(DropbitIntents.ACTION_PHONE_VERIFICATION__CN_BLACKLIST_ERROR));
        DialogFragment dialog = (DialogFragment) activity.getSupportFragmentManager().findFragmentByTag(VerifyPhoneVerificationCodeActivity.SERVER_ERROR_FRAGMENT_TAG);
        TextView messageDisplay = dialog.getDialog().findViewById(android.R.id.message);

        assertThat(messageDisplay.getText().toString(), equalTo("DropBit does not currently support phone numbers in your country. You can still use DropBit as a Bitcoin wallet, but some features will be limited. Please skip the phone verification process above to continue."));
    }

    @Test
    public void observes_broadcasts_of_sms_code_sent_ok_to_user_test() {

        receiver.onReceive(activity, new Intent(DropbitIntents.ACTION_PHONE_VERIFICATION__CODE_SENT));

        DialogFragment dialog = (DialogFragment) activity.getSupportFragmentManager().findFragmentByTag(VerifyPhoneVerificationCodeActivity.VERIFICATION_CODE_SENT);
        TextView messageDisplay = dialog.getDialog().findViewById(android.R.id.message);

        assertThat(messageDisplay.getText().toString(), equalTo("You will receive a verification code SMS shortly."));
    }

    @Test
    public void sets_focus_on_first_child_once_user_acknowledges_code_message() {
        receiver.onReceive(activity, new Intent(DropbitIntents.ACTION_PHONE_VERIFICATION__CODE_SENT));

        GenericAlertDialog dialog = (GenericAlertDialog) activity.getSupportFragmentManager().findFragmentByTag(VerifyPhoneVerificationCodeActivity.VERIFICATION_CODE_SENT);
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
        IntentFilter filter = activity.getIntentFilter();

        assertThat(filter.getAction(0), equalTo(DropbitIntents.ACTION_PHONE_VERIFICATION__INVALID_CODE));
        assertThat(filter.getAction(1), equalTo(DropbitIntents.ACTION_PHONE_VERIFICATION__EXPIRED_CODE));
        assertThat(filter.getAction(2), equalTo(DropbitIntents.ACTION_PHONE_VERIFICATION__SUCCESS));
        assertThat(filter.getAction(3), equalTo(DropbitIntents.ACTION_PHONE_VERIFICATION__RATE_LIMIT_ERROR));
        assertThat(filter.getAction(4), equalTo(DropbitIntents.ACTION_PHONE_VERIFICATION__CN_HTTP_ERROR));
        assertThat(filter.getAction(5), equalTo(DropbitIntents.ACTION_PHONE_VERIFICATION__CN_BLACKLIST_ERROR));
        assertThat(filter.getAction(6), equalTo(DropbitIntents.ACTION_PHONE_VERIFICATION__CODE_SENT));
    }

    @Test
    public void headline_message_has_formatted_string_test() {
        TextView headline = activity.findViewById(R.id.headline);

        assertThat(headline,
                hasText(activity.getResources()
                        .getString(R.string.activity_verify_phone_code_headline,
                                phoneNumber.toInternationalDisplayText())));
    }
}
