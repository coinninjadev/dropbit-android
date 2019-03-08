package com.coinninja.coinkeeper.text;

import android.content.Context;
import android.widget.EditText;

import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.currency.Currency;
import com.coinninja.coinkeeper.util.currency.USDCurrency;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class CurrencyFormattingTextWatcherTest {

    private EditText editText;
    private Currency currency = new USDCurrency();

    @Mock
    private CurrencyFormattingTextWatcher.Callback callback;
    private CurrencyFormattingTextWatcher watcher;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Context context = RuntimeEnvironment.application.getApplicationContext();
        editText = new EditText(context);
        watcher = new CurrencyFormattingTextWatcher();
        watcher.setCallback(callback);
        watcher.setCurrency(currency);

        editText.addTextChangedListener(watcher);
    }

    @Test
    public void formats_BTC_with_symbol() {
        BTCCurrency currency = new BTCCurrency("1.0");
        watcher.setCurrency(currency);
        editText.setText("1");

        assertThat(editText.getText().toString(), equalTo(currency.toFormattedCurrency()));
    }

    @Test
    public void allows_ending_in_dot() {
        editText.setText("1");
        editText.setText("1.");

        assertThat(editText.getText().toString(), equalTo("$1."));
    }

    @Test
    public void allows_zero() {
        editText.setText("1");
        editText.setText("");

        assertThat(editText.getText().toString(), equalTo("$0"));
    }

    @Test
    public void handles_paste_of_number() {
        String value = "2222.1";
        editText.setText(value);
        assertThat(editText.getText().toString(), equalTo(currency.toIncrementalFormat()));

        verify(callback).onValid(currency);
    }

    @Test
    public void keeps_zeros() {
        String value = "1.0";
        editText.setText(value);
        assertThat(editText.getText().toString(), equalTo("$1.0"));

        verify(callback).onValid(currency);
    }

    @Test
    public void keeps_zeros_2() {
        String value = "1.00";
        editText.setText(value);
        assertThat(editText.getText().toString(), equalTo("$1.00"));

        verify(callback).onValid(currency);
    }

    @Test
    public void keeps_zeros_3() {
        String value = "1.000";
        editText.setText(value);
        assertThat(editText.getText().toString(), equalTo("$1.00"));

        verify(callback).onInvalid(value);
    }

    @Test
    public void pauses_for_invalid() {
        String value = "$2,222.1";
        editText.setText(value);
        assertThat(editText.getText().toString(), equalTo(value));

        verify(callback).onValid(currency);

        String invalidValue = value + "r";
        editText.setText(invalidValue);
        assertThat(editText.getText().toString(), equalTo(value));

        verify(callback).onInvalid(invalidValue);
    }

    @Test
    public void pauses_for_invalid_double_decimal() {
        String value = "$2,222.";
        editText.setText(value);
        assertThat(editText.getText().toString(), equalTo(value));

        verify(callback).onValid(currency);

        String invalidValue = value + ".";
        editText.setText(invalidValue);
        assertThat(editText.getText().toString(), equalTo(value));

        verify(callback).onInvalid(invalidValue);
    }

    @Test
    public void endingDecimal() {
        String value = "$2,222";
        editText.setText(value);
        assertThat(editText.getText().toString(), equalTo(value));

        String endingWithDecimal = value + ".";
        editText.setText(endingWithDecimal);
        assertThat(editText.getText().toString(), equalTo(endingWithDecimal));

        verify(callback, times(2)).onValid(currency);
    }

}
