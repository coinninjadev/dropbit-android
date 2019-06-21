package com.coinninja.coinkeeper.view.widget.phonenumber;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.ui.base.TestableActivity;
import com.coinninja.coinkeeper.view.widget.phonenumber.PhoneNumberInputView.OnValidPhoneNumberObserver;
import com.google.i18n.phonenumbers.Phonenumber;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowAlertDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.coinninja.android.helpers.Views.clickOn;
import static com.coinninja.android.helpers.Views.withId;
import static com.coinninja.matchers.TextViewMatcher.hasText;
import static com.coinninja.matchers.ViewMatcher.isGone;
import static com.coinninja.matchers.ViewMatcher.isVisible;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class PhoneVerificationViewTest {
    private TestableActivity activity;
    private PhoneNumberInputView phoneNumberInputView;

    private List<CountryCodeLocale> countryCodeLocales;
    private TextView errorMessage;
    private TextView exampleNumber;
    private PhoneVerificationView phoneVerificationView;
    private Button verifyPhoneButton;

    @Before
    public void setUp() {
        activity = Robolectric.setupActivity(TestableActivity.class);
        activity.appendLayout(R.layout.activity_verify_phone);
        phoneVerificationView = withId(activity, R.id.verification_view);
        phoneNumberInputView = withId(activity, R.id.phone_number_input);
        errorMessage = withId(activity, R.id.error_message);
        exampleNumber = withId(activity, R.id.example_number);
        verifyPhoneButton = withId(activity, R.id.verify_phone_number);
        countryCodeLocales = new ArrayList<>();
        countryCodeLocales.add(new CountryCodeLocale(new Locale("en", "GB"), 44));
        countryCodeLocales.add(new CountryCodeLocale(new Locale("en", "US"), 1));
    }

    @After
    public void tearDown() {
        phoneNumberInputView = null;
        exampleNumber = null;
        errorMessage = null;
        activity = null;
        countryCodeLocales.clear();
        phoneVerificationView = null;
    }

    @Test
    public void initial_configuration() {
        assertThat(errorMessage, isGone());
        assertThat(verifyPhoneButton, isGone());
    }

    @Test
    public void shows_error_on_invalid_entry() {
        phoneNumberInputView.setText("000-000-0000");

        assertThat(errorMessage, isVisible());
    }

    @Test
    public void sets_country_code_locales_on_phone_input() {
        phoneVerificationView.setCountryCodeLocals(countryCodeLocales);

        assertThat(phoneNumberInputView.getCountryCodeLocales(), equalTo(countryCodeLocales));
    }

    @Test
    public void provides_reset() {
        errorMessage.setVisibility(View.VISIBLE);
        phoneNumberInputView.setText("330");

        phoneVerificationView.resetView();

        assertThat(errorMessage, isGone());
        assertThat(phoneNumberInputView.getText(), equalTo("+1"));
    }

    @Test(expected = Test.None.class) // No exception expected
    public void does_not_call_observer_when_not_set() {
        OnValidPhoneNumberObserver observer = mock(OnValidPhoneNumberObserver.class);
        Phonenumber.PhoneNumber phoneNumber = new Phonenumber.PhoneNumber();
        phoneNumber.setNationalNumber(3305555555L);
        phoneNumber.setCountryCode(1);

        phoneNumberInputView.setText("3305555555");

        // does not throw a NPE
    }

    @Test
    public void calls_callback_when_number_verified() {
        OnValidPhoneNumberObserver observer = mock(OnValidPhoneNumberObserver.class);
        Phonenumber.PhoneNumber phoneNumber = new Phonenumber.PhoneNumber();
        phoneNumber.setNationalNumber(3305555555L);
        phoneNumber.setCountryCode(1);
        phoneVerificationView.setOnValidPhoneNumberObserver(observer);

        phoneNumberInputView.setText("3305555555");

        verify(observer).onValidPhoneNumber(phoneNumber);
    }

    @Test
    public void shows_error_message_on_error() {
        exampleNumber.setVisibility(View.GONE);

        phoneNumberInputView.setText("0005555555");

        assertThat(errorMessage, isVisible());
    }

    @Test
    public void clears_text_on_invalid_input_except_cc() {
        phoneNumberInputView.setText("0005555555");

        assertThat(phoneNumberInputView.getText(), equalTo("+1"));
    }

    @Test
    public void updates_example_phone_number_when_changing() {
        phoneVerificationView.setCountryCodeLocals(countryCodeLocales);
        assertThat(exampleNumber, hasText("Example: +1 201-555-0123"));

        withId(activity, R.id.phone_number_view_country_codes).performClick();
        AlertDialog latestDialog = (AlertDialog) ShadowAlertDialog.getLatestDialog();
        shadowOf(latestDialog.getListView()).performItemClick(0);

        assertThat(exampleNumber, hasText("Example: +44 7400 123456"));
    }

    @Test
    public void resets_view_when_changing_countries() {
        phoneVerificationView.setCountryCodeLocals(countryCodeLocales);
        assertThat(exampleNumber, hasText("Example: +1 201-555-0123"));
        phoneNumberInputView.setText("330555");
        errorMessage.setVisibility(View.VISIBLE);

        withId(activity, R.id.phone_number_view_country_codes).performClick();
        AlertDialog latestDialog = (AlertDialog) ShadowAlertDialog.getLatestDialog();
        shadowOf(latestDialog.getListView()).performItemClick(0);

        assertThat(phoneNumberInputView.getText(), equalTo("+44 "));
        assertThat(errorMessage, isGone());
    }

    @Test
    public void shows_button_when_manual_submission_required() {
        countryCodeLocales.add(new CountryCodeLocale(new Locale("en", "NZ"), 64));
        phoneVerificationView.setCountryCodeLocals(countryCodeLocales);
        assertThat(verifyPhoneButton, isGone());

        clickOn(phoneVerificationView, R.id.phone_number_view_country_codes);
        AlertDialog latestDialog = (AlertDialog) ShadowAlertDialog.getLatestDialog();
        shadowOf(latestDialog.getListView()).performItemClick(2);
        assertThat(verifyPhoneButton, isVisible());

        clickOn(phoneVerificationView, R.id.phone_number_view_country_codes);
        latestDialog = (AlertDialog) ShadowAlertDialog.getLatestDialog();
        shadowOf(latestDialog.getListView()).performItemClick(0);
        assertThat(verifyPhoneButton, isGone());
    }

    @Test
    public void performs_validation_of_number_when_enabled_and_pressed__notify_observer_of_success() {
        OnValidPhoneNumberObserver observer = mock(OnValidPhoneNumberObserver.class);
        countryCodeLocales.add(new CountryCodeLocale(new Locale("en", "NZ"), 64));
        phoneVerificationView.setOnValidPhoneNumberObserver(observer);
        phoneVerificationView.setCountryCodeLocals(countryCodeLocales);
        clickOn(phoneVerificationView, R.id.phone_number_view_country_codes);
        AlertDialog latestDialog = (AlertDialog) ShadowAlertDialog.getLatestDialog();
        shadowOf(latestDialog.getListView()).performItemClick(2);

        phoneNumberInputView.setText("21345687");
        clickOn(verifyPhoneButton);

        Phonenumber.PhoneNumber phoneNumber = new Phonenumber.PhoneNumber();
        phoneNumber.setCountryCode(64);
        phoneNumber.setNationalNumber(21345687L);
        verify(observer).onValidPhoneNumber(phoneNumber);
    }

    @Test
    public void performs_validation_of_number_when_enabled_and_pressed__show_error() {
        OnValidPhoneNumberObserver observer = mock(OnValidPhoneNumberObserver.class);
        countryCodeLocales.add(new CountryCodeLocale(new Locale("en", "NZ"), 64));
        phoneVerificationView.setOnValidPhoneNumberObserver(observer);
        phoneVerificationView.setCountryCodeLocals(countryCodeLocales);
        clickOn(phoneVerificationView, R.id.phone_number_view_country_codes);
        AlertDialog latestDialog = (AlertDialog) ShadowAlertDialog.getLatestDialog();
        shadowOf(latestDialog.getListView()).performItemClick(2);

        phoneNumberInputView.setText("2134568");
        clickOn(verifyPhoneButton);

        assertThat(errorMessage, isVisible());
    }

    @Test
    public void performs_validation_of_number_when_enabled_and_pressed__show_error__failed_to_parse() {
        OnValidPhoneNumberObserver observer = mock(OnValidPhoneNumberObserver.class);
        countryCodeLocales.add(new CountryCodeLocale(new Locale("en", "NZ"), 64));
        phoneVerificationView.setOnValidPhoneNumberObserver(observer);
        phoneVerificationView.setCountryCodeLocals(countryCodeLocales);
        clickOn(phoneVerificationView, R.id.phone_number_view_country_codes);
        AlertDialog latestDialog = (AlertDialog) ShadowAlertDialog.getLatestDialog();
        shadowOf(latestDialog.getListView()).performItemClick(2);

        phoneNumberInputView.setText("whoops");
        clickOn(verifyPhoneButton);

        assertThat(errorMessage, isVisible());
    }


}