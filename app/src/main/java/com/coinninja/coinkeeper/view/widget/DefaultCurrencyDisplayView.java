package com.coinninja.coinkeeper.view.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.coinninja.android.helpers.Views;
import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.ui.transaction.history.DefaultCurrencyChangeObserver;
import com.coinninja.coinkeeper.util.DefaultCurrencies;
import com.coinninja.coinkeeper.util.currency.CryptoCurrency;
import com.coinninja.coinkeeper.util.currency.FiatCurrency;
import com.coinninja.coinkeeper.view.adapter.util.BindableTransaction;

import androidx.annotation.Nullable;

public class DefaultCurrencyDisplayView extends LinearLayout implements DefaultCurrencyChangeObserver {

    protected TextView secondaryCurrencyView;
    protected TextView primaryCurrencyView;
    protected DefaultCurrencies defaultCurrencies;
    protected BindableTransaction.SendState sendState;
    protected CryptoCurrency totalCrypto;
    protected FiatCurrency fiatValue;
    protected boolean useCryptoIcon = false;
    protected boolean useCryptoSymbol = false;
    protected int receivedTextAppearance = R.style.TextAppearance_History_Receive_Value;
    protected int sentTextAppearance = R.style.TextAppearance_History_Send_Value;
    protected int secondaryTextAppearance = R.style.TextAppearance_History_Currency;
    protected float primaryIconScale = 1;
    protected float secondaryIconScale = .8F;

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
        commonInit(context, attrs, defStyleAttr, defStyleRes);
    }

    protected int getLayoutId() {
        return R.layout.merge_default_currency_display_view;
    }

    protected void commonInit(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        LayoutInflater.from(context).inflate(getLayoutId(), this, true);
        setOrientation(VERTICAL);
        primaryCurrencyView = findViewById(R.id.primary_currency);
        secondaryCurrencyView = findViewById(R.id.secondary_currency);
        parseAttributes(context, attrs, defStyleAttr);
    }

    public void useCryptoIcon(boolean useCryptoIcon) {
        this.useCryptoIcon = useCryptoIcon;
    }

    public void useCryptoSymbol(boolean useCryptoSymbol) {
        this.useCryptoSymbol = useCryptoSymbol;
    }

    public void renderValues(DefaultCurrencies defaultCurrencies, CryptoCurrency totalCrypto, FiatCurrency fiatValue) {
        renderValues(defaultCurrencies, BindableTransaction.SendState.RECEIVE, totalCrypto, fiatValue);
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

    @Override
    public void onDefaultCurrencyChanged(DefaultCurrencies defaultCurrencies) {
        setDefaultCurrencyPreference(defaultCurrencies);
    }

    protected void parseAttributes(Context context, AttributeSet attrs, int defStyleAttr) {
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.DefaultCurrencyDisplayView, defStyleAttr, 0);
            if (typedArray.hasValue(R.styleable.DefaultCurrencyDisplayView_receivedTextAppearance)) {
                receivedTextAppearance = typedArray.getResourceId(R.styleable.DefaultCurrencyDisplayView_receivedTextAppearance, receivedTextAppearance);
            }

            if (typedArray.hasValue(R.styleable.DefaultCurrencyDisplayView_sentTextAppearance)) {
                sentTextAppearance = typedArray.getResourceId(R.styleable.DefaultCurrencyDisplayView_sentTextAppearance, sentTextAppearance);
            }

            if (typedArray.hasValue(R.styleable.DefaultCurrencyDisplayView_secondaryTextAppearance)) {
                secondaryTextAppearance = typedArray.getResourceId(R.styleable.DefaultCurrencyDisplayView_secondaryTextAppearance, secondaryTextAppearance);
            }

            if (typedArray.hasValue(R.styleable.DefaultCurrencyDisplayView_useCryptoIcon)) {
                useCryptoIcon = typedArray.getBoolean(R.styleable.DefaultCurrencyDisplayView_useCryptoIcon, useCryptoIcon);
            }

            if (typedArray.hasValue(R.styleable.DefaultCurrencyDisplayView_useCryptoSymbol)) {
                useCryptoSymbol = typedArray.getBoolean(R.styleable.DefaultCurrencyDisplayView_useCryptoSymbol, useCryptoSymbol);
            }


            if (typedArray.hasValue(R.styleable.DefaultCurrencyDisplayView_primaryCryptoIconScale)) {
                primaryIconScale = typedArray.getFloat(R.styleable.DefaultCurrencyDisplayView_primaryCryptoIconScale, primaryIconScale);
            }

            if (typedArray.hasValue(R.styleable.DefaultCurrencyDisplayView_secondaryCryptoIconScale)) {
                secondaryIconScale = typedArray.getFloat(R.styleable.DefaultCurrencyDisplayView_secondaryCryptoIconScale, secondaryIconScale);
            }

            typedArray.recycle();
        }
    }

    protected void invalidateValues() {
        invalidateCrypto();
        invalidateFiat();
        formatDirection();
    }

    protected void formatDirection() {
        switch (sendState) {
            case RECEIVE:
                styleTextView(primaryCurrencyView, receivedTextAppearance);
                break;
            case SEND:
            case TRANSFER:
                styleTextView(primaryCurrencyView, sentTextAppearance);
                break;
            default:
        }
    }

    protected void styleTextView(TextView textView, int resId) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            textView.setTextAppearance(getContext(), resId);
        } else {
            textView.setTextAppearance(resId);
        }
    }

    protected void invalidateCrypto() {
        TextView cryptoView = getCryptoView();
        String formattedCryptoValue = getFormattedCryptoValue();
        if (useCryptoIcon) {
            Views.renderBTCIconOnCurrencyViewPair(getContext(), defaultCurrencies, primaryCurrencyView,
                    primaryIconScale, secondaryCurrencyView, secondaryIconScale);
        } else if (useCryptoSymbol) {
            formattedCryptoValue = String.format("%s %s", defaultCurrencies.getCrypto().getSymbol(), formattedCryptoValue);
        }
        cryptoView.setText(formattedCryptoValue);
    }

    protected void invalidateFiat() {
        getFiatView().setText(getFormattedFiatValue());
    }

    protected String getFormattedCryptoValue() {
        totalCrypto.setCurrencyFormat(CryptoCurrency.NO_SYMBOL_FORMAT);
        return totalCrypto.toFormattedCurrency();
    }

    protected String getFormattedFiatValue() {
        return fiatValue.toFormattedCurrency();
    }

    protected TextView getFiatView() {
        return defaultCurrencies.getPrimaryCurrency().isFiat() ? primaryCurrencyView : secondaryCurrencyView;
    }

    protected TextView getCryptoView() {
        return defaultCurrencies.getPrimaryCurrency().isCrypto() ? primaryCurrencyView : secondaryCurrencyView;
    }
}
