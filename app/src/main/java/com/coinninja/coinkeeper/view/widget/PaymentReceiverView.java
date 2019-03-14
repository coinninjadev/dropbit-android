package com.coinninja.coinkeeper.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.view.widget.phonenumber.CountryCodeLocale;
import com.coinninja.coinkeeper.view.widget.phonenumber.PhoneNumberInputView;

import java.util.List;

import androidx.annotation.Nullable;


public class PaymentReceiverView extends RelativeLayout {

    private PhoneNumberInputView phoneNumberInputView;
    private Button showPhoneButton;

    public PaymentReceiverView(Context context) {
        this(context, null);
    }

    public PaymentReceiverView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PaymentReceiverView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public PaymentReceiverView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.merge_component_payment_receiver_view, this, true);
        phoneNumberInputView = findViewById(R.id.phone_number_input);
        showPhoneButton = findViewById(R.id.show_phone_input);
        phoneNumberInputView.setVisibility(View.GONE);
        showPhoneButton.setOnClickListener(v -> showPhoneInput());
        setFocusable(true);
        setFocusableInTouchMode(false);
        setClickable(true);
        setOnClickListener(v -> showPhoneInput());
    }

    public String getPaymentAddress() {
        return showPhoneButton.getText().toString();
    }

    public void setPaymentAddress(String paymentAddress) {
        showPhoneButton.setText(paymentAddress);
        showPaymentAddress();
    }

    private void showPhoneInput() {
        showPhoneButton.setVisibility(GONE);
        phoneNumberInputView.setVisibility(VISIBLE);
        showPhoneButton.setText("");
        phoneNumberInputView.performClick();
    }

    private void showPaymentAddress() {
        showPhoneButton.setVisibility(VISIBLE);
        phoneNumberInputView.setVisibility(GONE);
        phoneNumberInputView.setText("");
    }

    public void setOnValidPhoneNumberObserver(PhoneNumberInputView.OnValidPhoneNumberObserver observer) {
        phoneNumberInputView.setOnValidPhoneNumberObserver(observer);
    }

    public void setOnInvalidPhoneNumberObserver(PhoneNumberInputView.OnInvalidPhoneNumberObserver observer) {
        phoneNumberInputView.setOnInvalidPhoneNumberObserver(observer);
    }

    public List<CountryCodeLocale> getCountryCodeLocales() {
        return phoneNumberInputView.getCountryCodeLocales();
    }

    public void setCountryCodeLocales(List<CountryCodeLocale> countryCodeLocales) {
        phoneNumberInputView.setCountryCodeLocals(countryCodeLocales);
    }

    public String getPhoneNumber() {
        return phoneNumberInputView.getText();
    }

    public void clear() {
        phoneNumberInputView.setText("");
        showPhoneButton.setText("");
    }
}
