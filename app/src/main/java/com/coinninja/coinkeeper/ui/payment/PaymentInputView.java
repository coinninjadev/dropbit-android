package com.coinninja.coinkeeper.ui.payment;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;

import com.coinninja.android.helpers.Input;
import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.model.PaymentHolder;
import com.coinninja.coinkeeper.text.CurrencyFormattingTextWatcher;
import com.coinninja.coinkeeper.util.currency.CryptoCurrency;
import com.coinninja.coinkeeper.util.currency.Currency;

import static com.coinninja.android.helpers.Views.clearCompoundDrawablesOn;
import static com.coinninja.android.helpers.Views.renderBTCIconOnCurrencyViewPair;
import static com.coinninja.android.helpers.Views.shakeInError;
import static com.coinninja.android.helpers.Views.withId;

public class PaymentInputView extends ConstraintLayout implements CurrencyFormattingTextWatcher.Callback {

    public static final double SECONDARY_SCALE = .8;
    public static final double PRIMARY_SCALE = 1d;
    private TextView secondaryCurrency;
    private EditText primaryCurrency;
    private PaymentHolder paymentHolder;
    private boolean isSendingMax = false;
    private Button sendMax;
    private CurrencyFormattingTextWatcher watcher;
    private OnSendMaxObserver onSendMaxObserver;
    private OnSendMaxClearedObserver sendMaxClearedObserver;
    private int defaultSecondaryFontColor;
    private int cryptoFontColor;

    public PaymentInputView(Context context) {
        this(context, null);
    }

