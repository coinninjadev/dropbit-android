package com.coinninja.coinkeeper.view.widget.phonenumber;

import android.app.AlertDialog;
import android.os.Build;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.text.PhoneNumberFormattingTextWatcher;
import com.coinninja.coinkeeper.ui.base.TestableActivity;
import com.coinninja.coinkeeper.view.widget.phonenumber.PhoneNumberInputView.OnInvalidPhoneNumberObserver;
import com.google.i18n.phonenumbers.Phonenumber;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowAlertDialog;
import org.robolectric.shadows.ShadowDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.coinninja.android.helpers.Views.withId;
import static com.coinninja.coinkeeper.view.widget.phonenumber.PhoneNumberInputView.OnExamplePhoneNumberChangedObserver;
import static com.coinninja.coinkeeper.view.widget.phonenumber.PhoneNumberInputView.OnValidPhoneNumberObserver;
import static com.coinninja.matchers.TextViewMatcher.hasText;
import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class, qualifiers = "en-rUS")
public class PhoneNumberInputViewTest {

    private int EXISTING_ACTIVITY_ID;
    private TestableActivity activity;
    private PhoneNumberInputView phoneNumberInputView;

    private List<CountryCodeLocale> countryCodeLocales;

    @Before
    public void setUp() {
        EXISTING_ACTIVITY_ID = TestableActivity.LAYOUT;
        TestableActivity.LAYOUT = R.layout.__test_phone_number_input_view;
        activity = Robolectric.setupActivity(TestableActivity.class);
        phoneNumberInputView = withId(activity, R.id.phone_number_input);
        countryCodeLocales = new ArrayList<>();
        countryCodeLocales.add(new CountryCodeLocale(new Locale("en", "GB"), 44));
        countryCodeLocales.add(new CountryCodeLocale(new Locale("en", "US"), 1));
        phoneNumberInputView.setCountryCodeLocals(countryCodeLocales);
    }

    @After
    public void tearDown() {
        TestableActivity.LAYOUT = EXISTING_ACTIVITY_ID;
        phoneNumberInputView = null;
        activity = null;
    }

    @Test
    @Config(qualifiers = "en-rUS", sdk = Build.VERSION_CODES.M)
    public void defaults_to_device_local__pre_N() {
        Locale locale = phoneNumberInputView.getCurrentLocale();

        assertThat(locale.toString(), equalTo("en_US"));
    }

    @Test
    public void defaults_to_device_local__post_N() {
        Locale locale = phoneNumberInputView.getCurrentLocale();

        assertThat(locale.toString(), equalTo("en_US"));
    }

    @Test
    public void test_it_renders_country_code_list__US() {
        phoneNumberInputView.setCountryCodeLocals(countryCodeLocales);

        assertThat(countryCodeLocales.get(1), equalTo(phoneNumberInputView.getSelectedCountryCodeLocale()));
    }

    @Test
    @Config(qualifiers = "en-rGB")
    public void test_it_renders_country_code_list__GB() {
        phoneNumberInputView.setCountryCodeLocals(countryCodeLocales);

        assertThat(countryCodeLocales.get(0), equalTo(phoneNumberInputView.getSelectedCountryCodeLocale()));
    }

    @Test
    public void sets_the_flag_view_with_the_correct_emoji() {
        TextView emoji = withId(activity, R.id.phone_number_view_flag);

        assertThat(emoji, hasText(countryCodeLocales.get(1).getEmoji()));
    }

    @Test
    public void sets_country_code_for_initialized_country() {
        TextView selectedCountryCode = withId(activity, R.id.phone_number_view_number_input);

        assertThat(selectedCountryCode, hasText("+1"));
    }

    @Test
    public void sets_minimum_ems_when_widget_inits() {
        EditText phoneInput = withId(activity, R.id.phone_number_view_number_input);

        assertThat(phoneInput.getMaxEms(), equalTo(7));
        assertThat(phoneInput.getMaxEms(), equalTo(7));
    }

    @Test
    @Config(qualifiers = "en-rGB")
    public void sets_minimum_ems_when_widget_inits__gb() {
        EditText phoneInput = withId(activity, R.id.phone_number_view_number_input);

        assertThat(phoneInput.getMinEms(), equalTo(7));
        assertThat(phoneInput.getMaxEms(), equalTo(7));
    }

