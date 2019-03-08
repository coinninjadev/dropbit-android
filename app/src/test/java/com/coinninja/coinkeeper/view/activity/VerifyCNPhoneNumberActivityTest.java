package com.coinninja.coinkeeper.view.activity;

import android.content.Intent;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.model.db.PhoneNumber;
import com.coinninja.coinkeeper.service.RegisterUsersPhoneService;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;

import java.util.Locale;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class VerifyCNPhoneNumberActivityTest {


    private VerifyPhoneNumberActivity activity;
    private PhoneNumberUtil phoneNumberUtil;
    private Locale locale;
    private ActivityController<VerifyPhoneNumberActivity> activityController;
    private EditText phoneNumber;
    private ShadowActivity shadowActivity;
    private Analytics mockAnalytics;

    @Before
    public void setUp() {
        phoneNumberUtil = PhoneNumberUtil.getInstance();
        activityController = Robolectric.buildActivity(VerifyPhoneNumberActivity.class);
        activity = activityController.get();
        activityController.create().resume().start().visible();
        phoneNumber = activity.findViewById(R.id.phone_number);
        shadowActivity = shadowOf(activity);
        mockAnalytics = activity.analytics;
    }

    @After
    public void tearDown() {
        activity = null;
        phoneNumber = null;
        locale = null;
        activityController = null;
        phoneNumber = null;
        shadowActivity = null;
        mockAnalytics = null;
    }

    @Test
    public void showsKeyboard() {
        View phoneNumber = activity.findViewById(R.id.phone_number);
        assertTrue(phoneNumber.isFocused());
    }

    @Test
    public void user_can_skip_verification_of_key_words() {
        MenuItem closeMenuItem = mock(MenuItem.class);
        when(closeMenuItem.getItemId()).thenReturn(R.id.action_skip_btn);


        activity.onOptionsItemSelected(closeMenuItem);

        Intent intent = shadowActivity.peekNextStartedActivity();
        assertThat(intent.getComponent().getClassName(), equalTo(CalculatorActivity.class.getName()));
        assertThat(intent.getFlags(), equalTo(
                Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK |
                        Intent.FLAG_ACTIVITY_NEW_TASK));
        verify(mockAnalytics).trackEvent(Analytics.EVENT_PHONE_VERIFICATION_SKIPPED);
    }

    @Test
    public void passes_phone_data_to_verification_service() {
        phoneNumber.setText("3305555555");

        Intent intent = shadowActivity.getNextStartedService();
        PhoneNumber number = intent.getParcelableExtra(Intents.EXTRA_PHONE_NUMBER);
        assertThat(number.getNationalNumber(), equalTo(3305555555L));
        assertThat(number.getCountryCode(), equalTo(1));
    }

    @Test
    public void starts_verify_phone_number_service() {
        phoneNumber.setText("3305555555");

        Intent intent = shadowActivity.getNextStartedService();
        assertThat(intent.getComponent().getClassName(), equalTo(RegisterUsersPhoneService.class.getName()));
    }

    @Test
    public void hides_error_on_pause() {
        activity.findViewById(R.id.error_message).setVisibility(View.VISIBLE);

        activityController.pause();

        assertThat(activity.findViewById(R.id.error_message).getVisibility(), equalTo(View.INVISIBLE));
    }

    @Test
    public void shows_error_message_on_error() {
        assertThat(activity.findViewById(R.id.error_message).getVisibility(), equalTo(View.INVISIBLE));

        phoneNumber.setText("0005555555");

        assertThat(activity.findViewById(R.id.error_message).getVisibility(), equalTo(View.VISIBLE));
    }

    @Test
    public void clears_text_on_invalid_input() {
        phoneNumber.setText("0005555555");

        assertThat(phoneNumber.getText().toString(), equalTo(""));

    }

    @Test
    public void clears_input_when_resumed() {
        phoneNumber.setText("3305555555");

        activityController.pause();
        activityController.resume();

        assertThat(phoneNumber.getText().toString(), equalTo(""));
    }

    @Test
    public void valid_number_progresses_to_confirm_screen() {
        ((EditText) activity.findViewById(R.id.phone_number)).setText("3305555555");

        Intent intent = shadowActivity.peekNextStartedActivity();
        assertThat(intent.getComponent().getClassName(),
                equalTo(VerifyPhoneVerificationCodeActivity.class.getName()));
        PhoneNumber phoneNumber = intent.getExtras().getParcelable(Intents.EXTRA_PHONE_NUMBER);
        assertThat(phoneNumber.getNationalNumber(), equalTo(3305555555L));

    }

    @Test
    public void it_renders_the_verify_phone_number_view() {
        assertNotNull(activity.findViewById(R.id.phone_number));
    }

    /*
     things to remember...











     */
    @Test
    @Config(qualifiers = "en-rUS")
    public void facts_about_US_phone_number() throws NumberParseException {
        String number = "(330) 555-1111";
        locale = activity.getResources().getConfiguration().getLocales().get(0);
        assertThat(locale.getCountry(), equalTo("US"));

        // example number
        Phonenumber.PhoneNumber exampleNumberForType = phoneNumberUtil.getExampleNumberForType(locale.getCountry(), PhoneNumberUtil.PhoneNumberType.MOBILE);
        String example = String.valueOf(exampleNumberForType.getNationalNumber());
        Phonenumber.PhoneNumber parsedExample = phoneNumberUtil.parse(example, locale.getCountry());
        assertThat(example, equalTo("2015550123"));
        assertThat(example.length(), equalTo(10));
        assertThat(phoneNumberUtil.format(parsedExample, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL),
                equalTo("+1 201-555-0123"));
        assertThat(phoneNumberUtil.format(parsedExample, PhoneNumberUtil.PhoneNumberFormat.NATIONAL),
                equalTo("(201) 555-0123"));
        assertThat(phoneNumberUtil.format(parsedExample, PhoneNumberUtil.PhoneNumberFormat.E164),
                equalTo("+12015550123"));

        Phonenumber.PhoneNumber parsedNumber = phoneNumberUtil.parse(number, locale.getCountry());
        assertThat(parsedNumber.getNumberOfLeadingZeros(), equalTo(1));
        assertThat(parsedNumber.getNationalNumber(), equalTo(3305551111L));
        assertThat(parsedNumber.getCountryCode(), equalTo(1));
        assertTrue(phoneNumberUtil.isValidNumber(parsedNumber));
        assertThat(phoneNumberUtil.format(parsedNumber, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL),
                equalTo("+1 330-555-1111"));
        assertThat(phoneNumberUtil.format(parsedNumber, PhoneNumberUtil.PhoneNumberFormat.NATIONAL),
                equalTo(number));
        assertThat(phoneNumberUtil.format(parsedNumber, PhoneNumberUtil.PhoneNumberFormat.E164),
                equalTo("+13305551111"));


    }

    @Test
    @Config(qualifiers = "ja-rJP")
    public void formats_JP_phone_number() throws NumberParseException {
        String number = "03 3212 2323";
        locale = activity.getResources().getConfiguration().getLocales().get(0);
        assertThat(locale.getCountry(), equalTo("JP"));

        // example number
        Phonenumber.PhoneNumber exampleNumberForType = phoneNumberUtil.getExampleNumberForType(locale.getCountry(), PhoneNumberUtil.PhoneNumberType.MOBILE);
        String example = String.valueOf(exampleNumberForType.getNationalNumber());
        Phonenumber.PhoneNumber parsedExample = phoneNumberUtil.parse(example, locale.getCountry());
        assertThat(example, equalTo("9012345678"));
        assertThat(example.length(), equalTo(10));
        assertThat(phoneNumberUtil.format(parsedExample, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL),
                equalTo("+81 90-1234-5678"));
        assertThat(phoneNumberUtil.format(parsedExample, PhoneNumberUtil.PhoneNumberFormat.NATIONAL),
                equalTo("090-1234-5678"));
        assertThat(phoneNumberUtil.format(parsedExample, PhoneNumberUtil.PhoneNumberFormat.E164),
                equalTo("+819012345678"));

        Phonenumber.PhoneNumber parsedNumber = phoneNumberUtil.parse(number, locale.getCountry());
        assertThat(parsedNumber.getNumberOfLeadingZeros(), equalTo(1));
        assertThat(parsedNumber.getNationalNumber(), equalTo(332122323L));
        assertThat(parsedNumber.getCountryCode(), equalTo(81));
        assertTrue(phoneNumberUtil.isValidNumber(parsedNumber));
        assertThat(phoneNumberUtil.format(parsedNumber, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL),
                equalTo("+81 3-3212-2323"));
        assertThat(phoneNumberUtil.format(parsedNumber, PhoneNumberUtil.PhoneNumberFormat.NATIONAL),
                equalTo("03-3212-2323"));
        assertThat(phoneNumberUtil.format(parsedNumber, PhoneNumberUtil.PhoneNumberFormat.E164),
                equalTo("+81332122323"));
    }

}