    public PaymentInputView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PaymentInputView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.merge_component_payment_input_view, this, true);
        init();
    }

    public void setOnSendMaxObserver(OnSendMaxObserver onSendMaxObserver) {
        this.onSendMaxObserver = onSendMaxObserver;
    }

    public void setPaymentHolder(PaymentHolder paymentHolder) {
        this.paymentHolder = paymentHolder;
        onPaymentHolderChanged();
    }

    @Override
    public void onValid(Currency currency) {
        if (paymentHolder == null) return;

        paymentHolder.updateValue(currency);

        if (hasEvaluationCurrency())
            updateSecondaryCurrencyWith(paymentHolder.getSecondaryCurrency());
    }

    @Override
    public void onInvalid(String text) {
        shakeInError(primaryCurrency);
    }

    @Override
    public void onZeroed() {
        sendMax.setVisibility(VISIBLE);
        clearSendMaxIfNessisary();
    }

    @Override
    public void onInput() {
        sendMax.setVisibility(GONE);
        clearSendMaxIfNessisary();
    }

    @Override
    public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
        if (direction == View.FOCUS_DOWN) {
            return requestFocusIfZero();
        }
        return false;
    }

    public void setOnSendMaxClearedObserver(OnSendMaxClearedObserver sendMaxClearedObserver) {
        this.sendMaxClearedObserver = sendMaxClearedObserver;
    }

    private void clearSendMaxIfNessisary() {
        if (isSendingMax) {
            notifyOfClearedSendMax();
            isSendingMax = false;
        }
    }

    private void onSendMax() {
        if (onSendMaxObserver != null) {
            isSendingMax = true;
            sendMax.setVisibility(GONE);
            onSendMaxObserver.onSendMax();
        }
    }

    private void notifyOfClearedSendMax() {
        if (sendMaxClearedObserver != null && isSendingMax) {
            sendMaxClearedObserver.onSendMaxCleared();
            isSendingMax = false;
        }
    }

    private boolean requestFocusIfZero() {
        boolean focusRequested = false;
        if (paymentHolder != null && paymentHolder.getPrimaryCurrency().toLong() > 0) {
            primaryCurrency.clearFocus();
        } else {
            focusRequested = focusOnPrimary();
        }

        return focusRequested;
    }

    private void init() {
        primaryCurrency = withId(this, R.id.primary_currency);
        secondaryCurrency = withId(this, R.id.secondary_currency);
        defaultSecondaryFontColor = secondaryCurrency.getCurrentTextColor();
        cryptoFontColor = ResourcesCompat.getColor(getResources(), R.color.bitcoin_orange, getContext().getTheme());
        sendMax = withId(this, R.id.send_max);
        secondaryCurrency.setVisibility(GONE);
        watcher = new CurrencyFormattingTextWatcher();
        watcher.setCallback(this);
        primaryCurrency.addTextChangedListener(watcher);
        setOnClickListener(v -> focusOnPrimary());
        sendMax.setOnClickListener(v -> onSendMax());
    }

    private void togglePrimaryCurrencies() {
        paymentHolder.toggleCurrencies();
        onPaymentHolderChanged();
    }

    private boolean focusOnPrimary() {
        primaryCurrency.postDelayed(() -> Input.INSTANCE.showKeyboard(primaryCurrency), 100);
        boolean requestedFocus = primaryCurrency.requestFocus();
        if (requestedFocus) {
            primaryCurrency.setSelection(primaryCurrency.getText().length());
        }
        return requestedFocus;
    }

    private void onPaymentHolderChanged() {
        if (paymentHolder == null) return;
        watcher.setCurrency(paymentHolder.getPrimaryCurrency());

        setPrimaryCurrencyWithoutNotifying();

        if (hasEvaluationCurrency()) {
            updateSecondaryCurrencyWith(paymentHolder.getSecondaryCurrency());
            configureToggleCurrencyButton();
        }
        invalidateSymbol();
    }

    private void setPrimaryCurrencyWithoutNotifying() {
        primaryCurrency.removeTextChangedListener(watcher);
        updatePrimaryCurrencyWith(paymentHolder.getPrimaryCurrency());
        if (!paymentHolder.getPrimaryCurrency().isZero()) {
            sendMax.setVisibility(GONE);
        }
        primaryCurrency.addTextChangedListener(watcher);
    }

    private void updatePrimaryCurrencyWith(Currency currency) {
        if (currency.isCrypto()) {
            currency.setCurrencyFormat(CryptoCurrency.NO_SYMBOL_FORMAT);
        }

        if (currency.isZero()) {
            if (currency.isCrypto()) {
                primaryCurrency.setText(currency.toFormattedCurrency());
            } else {
                primaryCurrency.setText(String.format("%s0", currency.getSymbol()));
            }
            onZeroed();
        } else {
            primaryCurrency.setText(currency.toFormattedCurrency());
        }
        primaryCurrency.setSelection(primaryCurrency.getText().length());
    }

    private void updateSecondaryCurrencyWith(Currency value) {
        if (value.isCrypto()) {
            value.setCurrencyFormat(CryptoCurrency.NO_SYMBOL_FORMAT);
            secondaryCurrency.setTextColor(cryptoFontColor);
        } else {
            secondaryCurrency.setTextColor(defaultSecondaryFontColor);
        }

        secondaryCurrency.setVisibility(VISIBLE);
        secondaryCurrency.setText(value.toFormattedCurrency());
    }

    private void configureToggleCurrencyButton() {
        View toggleView = withId(this, R.id.primary_currency_toggle);
        toggleView.setOnClickListener(v -> togglePrimaryCurrencies());
        toggleView.setVisibility(VISIBLE);
    }

    private void invalidateSymbol() {
        renderBTCIconOnCurrencyViewPair(getContext(), paymentHolder.getDefaultCurrencies(),
                primaryCurrency, PRIMARY_SCALE, secondaryCurrency, SECONDARY_SCALE);

        if (!paymentHolder.getPrimaryCurrency().isCrypto() && !hasEvaluationCurrency()) {
            clearCompoundDrawablesOn(secondaryCurrency);
        }
    }

    private boolean hasEvaluationCurrency() {
        return paymentHolder.getEvaluationCurrency() != null && paymentHolder.getEvaluationCurrency().toLong() > 0L;
    }

    public interface OnSendMaxObserver {
        void onSendMax();
    }

    public interface OnSendMaxClearedObserver {
        void onSendMaxCleared();
    }
}
