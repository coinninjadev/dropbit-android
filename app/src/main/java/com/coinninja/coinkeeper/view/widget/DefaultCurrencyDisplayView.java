package com.coinninja.coinkeeper.view.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.coinninja.android.helpers.Views;
import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.ui.transaction.history.DefaultCurrencyChangeObserver;
import com.coinninja.coinkeeper.util.DefaultCurrencies;
import com.coinninja.coinkeeper.util.currency.CryptoCurrency;
import com.coinninja.coinkeeper.util.currency.FiatCurrency;
import com.coinninja.coinkeeper.view.adapter.util.BindableTransaction;

public class DefaultCurrencyDisplayView extends LinearLayout implements DefaultCurrencyChangeObserver {
    protected TextView secondaryCurrencyView;
    protected TextView primaryCurrencyView;
    protected DefaultCurrencies defaultCurrencies;
    protected BindableTransaction.SendState sendState;
    protected CryptoCurrency totalCrypto;
    protected FiatCurrency fiatValue;
    protected boolean useCryptoIcon = false;
    protected boolean useCryptoSymbol = false;
    protected boolean useLargeStyles = false;
    protected int primaryForegroundColor;
    protected boolean hasPrimaryColor = false;
    protected int fiatTextColor;
    protected int cryptoTextColor;
    protected int receivedTextAppearance = R.style.TextAppearance_Balance_Primary;
    protected int sentTextAppearance = R.style.TextAppearance_Balance_Primary;
    protected int secondaryTextAppearance = R.style.TextAppearance_History_Currency;
    protected int receivedTextAppearanceLarge = R.style.TextAppearance_Balance_Primary_Large;
    protected int secondaryTextAppearanceLarge = R.style.TextAppearance_Balance_Secondary_Large;
    protected float primaryIconScale = 1F;
    protected float primaryIconLargeScale = 1.2F;
    protected float secondaryIconScale = .8F;
    private boolean isInvalidating = false;
    private Drawable receivedBackground;
    private Drawable sentBackground;
    private float verticalPadding = 0F;
    private float horizontalPadding = 0F;

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

