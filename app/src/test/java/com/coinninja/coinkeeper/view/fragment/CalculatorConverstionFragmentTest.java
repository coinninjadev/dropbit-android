package com.coinninja.coinkeeper.view.fragment;

import android.view.View;
import android.widget.TextView;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.presenter.CurrencyConversionPresenter;
import com.coinninja.coinkeeper.presenter.LargeCurrencyConverstionPresenter;
import com.coinninja.coinkeeper.util.Keys;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.currency.USDCurrency;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.FragmentController;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class CalculatorConverstionFragmentTest {

    private CalculatorConverstionFragment fragment;
    private CurrencyConversionPresenter presenter;

    @Before
    public void setUp() {
        presenter = mock(CurrencyConversionPresenter.class);
        FragmentController<CalculatorConverstionFragment> fragmentController = Robolectric.buildFragment(CalculatorConverstionFragment.class);
        fragment = fragmentController.get();
        fragment.setPresenter(presenter);
        fragmentController.create().start().resume().visible();

        verify(presenter, times(1)).attach(fragment);
    }

    @Test
    public void initializesWithLargePresentorWhenCurrentCurrencyisBTC() {
        CalculatorConverstionFragment fragment = CalculatorConverstionFragment.newInstance(new BTCCurrency());
        assertTrue(fragment.getPresenter() instanceof LargeCurrencyConverstionPresenter);
    }

    @Test
    public void initializesWithLargePresentorWhenCurrentCurrencyisUSD() {
        CalculatorConverstionFragment fragment = CalculatorConverstionFragment.newInstance(new USDCurrency());
        assertTrue(fragment.getPresenter() instanceof CurrencyConversionPresenter);
        assertFalse(fragment.getPresenter() instanceof LargeCurrencyConverstionPresenter);
    }

    @Test
    public void forwardsKeyedInput() {
        fragment.onKeyPress(Keys.EIGHT);
        verify(presenter).onInput(Keys.EIGHT);
    }

    @Test
    public void setsCurrencySybol() {

        fragment.setStandardCurrencySymbol("$");

        TextView view = fragment.getView().findViewById(R.id.standard_currency_symbol);
        assertThat(view.getText(), equalTo("$"));
    }

    @Test
    public void cansetValue() {

        fragment.setStandardWholeValues("1,000");

        TextView view = fragment.getView().findViewById(R.id.standard_currency_whole_values);
        assertThat(view.getText(), equalTo("1,000"));
    }

    @Test
    public void canSetFractionValues() {
        fragment.setStandardSubValues("59");

        TextView view = fragment.getView().findViewById(R.id.standard_currency_sub_values);
        assertThat(view.getText(), equalTo("59"));
    }

    @Test
    public void canShowLargeDelimeter() {
        assertThat(fragment.getView().findViewById(R.id.standard_currency_sub_delimiter_large).getVisibility(), equalTo(View.GONE));

        fragment.showStandardDelimeterLarge();

        assertThat(fragment.getView().findViewById(R.id.standard_currency_sub_delimiter_large).getVisibility(), equalTo(View.VISIBLE));
    }

    @Test
    public void canHideLargeDelimeter() {
        fragment.showStandardDelimeterLarge();
        fragment.hideStandardDelimeter();

        assertThat(fragment.getView().findViewById(R.id.standard_currency_sub_delimiter_large).getVisibility(), equalTo(View.GONE));
    }

    @Test
    public void canShowSmallDelimeter() {
        assertThat(fragment.getView().findViewById(R.id.standard_currency_sub_delimiter).getVisibility(), equalTo(View.GONE));
        assertThat(fragment.getView().findViewById(R.id.standard_currency_sub_values).getVisibility(), equalTo(View.GONE));

        fragment.showStandardDelimeter();

        assertThat(fragment.getView().findViewById(R.id.standard_currency_sub_delimiter).getVisibility(), equalTo(View.VISIBLE));
        assertThat(fragment.getView().findViewById(R.id.standard_currency_sub_values).getVisibility(), equalTo(View.VISIBLE));
    }

    @Test
    public void canHideSmallDelimeter() {
        fragment.showStandardDelimeter();
        fragment.hideStandardDelimeter();

        assertThat(fragment.getView().findViewById(R.id.standard_currency_sub_delimiter).getVisibility(), equalTo(View.GONE));
        assertThat(fragment.getView().findViewById(R.id.standard_currency_sub_values).getVisibility(), equalTo(View.GONE));
    }

    @Test
    public void canSetCalculatedCurrency() {
        fragment.updateAlternitiveCurrency("BTC: 1.001");

        TextView view = fragment.getView().findViewById(R.id.converted_currency);
        assertThat(view.getText(), equalTo("BTC: 1.001"));
    }

    @Test
    public void forwardsPriceChanges() {
        USDCurrency evaluationCurrency = new USDCurrency("400.00");
        fragment.onPriceRecieved(evaluationCurrency);

        verify(presenter, times(1)).setEvaluationCurrency(evaluationCurrency);
    }

    @Test
    public void fetchesWholeValues() {
        TextView view = fragment.getView().findViewById(R.id.standard_currency_whole_values);
        view.setText("1,000");

        assertThat(fragment.getWholeValues(), equalTo("1,000"));
    }

    @Test
    public void fetchesSubValues() {
        TextView view = fragment.getView().findViewById(R.id.standard_currency_sub_values);
        view.setText("99");

        assertThat(fragment.getSubValues(), equalTo("99"));

    }
}