    @Test
    public void formats_input() {
        EditText phoneInput = withId(activity, R.id.phone_number_view_number_input);

        phoneNumberInputView.setText("3305551111");

        assertThat(phoneInput, hasText("+1 330-555-1111"));
    }

    @Test
    @Config(qualifiers = "en-rGB")
    public void formats_input__GB() {
        EditText phoneInput = withId(activity, R.id.phone_number_view_number_input);

        phoneNumberInputView.setText("+441632960025");

        assertThat(phoneInput, hasText("+44 1632 960025"));
    }

    @Test
    public void sets_selection_to_end_when_clicked() {
        EditText phoneInput = withId(activity, R.id.phone_number_view_number_input);

        phoneInput.performClick();

        assertThat(phoneInput.getSelectionEnd(), equalTo(phoneInput.getText().length()));
        assertThat(phoneInput.getSelectionStart(), equalTo(phoneInput.getText().length()));
    }

    @Test
    public void shows_alert_dialog_for_selection_countries_when_selector_chosen() {
        withId(activity, R.id.phone_number_view_country_codes).performClick();

        assertNotNull(ShadowDialog.getLatestDialog());
    }

    @Test
    public void selecting_country_sets_view_to_locale() {
        PhoneNumberFormattingTextWatcher watcher = mock(PhoneNumberFormattingTextWatcher.class);
        TextView emoji = withId(activity, R.id.phone_number_view_flag);
        EditText phoneInput = withId(activity, R.id.phone_number_view_number_input);
        phoneInput.removeTextChangedListener(phoneNumberInputView.numberFormattingTextWatcher);
        phoneInput.addTextChangedListener(watcher);
        phoneNumberInputView.numberFormattingTextWatcher = watcher;
        withId(activity, R.id.phone_number_view_country_codes).performClick();
        AlertDialog latestDialog = (AlertDialog) ShadowAlertDialog.getLatestDialog();

        shadowOf(latestDialog.getListView()).performItemClick(0);

        assertThat(phoneInput, hasText("+44"));
        assertThat(emoji, hasText(countryCodeLocales.get(0).getEmoji()));
        assertThat(phoneNumberInputView.getSelectedCountryCodeLocale(), equalTo(countryCodeLocales.get(0)));
        verify(watcher).updateLocale(countryCodeLocales.get(0).getLocale());
    }

    @Test
    public void notifies_observers_when_example_number_changes() {
        OnExamplePhoneNumberChangedObserver observer = mock(OnExamplePhoneNumberChangedObserver.class);

        phoneNumberInputView.setOnExampleNumberChangeObserver(observer);
        verify(observer).onExamplePhoneNumberChanged("+1 201-555-0123");

        withId(activity, R.id.phone_number_view_country_codes).performClick();
        AlertDialog latestDialog = (AlertDialog) ShadowAlertDialog.getLatestDialog();
        shadowOf(latestDialog.getListView()).performItemClick(0);

        verify(observer).onExamplePhoneNumberChanged("+44 7400 123456");
    }

    @Test
    public void notifies_observer_when_phone_entry_is_valid() {
        OnValidPhoneNumberObserver observer = mock(OnValidPhoneNumberObserver.class);
        phoneNumberInputView.setOnValidPhoneNumberObserver(observer);

        phoneNumberInputView.setText("+1 330-555-0123");

        Phonenumber.PhoneNumber phoneNumber = new Phonenumber.PhoneNumber();
        phoneNumber.setCountryCode(1);
        phoneNumber.setNationalNumber(3305550123L);
        verify(observer).onValidPhoneNumber(eq(phoneNumber));
    }

    @Test
    public void notifies_observer_when_phone_entry_is_not_valid() {
        OnInvalidPhoneNumberObserver observer = mock(OnInvalidPhoneNumberObserver.class);
        phoneNumberInputView.setOnInvalidPhoneNumberObserver(observer);
        String text = "+1 000-000-0000";

        phoneNumberInputView.setText(text);

        verify(observer).onInvalidPhoneNumber(text);
    }

    @Test
    public void can_get_phone_text_from_view() {
        String text = "+1 330-555-0123";

        phoneNumberInputView.setText(text);

        assertThat(phoneNumberInputView.getText(), equalTo(text));
    }
}