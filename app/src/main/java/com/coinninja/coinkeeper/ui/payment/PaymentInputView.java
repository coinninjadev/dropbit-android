package com.coinninja.coinkeeper.ui.payment;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.coinninja.android.helpers.Input;
import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.model.PaymentHolder;
import com.coinninja.coinkeeper.text.CurrencyFormattingTextWatcher;
import com.coinninja.coinkeeper.util.currency.CryptoCurrency;
import com.coinninja.coinkeeper.util.currency.Currency;

import androidx.constraintlayout.widget.ConstraintLayout;

import static com.coinninja.android.helpers.Resources.scaleValue;
import static com.coinninja.android.helpers.Views.shakeInError;
import static com.coinninja.android.helpers.Views.withId;

public class PaymentInputView extends ConstraintLayout implements CurrencyFormattingTextWatcher.Callback {

    private TextView secondaryCurrency;
    private EditText primaryCurrency;
    private PaymentHolder paymentHolder;
    private CurrencyFormattingTextWatcher watcher;

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

    public void setPaymentHolder(PaymentHolder paymentHolder) {
        this.paymentHolder = paymentHolder;
        onPaymentHolderChanged();
    }

    @Override
    public void onValid(Currency currency) {
        if (paymentHolder == null) return;

        paymentHolder.updateValue(currency);
        updateSecondaryCurrencyWith(paymentHolder.getSecondaryCurrency());
    }

    @Override
    public void onInvalid(String text) {
        shakeInError(primaryCurrency);
    }

    @Override
    public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
        if (direction == View.FOCUS_DOWN) {
            return requestFocusIfZero();
        }
        return false;
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
        secondaryCurrency.setVisibility(GONE);
        watcher = new CurrencyFormattingTextWatcher();
        watcher.setCallback(this);
        primaryCurrency.addTextChangedListener(watcher);
        setOnClickListener(v -> focusOnPrimary());
        withId(this, R.id.primary_currency_toggle).setOnClickListener(v -> togglePrimaryCurrencies());
    }

    private void togglePrimaryCurrencies() {
        paymentHolder.toggleCurrencies();
        onPaymentHolderChanged();
    }

    private boolean focusOnPrimary() {
        primaryCurrency.postDelayed(() -> Input.showKeyboard(primaryCurrency), 100);
        boolean requestedFocus = primaryCurrency.requestFocus();
        if (requestedFocus) {
            primaryCurrency.setSelection(primaryCurrency.getText().length());
        }
        return requestedFocus;
    }

    private void onPaymentHolderChanged() {
        if (paymentHolder == null) return;
        watcher.setCurrency(paymentHolder.getPrimaryCurrency());
        updatePrimaryCurrencyWith(paymentHolder.getPrimaryCurrency());

        if (hasEvaluationCurrency()) {
            updateSecondaryCurrencyWith(paymentHolder.getSecondaryCurrency());
        }

        invalidateSymbol();
    }

    private void updatePrimaryCurrencyWith(Currency value) {
        if (value.isCrypto()) {
            value.setCurrencyFormat(CryptoCurrency.NO_SYMBOL_FORMAT);
        }

        if (value.isZero()) {
            primaryCurrency.setText("");
        } else {
            primaryCurrency.setText(value.toFormattedCurrency());
        }
    }

    private void updateSecondaryCurrencyWith(Currency value) {
        if (value.isCrypto()) {
            value.setCurrencyFormat(CryptoCurrency.NO_SYMBOL_FORMAT);
        }

        secondaryCurrency.setVisibility(VISIBLE);
        secondaryCurrency.setText(value.toFormattedCurrency());
    }

    private void invalidateSymbol() {
        if (paymentHolder.getPrimaryCurrency().isCrypto()) {
            Drawable drawable = getDrawableFor((CryptoCurrency) paymentHolder.getPrimaryCurrency());
            drawable.setBounds(0, 0,
                    (int) scaleValue(getContext(), TypedValue.COMPLEX_UNIT_DIP, 20F),
                    (int) scaleValue(getContext(), TypedValue.COMPLEX_UNIT_DIP, 21F));
            primaryCurrency.setCompoundDrawables(drawable, null, null, null);
            secondaryCurrency.setCompoundDrawables(null, null, null, null);
        } else if (hasEvaluationCurrency()) {
            Drawable drawable = getDrawableFor((CryptoCurrency) paymentHolder.getSecondaryCurrency());
            drawable.setBounds(0, 0,
                    (int) (scaleValue(getContext(), TypedValue.COMPLEX_UNIT_DIP, 20F) * .8),
                    (int) (scaleValue(getContext(), TypedValue.COMPLEX_UNIT_DIP, 21F) * .8));
            secondaryCurrency.setCompoundDrawables(drawable, null, null, null);
            primaryCurrency.setCompoundDrawables(null, null, null, null);
        } else {
            primaryCurrency.setCompoundDrawables(null, null, null, null);
        }
    }

    private boolean hasEvaluationCurrency() {
        return paymentHolder.getEvaluationCurrency() != null && paymentHolder.getEvaluationCurrency().toLong() > 0L;
    }

    private Drawable getDrawableFor(CryptoCurrency currency) {
        return currency.getSymbolDrawable(getContext());
    }
}
