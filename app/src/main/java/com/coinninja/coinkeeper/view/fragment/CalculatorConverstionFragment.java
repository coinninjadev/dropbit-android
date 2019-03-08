package com.coinninja.coinkeeper.view.fragment;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.presenter.CurrencyConversionPresenter;
import com.coinninja.coinkeeper.presenter.LargeCurrencyConverstionPresenter;
import com.coinninja.coinkeeper.ui.base.BaseFragment;
import com.coinninja.coinkeeper.util.Keys;
import com.coinninja.coinkeeper.util.currency.Currency;

import androidx.annotation.Nullable;


public class CalculatorConverstionFragment extends BaseFragment implements KeyboardFragment.OnKeyPressListener, CurrencyConversionPresenter.View {
    private CurrencyConversionPresenter presenter;
    private Currency evaluationCurrency = null;
    private Currency currentCurrencyState;


    public static CalculatorConverstionFragment newInstance(Currency currentCurrencyState) {
        CalculatorConverstionFragment fragment = new CalculatorConverstionFragment();

        if (currentCurrencyState.getMaxNumSubValues() > 2) {
            fragment.setPresenter(new LargeCurrencyConverstionPresenter(currentCurrencyState));
        } else {
            fragment.setPresenter(new CurrencyConversionPresenter(currentCurrencyState));
        }

        fragment.setCurrentCurrencyState(currentCurrencyState);
        fragment.currentCurrencyState = currentCurrencyState;

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_calculator_conversion_layout, container, false);
        return inflate;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (presenter != null) {
            presenter.attach(this);
        }
    }

    @Override
    public void setStandardCurrencySymbol(String symbol) {
        TextView symbolText = ((TextView) getView().findViewById(R.id.standard_currency_symbol));
        ImageView symbolImage = ((ImageView) getView().findViewById(R.id.btc_currency_symbol));

        symbolText.setText(symbol);
        symbolText.setVisibility(View.VISIBLE);

        symbolImage.setVisibility(View.GONE);
    }

    @Override
    public void setBTCCurrencySymbol(String symbol) {
        TextView symbolText = ((TextView) getView().findViewById(R.id.standard_currency_symbol));
        ImageView symbolImage = ((ImageView) getView().findViewById(R.id.btc_currency_symbol));

        symbolText.setText("");
        symbolText.setVisibility(View.GONE);

        symbolImage.setVisibility(View.VISIBLE);
    }

    @Override
    public void setStandardWholeValues(String wholeValues) {
        TextView view = getView().findViewById(R.id.standard_currency_whole_values);
        scaleInput(wholeValues.length());

        view.setText(wholeValues);
    }

    @Override
    public void setStandardSubValues(String subValues) {
        ((TextView) getView().findViewById(R.id.standard_currency_sub_values)).setText(subValues);
    }

    @Override
    public void onKeyPress(Keys which) {
        presenter.onInput(which);
    }

    @Override
    public void invalidInput() {
        Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.shake_view);
        Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        View view = getView().findViewById(R.id.standard_currency);
        view.startAnimation(animation);
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                vibrator.cancel();
            }
        }, 250);
        long[] pattern = {25, 100, 25, 100};
        vibrator.vibrate(pattern, 0);
    }

    @Override
    public void showStandardDelimeter() {
        getView().findViewById(R.id.standard_currency_sub_delimiter).setVisibility(View.VISIBLE);
        getView().findViewById(R.id.standard_currency_sub_values).setVisibility(View.VISIBLE);
    }

    @Override
    public void showStandardDelimeterLarge() {
        getView().findViewById(R.id.standard_currency_sub_delimiter_large).setVisibility(View.VISIBLE);
    }


    @Override
    public void hideStandardDelimeter() {
        getView().findViewById(R.id.standard_currency_sub_delimiter).setVisibility(View.GONE);
        getView().findViewById(R.id.standard_currency_sub_delimiter_large).setVisibility(View.GONE);
        getView().findViewById(R.id.standard_currency_sub_values).setVisibility(View.GONE);
    }

    @Override
    public void updateAlternitiveCurrency(String value) {
        ((TextView) getView().findViewById(R.id.converted_currency)).setText(value);
    }

    @Override
    public String getSubValues() {
        return ((TextView) getView().findViewById(R.id.standard_currency_sub_values)).getText().toString();
    }

    @Override
    public String getWholeValues() {
        return ((TextView) getView().findViewById(R.id.standard_currency_whole_values)).getText().toString();
    }

    @SuppressLint("ResourceType")
    private void scaleInput(int numDidgets) {
        TypedArray large_fonts = getResources().obtainTypedArray(R.array.calc_large_font_size);
        TypedArray small_fonts = getResources().obtainTypedArray(R.array.calc_small_font_size);
        TextView wholeValues = getView().findViewById(R.id.standard_currency_whole_values);
        TextView subValues = getView().findViewById(R.id.standard_currency_sub_values);
        TextView delimeter = getView().findViewById(R.id.standard_currency_sub_delimiter);
        TextView largeDelimeter = getView().findViewById(R.id.standard_currency_sub_delimiter_large);
        TextView currencySymbol = getView().findViewById(R.id.standard_currency_symbol);

        float large = large_fonts.getFloat(numDidgets - 1, 80f);
        float small = small_fonts.getFloat(numDidgets - 1, 50f);

        wholeValues.setTextSize(large);
        currencySymbol.setTextSize(small);
        subValues.setTextSize(small);
        delimeter.setTextSize(small);
        largeDelimeter.setTextSize(large);

        large_fonts.recycle();
        small_fonts.recycle();
    }


    public void setCurrentCurrencyState(Currency currentCurrencyState) {
        this.currentCurrencyState = currentCurrencyState;
    }

    public Currency getCurrentCurrencyState() {
        return presenter.getCurrentState();
    }

    public void setPresenter(CurrencyConversionPresenter presenter) {
        this.presenter = presenter;
    }

    public CurrencyConversionPresenter getPresenter() {
        return presenter;
    }

    public void onPriceRecieved(Currency evaluationCurrency) {
        this.evaluationCurrency = evaluationCurrency;
        presenter.setEvaluationCurrency(evaluationCurrency);
    }

    public Currency getEvaluationCurrency() {
        return evaluationCurrency;
    }
}
