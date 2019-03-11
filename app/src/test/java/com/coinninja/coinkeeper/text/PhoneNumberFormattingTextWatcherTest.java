package com.coinninja.coinkeeper.text;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.google.i18n.phonenumbers.Phonenumber;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Locale;

import static com.coinninja.matchers.TextViewMatcher.hasText;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class,
        qualifiers = "en-rUS")
public class PhoneNumberFormattingTextWatcherTest {

    private EditText editText;
    private PhoneNumberFormattingTextWatcher.Callback callback;
    private PhoneNumberFormattingTextWatcher watcher;

    @Before
    public void setUp() {
        Context context = RuntimeEnvironment.application.getApplicationContext();
        Locale locale = context.getResources().getConfiguration().locale;
        callback = mock(PhoneNumberFormattingTextWatcher.Callback.class);
        editText = new EditText(context);
        watcher = new PhoneNumberFormattingTextWatcher(locale, callback);
        editText.addTextChangedListener(watcher);
    }

    @Test
    public void handles_paste_of_number() {
        long expected = 3304175555L;
        Phonenumber.PhoneNumber phoneNumber = new Phonenumber.PhoneNumber();
        phoneNumber.setNationalNumber(expected);
        phoneNumber.setCountryCode(1);

        editText.setText("+13304175555");
        assertThat(editText.getText().toString(), equalTo("+1 330-417-5555"));

        editText.setText("13304175555");
        assertThat(editText.getText().toString(), equalTo("+1 330-417-5555"));

        verify(callback, atLeast(1)).onPhoneNumberValid(phoneNumber);
    }

    @Test
    public void handles_paste_of_number_parens() {
        long expected = 3304175555L;
        Phonenumber.PhoneNumber phoneNumber = new Phonenumber.PhoneNumber();
        phoneNumber.setNationalNumber(expected);
        phoneNumber.setCountryCode(1);
        editText.setText("(330) 417-5555");
        assertThat(editText.getText().toString(), equalTo("+1 330-417-5555"));

        verify(callback).onPhoneNumberValid(phoneNumber);
    }

    @Test
    public void formats_after_region_code_change() {
        editText.setText("330");
        editText.setText("3304");
        assertThat(editText.getText().toString(), equalTo("+1 330-4"));
    }

    @Test
    public void formats_subscription_values() {
        editText.setText("330 41788");
        assertThat(editText.getText().toString(), equalTo("+1 330-417-88"));
        editText.setText("330-417-8888");
        assertThat(editText.getText().toString(), equalTo("+1 330-417-8888"));
    }

    @Test
    public void prevents_digits_after_phone_number() {
        editText.setText("+1 330-417-88888");
        assertThat(editText.getText().toString(), equalTo("+1 330-417-8888"));

        editText.setText("+1 330-417-888888");
        assertThat(editText.getText().toString(), equalTo("+1 330-417-8888"));

        editText.setText("+1 330-417-88888888");
        assertThat(editText.getText().toString(), equalTo("+1 330-417-8888"));
    }

    @Test
    public void digit_incremental() {
        editText.setText("33041788888");
        assertThat(editText.getText().toString(), equalTo("+1 330-417-8888"));
        editText.setText("3");
        assertThat(editText.getText().toString(), equalTo("+1 3"));
        editText.setText("33");
        assertThat(editText.getText().toString(), equalTo("+1 33"));
        editText.setText("330");
        assertThat(editText.getText().toString(), equalTo("+1 330"));
        editText.setText("330-");
        assertThat(editText.getText().toString(), equalTo("+1 330"));
        editText.setText("3304");
        assertThat(editText.getText().toString(), equalTo("+1 330-4"));
        editText.setText("33041");
        assertThat(editText.getText().toString(), equalTo("+1 330-41"));
        editText.setText("330417");
        assertThat(editText.getText().toString(), equalTo("+1 330-417"));
        editText.setText("330-417-");
        assertThat(editText.getText().toString(), equalTo("+1 330-417"));
        editText.setText("3304178");
        assertThat(editText.getText().toString(), equalTo("+1 330-417-8"));
        editText.setText("33041788");
        assertThat(editText.getText().toString(), equalTo("+1 330-417-88"));
        editText.setText("330417888");
        assertThat(editText.getText().toString(), equalTo("+1 330-417-888"));
        editText.setText("3304178888");
        assertThat(editText.getText().toString(), equalTo("+1 330-417-8888"));

    }


    @Test
    public void formats_autofilled_textviews() {
        long expected = 3304178888L;
        Phonenumber.PhoneNumber phoneNumber = new Phonenumber.PhoneNumber();
        phoneNumber.setNationalNumber(expected);
        phoneNumber.setCountryCode(1);

        editText.setText("13304178888");

        verify(callback).onPhoneNumberValid(phoneNumber);
    }

    @Test
    public void lets_callback_know_when_a_valid_phone_number_found() {
        long expected = 3304178888L;
        Phonenumber.PhoneNumber phoneNumber = new Phonenumber.PhoneNumber();
        phoneNumber.setNationalNumber(expected);
        phoneNumber.setCountryCode(1);

        editText.setText("(330) 417-8888");

        verify(callback).onPhoneNumberValid(phoneNumber);
    }

    @Test
    public void lets_callback_know_that_number_is_invalid() {
        String text = "0000000000";
        editText.setText(text);

        verify(callback).onPhoneNumberInValid("+1 000-000-0000");
    }

    @Test
    public void characters_pause_formatting() {
        String text = "3304178c";
        editText.setText(text);
        assertThat(editText.getText().toString(), equalTo(text));

        text = "33041788";
        editText.setText(text);
        assertThat(editText.getText().toString(), equalTo("+1 330-417-88"));
    }

    @Test
    public void updates_locale_and_formatting_rules__US_to_GB() {
        watcher.updateLocale(new Locale("en", "GB"));

        editText.setText("+441632960025");

        assertThat(editText, hasText("+44 1632 960025"));
    }

    @Test
    public void updates_locale_and_formatting_rules__US_to_JP() {
        watcher.updateLocale(new Locale("ja", "JP"));

        editText.setText("+819068427999");

        assertThat(editText, hasText("+81 90-6842-7999"));
    }

    @Test
    public void prepends_country_code() {
        watcher.updateLocale(new Locale("ja", "JP"));

        editText.setText("9068427999");
        assertThat(editText, hasText("+81 90-6842-7999"));

        editText.setText("+819068427999");
        assertThat(editText, hasText("+81 90-6842-7999"));

        editText.setText("819068427999");
        assertThat(editText, hasText("+81 90-6842-7999"));

        editText.setText("9");
        assertThat(editText, hasText("+81 9"));

        editText.setText("+819");
        assertThat(editText, hasText("+81 9"));

        editText.setText("+8");
        assertThat(editText, hasText("+81 "));

        editText.setText("");
        assertThat(editText, hasText("+81 "));
    }

    @Test
    public void plays_nice_with_other_watchers___passive___we_aint_got_time_for_that() {
        CompetitiveWatcher competitiveWatcher = new CompetitiveWatcher();

        editText.addTextChangedListener(competitiveWatcher);
        editText.setText("3304");

        assertThat(editText.getText().toString(), equalTo(""));
    }

    private class CompetitiveWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (!s.toString().isEmpty()) {
                s.replace(0, s.length(), "");
            }
        }
    }
}