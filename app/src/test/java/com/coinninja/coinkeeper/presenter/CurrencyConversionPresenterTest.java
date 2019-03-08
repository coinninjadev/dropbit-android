package com.coinninja.coinkeeper.presenter;

import com.coinninja.coinkeeper.util.Keys;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.currency.USDCurrency;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CurrencyConversionPresenterTest {
    private final String valueOfBTC = "6200";

    private CurrencyConversionPresenter.View view;
    private CurrencyConversionPresenter presenter;
    private USDCurrency currencyState = new USDCurrency();

    @Before
    public void setUp() throws Exception {
        view = mock(CurrencyConversionPresenter.View.class);
        presenter = new CurrencyConversionPresenter(currencyState);
        presenter.attach(view);
        presenter.setEvaluationCurrency(new USDCurrency(valueOfBTC));
    }


    @Test
    public void doesNotInitCurrencyWhenBTCisEmpty() {
        currencyState = new BTCCurrency().toUSD(new USDCurrency("600"));
        presenter = new CurrencyConversionPresenter(currencyState);
        presenter.setEvaluationCurrency(new USDCurrency("600"));

        presenter.attach(view);

        verify(view, times(0)).setStandardWholeValues(any());
    }

    @Test
    public void doesNotInitViewWhenEvalueationCurrencyisNull() {
        currencyState = new USDCurrency("100");
        presenter = new CurrencyConversionPresenter(currencyState);
        presenter.setEvaluationCurrency(null);

        presenter.attach(view);

        verify(view, times(0)).setStandardWholeValues(any());
    }

    @Test
    public void initsViewWithConvertedCurrency() {
        currencyState = new USDCurrency("100");
        presenter = new CurrencyConversionPresenter(currencyState);
        presenter.setEvaluationCurrency(new USDCurrency("600"));
        presenter.attach(view);

        verify(view).setStandardWholeValues("100");
        verify(view).setStandardSubValues("00");
        verify(view).showStandardDelimeter();
        verify(view).updateAlternitiveCurrency(new BTCCurrency(0.16666667).toFormattedCurrency());
    }

    @Test
    public void instructsViewToSetCurrencySymbolWhenAttached() {
        verify(view, times(1)).setStandardCurrencySymbol("$");
    }

    @Test
    public void canAddValuesToLeft() {
        when(view.getWholeValues()).thenReturn("9");
        when(view.getSubValues()).thenReturn("");

        presenter.onInput(Keys.EIGHT);

        verify(view).setStandardWholeValues("98");
        verify(view, times(2)).updateAlternitiveCurrency(any());
    }

    @Test
    public void toManyValuesToLeftIsNotValid() {
        when(view.getWholeValues()).thenReturn("9999999999");

        presenter.onInput(Keys.EIGHT);

        verify(view).invalidInput();
    }

    @Test
    public void canAddValuesToRight() {
        presenter.currentMode = CurrencyConversionPresenter.Mode.RIGHT_OF_DELIM;
        when(view.getWholeValues()).thenReturn("100");
        when(view.getSubValues()).thenReturn("");

        presenter.onInput(Keys.EIGHT);

        verify(view).setStandardSubValues("8");
        verify(view).updateAlternitiveCurrency(new BTCCurrency(0.01612903).toFormattedCurrency());
    }

    @Test
    public void toManyValuesToRightIsNotValid() {
        presenter.currentMode = CurrencyConversionPresenter.Mode.RIGHT_OF_DELIM;
        when(view.getWholeValues()).thenReturn("100");
        when(view.getSubValues()).thenReturn("67");

        presenter.onInput(Keys.EIGHT);

        verify(view).invalidInput();
    }

    @Test
    public void singleDotTogglesMode() {
        when(view.getWholeValues()).thenReturn("100");

        presenter.onInput(Keys.DOT);

        assertThat(presenter.currentMode, equalTo(CurrencyConversionPresenter.Mode.RIGHT_OF_DELIM));
        verify(view).showStandardDelimeter();
    }

    @Test
    public void doesNotAllowMultipleDots() {
        presenter.currentMode = CurrencyConversionPresenter.Mode.RIGHT_OF_DELIM;

        presenter.onInput(Keys.DOT);

        verify(view).invalidInput();
    }

    @Test
    public void backPopsOffOfLeft() {
        when(view.getWholeValues()).thenReturn("100");

        presenter.onInput(Keys.BACK);

        verify(view).setStandardWholeValues("10");
        verify(view).updateAlternitiveCurrency(new BTCCurrency("0.00161290").toFormattedCurrency());
    }

    @Test
    public void backPopsOffOfRight() {
        presenter.currentMode = CurrencyConversionPresenter.Mode.RIGHT_OF_DELIM;
        when(view.getWholeValues()).thenReturn("100");
        when(view.getSubValues()).thenReturn("98");

        presenter.onInput(Keys.BACK);

        verify(view).setStandardSubValues("9");
        verify(view).updateAlternitiveCurrency(new BTCCurrency(0.01628710).toFormattedCurrency());
    }

    @Test
    public void backTogglesMode() {
        presenter.currentMode = CurrencyConversionPresenter.Mode.RIGHT_OF_DELIM;
        when(view.getSubValues()).thenReturn("");

        presenter.onInput(Keys.BACK);

        assertThat(presenter.currentMode, equalTo(CurrencyConversionPresenter.Mode.LEFT_OF_DELIM));
        verify(view).hideStandardDelimeter();
    }

    @Test
    public void trimsInitalZeroWhenSet() {
        when(view.getWholeValues()).thenReturn("0");

        presenter.onInput(Keys.ONE);

        verify(view).setStandardWholeValues("1");
    }

    @Test
    public void clearAllClearsAllInputLeftMode() {
        presenter.currentMode = CurrencyConversionPresenter.Mode.LEFT_OF_DELIM;

        presenter.onInput(Keys.CLEAR);

        assertThat(presenter.currentMode, equalTo(CurrencyConversionPresenter.Mode.LEFT_OF_DELIM));
        verify(view, times(1)).setStandardWholeValues("0");
        verify(view).setStandardSubValues("");
        verify(view, times(2)).updateAlternitiveCurrency(new BTCCurrency().toFormattedCurrency());
        verify(view).hideStandardDelimeter();
        verify(view).invalidInput();
    }

    @Test
    public void clearAllClearsAllInputRightMode() {
        presenter.currentMode = CurrencyConversionPresenter.Mode.RIGHT_OF_DELIM;

        presenter.onInput(Keys.CLEAR);

        assertThat(presenter.currentMode, equalTo(CurrencyConversionPresenter.Mode.LEFT_OF_DELIM));
        verify(view, times(1)).setStandardWholeValues("0");
        verify(view).setStandardSubValues("");
        verify(view, times(2)).updateAlternitiveCurrency(new BTCCurrency().toFormattedCurrency());
        verify(view).hideStandardDelimeter();
        verify(view).invalidInput();
    }

    @Test
    public void wholeNumbersAreFormmated() {
        when(view.getWholeValues()).thenReturn("100");

        presenter.onInput(Keys.EIGHT);

        verify(view).setStandardWholeValues("1,008");
    }

    @Test
    public void leadingZerosIsInvalid() {
        when(view.getWholeValues()).thenReturn("0");

        presenter.onInput(Keys.ZERO);

        verify(view).invalidInput();
    }

    @Test
    public void backOn9Clears9() {
        when(view.getWholeValues()).thenReturn("9");

        presenter.onInput(Keys.BACK);

        verify(view, times(1)).setStandardWholeValues("0");
        verify(view, times(0)).invalidInput();
    }

    @Test
    public void backOnEmptyIsInvalid() {
        when(view.getWholeValues()).thenReturn("");

        presenter.onInput(Keys.BACK);

        verify(view).invalidInput();
    }

    @Test
    public void backOnZeroIsInvalid() {
        when(view.getWholeValues()).thenReturn("0");

        presenter.onInput(Keys.BACK);

        verify(view).invalidInput();
    }

    @Test
    public void backWithLargeFormatUpdatesFormat() {
        when(view.getWholeValues()).thenReturn("10,000");

        presenter.onInput(Keys.BACK);

        verify(view).setStandardWholeValues("1,000");
    }
}
