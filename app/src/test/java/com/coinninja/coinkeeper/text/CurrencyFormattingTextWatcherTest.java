package com.coinninja.coinkeeper.text;

import android.content.Context;
import android.widget.EditText;

import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.currency.USDCurrency;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static com.coinninja.matchers.TextViewMatcher.hasText;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class CurrencyFormattingTextWatcherTest {

    private EditText editText;

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

        editText.addTextChangedListener(watcher);
    }

    @Test
    public void formats_BTC() {
        watcher.setCurrency(new BTCCurrency());

        editText.setText("1");
        assertThat(editText, hasText("1"));

        editText.setText("1.");
        assertThat(editText, hasText("1."));

        editText.setText("1.0");
        assertThat(editText, hasText("1.0"));

        editText.setText("1.00");
        assertThat(editText, hasText("1.00"));

        editText.setText("1.000");
        assertThat(editText, hasText("1.000"));

        editText.setText("1.0000");
        assertThat(editText, hasText("1.0000"));

        editText.setText("1.00000");
        assertThat(editText, hasText("1.00000"));

        editText.setText("1.12345678");
        assertThat(editText, hasText("1.12345678"));

        editText.setText("1.123456789");
        assertThat(editText, hasText("1.12345678"));
        verify(callback).onInvalid("1.123456789");

        editText.setText("0.00");
        assertThat(editText, hasText("0.00"));
    }

    @Test
    public void allows_empty_btc() {
        watcher.setCurrency(new BTCCurrency());

        editText.setText("0.001");
        assertThat(editText, hasText("0.001"));

        editText.setText("0.00");
        assertThat(editText, hasText("0.00"));
    }

    @Test
    public void allows_empty() {
        editText.setText("");
        assertThat(editText, hasText("$0"));
    }

    @Test
    public void handles_paste_of_number() {
        String value = "2222.1";
        editText.setText(value);
        assertThat(editText, hasText("$2,222.1"));
        verify(callback).onValid(watcher.getCurrency());
    }

    @Test
    public void allow_entry_for_precision() {
        String value = "$2,222.";
        editText.setText(value);
        assertThat(editText, hasText(value));
        verify(callback).onValid(watcher.getCurrency());
    }

    @Test
    public void keeps_precise_zeros() {
        String value = "1.0";
        editText.setText(value);
        assertThat(editText, hasText("$1.0"));

        value = "1.00";
        editText.setText(value);
        assertThat(editText, hasText("$1.00"));


        verify(callback, times(2)).onValid(watcher.getCurrency());
    }

    @Test
    public void does_not_add_unnecessary_padding() {
        String value = "1.10";
        editText.setText(value);
        assertThat(editText, hasText("$1.10"));
    }

    @Test
    public void keeps_precise_zeros_within_limits_of_currency() {
        String value = "1.000";
        editText.setText(value);
        assertThat(editText, hasText("$1.00"));
        verify(callback).onValid(watcher.getCurrency());
    }

    @Test
    public void does_not_allow_double_decimals() {
        String value = "$2,222..";
        editText.setText(value);
        assertThat(editText, hasText("$2,222."));
        verify(callback).onInvalid(value);
    }

    @Test
    public void does_not_allow_multiple_fractions() {
        String value = "$2,222,222.01.";
        editText.setText(value);
        verify(callback).onInvalid(value);
        assertThat(editText, hasText("$2,222,222.01"));
    }

    @Test
    public void does_not_allow_grouping_separator_after_precision_separator() {
        String value = "$2,222,222.1,";
        editText.setText(value);
        verify(callback).onInvalid(value);
        assertThat(editText, hasText("$2,222,222.1"));

        value = "$2,2222";
        editText.setText(value);
        assertThat(editText, hasText("$22,222"));
        verify(callback).onValid(watcher.getCurrency());
    }

    @Test
    public void US_formatting() {
        String value = "$2,222.01";
        editText.setText(value);
        assertThat(editText, hasText(value));

        value = "$2,222,222.01";
        editText.setText(value);
        assertThat(editText, hasText(value));
    }

    @Test
    @Config(qualifiers = "de-rDE")
    public void German_formatting() {
        String value = "$2.222,01";
        editText.setText(value);
        assertThat(editText, hasText(value));

        value = "$2.222.222,01";
        editText.setText(value);
        assertThat(editText, hasText(value));
    }

    @Test
    public void notifies_on_input() {
        watcher.setCurrency(new BTCCurrency());

        editText.setText("0");
        editText.setText("0.");
        editText.setText("0.0");

        watcher.setCurrency(new USDCurrency());
        editText.setText("$0");
        editText.setText("$0.");
        editText.setText("$0.0");

        verify(callback, times(4)).onInput();
    }

    @Test
    public void notifies_when_reached_zero_value() {
        watcher.setCurrency(new BTCCurrency());

        editText.setText("1");
        editText.setText("0.0");
        editText.setText("0.");
        editText.setText("0");

        watcher.setCurrency(new USDCurrency());
        editText.setText("$0.9");
        editText.setText("$0.");
        editText.setText("$0");

        verify(callback, times(2)).onZeroed();
    }
}
