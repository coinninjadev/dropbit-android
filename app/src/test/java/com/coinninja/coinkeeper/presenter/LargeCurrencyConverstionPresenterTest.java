package com.coinninja.coinkeeper.presenter;

import com.coinninja.coinkeeper.presenter.CurrencyConversionPresenter.View;
import com.coinninja.coinkeeper.util.Keys;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.currency.USDCurrency;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LargeCurrencyConverstionPresenterTest {

    private BTCCurrency currencyState;

    @Mock
    View view;
    private LargeCurrencyConverstionPresenter presenter;

    @Before
    public void setUp() {
        currencyState = new BTCCurrency();
        presenter = new LargeCurrencyConverstionPresenter(currencyState);
        presenter.setEvaluationCurrency(new USDCurrency("600"));
        presenter.attach(view);
    }

    @Test
    public void zero_dot_zero_with_zero() {
        when(view.getWholeValues()).thenReturn("0.0");

        presenter.appendToSub("0");

        verify(view).setStandardWholeValues("0.00");
    }

    @Test
    public void handle_btc_0_dot_0() {
        when(view.getWholeValues()).thenReturn("0");
        presenter.appendToSub("0");

        verify(view).setStandardWholeValues("0.0");
    }

    @Test
    public void doesNotInitCurrencyWhenBTCisEmpty() {
        currencyState = new BTCCurrency();
        presenter = new LargeCurrencyConverstionPresenter(currencyState);
        presenter.setEvaluationCurrency(new USDCurrency("600"));

        presenter.attach(view);

        verify(view, times(0)).updateAlternitiveCurrency(any());
    }

    @Test
    public void doesNotInitViewWhenEvalueationCurrencyisNull() {
        currencyState = new BTCCurrency("1");
        presenter = new LargeCurrencyConverstionPresenter(currencyState);
        presenter.setEvaluationCurrency(null);

        presenter.attach(view);

        verify(view, times(0)).updateAlternitiveCurrency(any());
    }

    @Test
    public void initsViewWithConvertedCurrency() {
        currencyState = new BTCCurrency("1");
        presenter = new LargeCurrencyConverstionPresenter(currencyState);
        presenter.setEvaluationCurrency(new USDCurrency("600"));
        presenter.attach(view);

        verify(view).setStandardWholeValues("1");
        verify(view).updateAlternitiveCurrency("$600.00");
    }

    @Test
    public void initsViewWithConvertedCurrencyTrimmingZeros() {
        currencyState = new BTCCurrency("1.0002130");
        presenter = new LargeCurrencyConverstionPresenter(currencyState);
        presenter.setEvaluationCurrency(new USDCurrency("600"));
        presenter.attach(view);

        verify(view).setStandardWholeValues("1.000213");
    }

    @Test
    public void canAddValuesToLeft() {
        when(view.getWholeValues()).thenReturn("9");

        presenter.onInput(Keys.EIGHT);

        verify(view).setStandardWholeValues("98");
        verify(view).updateAlternitiveCurrency(any());
    }

    @Test
    public void showingDelimShowsLargeDelim() {
        when(view.getWholeValues()).thenReturn("9");

        presenter.onInput(Keys.DOT);


        verify(view).showStandardDelimeterLarge();
        verify(view, times(0)).showStandardDelimeter();
    }

    @Test
    public void addsDecimalWHenRightOfDelim() {
        presenter.currentMode = CurrencyConversionPresenter.Mode.RIGHT_OF_DELIM;
        when(view.getWholeValues()).thenReturn("9");

        presenter.onInput(Keys.EIGHT);

        verify(view).setStandardWholeValues("9.8");
    }

    @Test
    public void hidesDelimiterOnceSubValuesAreEntered() {
        presenter.currentMode = CurrencyConversionPresenter.Mode.RIGHT_OF_DELIM;
        when(view.getWholeValues()).thenReturn("9");

        presenter.onInput(Keys.EIGHT);

        verify(view).hideStandardDelimeter();
    }

    @Test
    public void canBuildLongDecimals() {
        presenter.currentMode = CurrencyConversionPresenter.Mode.RIGHT_OF_DELIM;
        when(view.getWholeValues()).thenReturn("9");

        presenter.onInput(Keys.ZERO);

        verify(view).setStandardWholeValues("9.0");
    }

    @Test
    public void reachingMaxNumberOfBitCoinsTellsViewInvalid() {
        when(view.getWholeValues()).thenReturn("2,100,000");

        presenter.onInput(Keys.ZERO);

        verify(view).invalidInput();
    }

    @Test
    public void capsDecimalsWithinConstraints() {
        presenter.currentMode = CurrencyConversionPresenter.Mode.RIGHT_OF_DELIM;
        when(view.getWholeValues()).thenReturn("1.00000435");

        presenter.onInput(Keys.ZERO);

        verify(view, times(0)).setStandardWholeValues(any());
        verify(view).invalidInput();
    }

}