    // NOTE: this is used by the motion scene for animated layouts
    public synchronized void setUseLargeStyles(boolean shouldUseLargeStyles) {
        boolean didChange = useLargeStyles != shouldUseLargeStyles;
        useLargeStyles = shouldUseLargeStyles;
        if (!isInvalidating && didChange) {
            isInvalidating = true;
            invalidateCrypto();
            formatDirection();
            setColors();
            isInvalidating = false;
            postDelayed(this::requestLayout, 600);
        }
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

            if (typedArray.hasValue(R.styleable.DefaultCurrencyDisplayView_receivedTextAppearanceLarge)) {
                receivedTextAppearanceLarge = typedArray.getResourceId(R.styleable.DefaultCurrencyDisplayView_receivedTextAppearanceLarge, receivedTextAppearanceLarge);
            }

            if (typedArray.hasValue(R.styleable.DefaultCurrencyDisplayView_secondaryTextAppearanceLarge)) {
                secondaryTextAppearanceLarge = typedArray.getResourceId(R.styleable.DefaultCurrencyDisplayView_secondaryTextAppearance, secondaryTextAppearanceLarge);
            }

            if (typedArray.hasValue(R.styleable.DefaultCurrencyDisplayView_cryptoTextColor)) {
                cryptoTextColor = typedArray.getColor(R.styleable.DefaultCurrencyDisplayView_cryptoTextColor, context.getResources().getColor(R.color.bitcoin_orange));
            }

            if (typedArray.hasValue(R.styleable.DefaultCurrencyDisplayView_fiatTextColor)) {
                fiatTextColor = typedArray.getColor(R.styleable.DefaultCurrencyDisplayView_fiatTextColor, context.getResources().getColor(R.color.colorPrimaryDark));
            }

            if (typedArray.hasValue(R.styleable.DefaultCurrencyDisplayView_primaryForegroundColor)) {
                primaryForegroundColor = typedArray.getColor(R.styleable.DefaultCurrencyDisplayView_primaryForegroundColor, context.getResources().getColor(R.color.font_white));
                hasPrimaryColor = true;
            }

            if (typedArray.hasValue(R.styleable.DefaultCurrencyDisplayView_primaryBackgroundSent)) {
                sentBackground = getResources().getDrawable(typedArray.getResourceId(R.styleable.DefaultCurrencyDisplayView_primaryBackgroundSent, R.drawable.primary_sent_pill)).mutate();
            }

            if (typedArray.hasValue(R.styleable.DefaultCurrencyDisplayView_primaryBackgroundReceived)) {
                receivedBackground = getResources().getDrawable(typedArray.getResourceId(R.styleable.DefaultCurrencyDisplayView_primaryBackgroundReceived, R.drawable.primary_receive_pill)).mutate();
            }

            if (typedArray.hasValue(R.styleable.DefaultCurrencyDisplayView_useCryptoIcon)) {
                useCryptoIcon = typedArray.getBoolean(R.styleable.DefaultCurrencyDisplayView_useCryptoIcon, useCryptoIcon);
            }

            if (typedArray.hasValue(R.styleable.DefaultCurrencyDisplayView_useCryptoSymbol)) {
                useCryptoSymbol = typedArray.getBoolean(R.styleable.DefaultCurrencyDisplayView_useCryptoSymbol, useCryptoSymbol);
            }

            if (typedArray.hasValue(R.styleable.DefaultCurrencyDisplayView_useLargeStyles)) {
                useLargeStyles = typedArray.getBoolean(R.styleable.DefaultCurrencyDisplayView_useLargeStyles, false);
            }

            if (typedArray.hasValue(R.styleable.DefaultCurrencyDisplayView_primaryCryptoIconLargeScale)) {
                primaryIconLargeScale = typedArray.getFloat(R.styleable.DefaultCurrencyDisplayView_primaryCryptoIconLargeScale, primaryIconLargeScale);
            }

            if (typedArray.hasValue(R.styleable.DefaultCurrencyDisplayView_primaryCryptoIconScale)) {
                primaryIconScale = typedArray.getFloat(R.styleable.DefaultCurrencyDisplayView_primaryCryptoIconScale, primaryIconScale);
            }

            if (typedArray.hasValue(R.styleable.DefaultCurrencyDisplayView_secondaryCryptoIconScale)) {
                secondaryIconScale = typedArray.getFloat(R.styleable.DefaultCurrencyDisplayView_secondaryCryptoIconScale, secondaryIconScale);
            }

            if (typedArray.hasValue(R.styleable.DefaultCurrencyDisplayView_horizontalPillPadding)) {
                horizontalPadding = typedArray.getDimension(R.styleable.DefaultCurrencyDisplayView_horizontalPillPadding, 0F);
            }

            if (typedArray.hasValue(R.styleable.DefaultCurrencyDisplayView_verticalPillPadding)) {
                verticalPadding = typedArray.getDimension(R.styleable.DefaultCurrencyDisplayView_verticalPillPadding, 0F);
            }

            typedArray.recycle();
        }
    }

    protected void invalidateValues() {
        invalidateCrypto();
        invalidateFiat();
        formatDirection();
        setColors();
    }

    protected void formatDirection() {
        switch (sendState) {
            case RECEIVE:
                if (useLargeStyles) {
                    styleTextView(primaryCurrencyView, receivedTextAppearanceLarge);
                } else {
                    styleTextView(primaryCurrencyView, receivedTextAppearance);
                }

                if (receivedBackground != null) {
                    primaryCurrencyView.setBackground(receivedBackground);
                    primaryCurrencyView.setPadding((int) horizontalPadding, (int) verticalPadding, (int) horizontalPadding, (int) verticalPadding);
                }
                break;
            case SEND:
            case TRANSFER:
                styleTextView(primaryCurrencyView, sentTextAppearance);

                if (sentBackground != null) {
                    primaryCurrencyView.setBackground(sentBackground);
                    primaryCurrencyView.setPadding((int) horizontalPadding, (int) verticalPadding, (int) horizontalPadding, (int) verticalPadding);
                }
                break;
            default:
        }

        if (useLargeStyles) {
            styleTextView(secondaryCurrencyView, secondaryTextAppearanceLarge);
        } else {
            styleTextView(secondaryCurrencyView, secondaryTextAppearance);
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
                    useLargeStyles ? primaryIconLargeScale : primaryIconScale, secondaryCurrencyView, secondaryIconScale);
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

    private void setColors() {
        getFiatView().setTextColor(fiatTextColor);
        getCryptoView().setTextColor(cryptoTextColor);

        if (hasPrimaryColor) {
            primaryCurrencyView.setTextColor(primaryForegroundColor);
        }
    }
}
