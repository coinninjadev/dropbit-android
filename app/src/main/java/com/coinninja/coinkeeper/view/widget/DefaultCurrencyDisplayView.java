package com.coinninja.coinkeeper.view.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.ui.transaction.history.DefaultCurrencyChangeObserver;
import com.coinninja.coinkeeper.util.DefaultCurrencies;
import com.coinninja.coinkeeper.util.currency.CryptoCurrency;
import com.coinninja.coinkeeper.util.currency.FiatCurrency;
import com.coinninja.coinkeeper.view.adapter.util.BindableTransaction;

import androidx.annotation.Nullable;

public class DefaultCurrencyDisplayView extends LinearLayout implements DefaultCurrencyChangeObserver {

    private TextView secondaryCurrencyView;
    private TextView primaryCurrencyView;
    private DefaultCurrencies defaultCurrencies;
    private BindableTransaction.SendState sendState;
    private CryptoCurrency totalCrypto;
    private FiatCurrency fiatValue;
    private boolean showPositiveChange = true;
    private int receivedTextAppearance = R.style.TextAppearance_History_Receive_Value;
    private int sentTextAppearance = R.style.TextAppearance_History_Send_Value;

    public DefaultCurrencyDisplayView(Context context) {
        this(context, null);
    }

    public DefaultCurrencyDisplayView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DefaultCurrencyDisplayView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public DefaultCurrencyDisplayView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.merge_default_currency_display_view, this, true);
        primaryCurrencyView = findViewById(R.id.primary_currency);
        secondaryCurrencyView = findViewById(R.id.secondary_currency);
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.DefaultCurrencyDisplayView, defStyleAttr, 0);
            if (typedArray.hasValue(R.styleable.DefaultCurrencyDisplayView_receivedTextAppearance)) {
                receivedTextAppearance = typedArray.getResourceId(R.styleable.DefaultCurrencyDisplayView_receivedTextAppearance, receivedTextAppearance);
            }

            if (typedArray.hasValue(R.styleable.DefaultCurrencyDisplayView_sentTextAppearance)) {
                sentTextAppearance = typedArray.getResourceId(R.styleable.DefaultCurrencyDisplayView_sentTextAppearance, sentTextAppearance);
            }

            typedArray.recycle();
        }
    }

    public CryptoCurrency getTotalCrypto() {
        return totalCrypto;
    }

    public FiatCurrency getFiatValue() {
        return fiatValue;
    }

    public void renderValues(DefaultCurrencies defaultCurrencies, BindableTransaction.SendState sendState, CryptoCurrency totalCrypto, FiatCurrency fiatValue) {
        this.defaultCurrencies = defaultCurrencies;
        this.sendState = sendState;
        this.totalCrypto = totalCrypto;
        this.fiatValue = fiatValue;
        invalidateValues();
    }

    public String getPrimaryCurrencyText() {
        return primaryCurrencyView.getText().toString();
    }

    public String getSecondaryCurrencyText() {
        return secondaryCurrencyView.getText().toString();
    }

    public void setDefaultCurrencyPreference(DefaultCurrencies defaultCurrencies) {
        this.defaultCurrencies = defaultCurrencies;
        invalidateValues();
    }

    public void setShowPositiveChange(boolean showPositiveChange) {
        this.showPositiveChange = showPositiveChange;
        invalidateValues();
    }

    @Override
    public void onDefaultCurrencyChanged(DefaultCurrencies defaultCurrencies) {
        setDefaultCurrencyPreference(defaultCurrencies);
    }

    private void invalidateValues() {
        invalidateCrypto();
        invalidateFiat();
        formatDirection();
    }

    private void formatDirection() {
        switch (sendState) {
            case RECEIVE:
                stylePrimary(receivedTextAppearance);
                if (showPositiveChange)
                    primaryCurrencyView.setText(String.format("+ %s", getPrimaryCurrencyText()));
                break;
            case SEND:
            case TRANSFER:
                stylePrimary(sentTextAppearance);
                primaryCurrencyView.setText(String.format("- %s", getPrimaryCurrencyText()));
                break;
            default:
        }
    }

    public void stylePrimary(int resId) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            primaryCurrencyView.setTextAppearance(getContext(), resId);
        } else {
            primaryCurrencyView.setTextAppearance(resId);
        }
    }

    private void invalidateCrypto() {
        getCryptoView().setText(getFormattedCryptoValue());
    }

    private void invalidateFiat() {
        getFiatView().setText(getFormattedFiatValue());
    }

    private String getFormattedCryptoValue() {
        totalCrypto.setCurrencyFormat(CryptoCurrency.NO_SYMBOL_FORMAT);
        return totalCrypto.toFormattedCurrency();
    }

    private String getFormattedFiatValue() {
        return fiatValue.toFormattedCurrency();
    }

    private TextView getFiatView() {
        return defaultCurrencies.getPrimaryCurrency().isFiat() ? primaryCurrencyView : secondaryCurrencyView;
    }

    private TextView getCryptoView() {
        return defaultCurrencies.getPrimaryCurrency().isCrypto() ? primaryCurrencyView : secondaryCurrencyView;
    }
}
