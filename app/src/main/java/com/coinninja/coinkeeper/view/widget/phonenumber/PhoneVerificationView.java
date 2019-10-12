package com.coinninja.coinkeeper.view.widget.phonenumber;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.ui.phone.verification.ManualPhoneVerification;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.List;

import static com.coinninja.android.helpers.ViewsKt.shakeInError;

public class PhoneVerificationView extends ConstraintLayout {

    PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
    private TextView errorMessage;
    private TextView exampleNumber;
    private PhoneNumberInputView phoneNumberInput;
    private Button verifyPhoneButton;
    private PhoneNumberInputView.OnValidPhoneNumberObserver onPhoneNumberValidObserver;
    private boolean shouldManuallyVerifyNumber = false;
    private CountryCodeLocale selectedCountryCodeLocale;

    public PhoneVerificationView(Context context) {
        this(context, null);
    }

    public PhoneVerificationView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PhoneVerificationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void resetView() {
        hideError();
        clearPhoneNumber();
    }

    public void setCountryCodeLocals(List<CountryCodeLocale> countryCodeLocales) {
        phoneNumberInput.setCountryCodeLocals(countryCodeLocales);
    }

    public List<CountryCodeLocale> getCountryCodeLocales() {
        return phoneNumberInput.getCountryCodeLocales();
    }

    public void setOnValidPhoneNumberObserver(PhoneNumberInputView.OnValidPhoneNumberObserver onPhoneNumberValidObserver) {
        this.onPhoneNumberValidObserver = onPhoneNumberValidObserver;
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.merge_phone_verification_view, this, true);
        errorMessage = findViewById(R.id.error_message);
        exampleNumber = findViewById(R.id.example_number);
        phoneNumberInput = findViewById(R.id.phone_number_input);
        verifyPhoneButton = findViewById(R.id.verify_phone_number);
        errorMessage.setVisibility(GONE);
        verifyPhoneButton.setVisibility(GONE);
        phoneNumberInput.setOnExampleNumberChangeObserver(this::onExampleNumberChanged);
        phoneNumberInput.setOnInvalidPhoneNumberObserver(this::onPhoneNumberInValid);
        phoneNumberInput.setOnValidPhoneNumberObserver(this::onPhoneNumberValid);
        phoneNumberInput.setOnCountryCodeChangeObserver(this::onCountryCodeLocaleSelected);
    }

    private void clearPhoneNumber() {
        phoneNumberInput.setText("");
    }

    private void hideError() {
        errorMessage.setVisibility(GONE);
    }

    private void invalidateVerifyPhoneNumberButton() {
        if (shouldManuallyVerifyNumber) {
            verifyPhoneButton.setVisibility(VISIBLE);
            verifyPhoneButton.setOnClickListener(v -> onManuallyValidatePhoneNumber());
        } else {
            verifyPhoneButton.setVisibility(GONE);
            verifyPhoneButton.setOnClickListener(null);
        }
    }

    private void notifyObserverOfValidInput(Phonenumber.PhoneNumber phoneNumber) {
        if (onPhoneNumberValidObserver != null) {
            onPhoneNumberValidObserver.onValidPhoneNumber(phoneNumber);
        }
    }

    private void onPhoneNumberInValid(String text) {
        clearPhoneNumber();
        showError();
    }

    private void onExampleNumberChanged(String exampleNumber) {
        TextView exampleView = findViewById(R.id.example_number);
        exampleView.setText(getContext().getString(R.string.verification_example, exampleNumber));
    }

    private void onCountryCodeLocaleSelected(CountryCodeLocale countryCodeLocale) {
        selectedCountryCodeLocale = countryCodeLocale;
        shouldManuallyVerifyNumber = ManualPhoneVerification.INSTANCE.shouldManuallyVerify(countryCodeLocale.getLocale());
        invalidateVerifyPhoneNumberButton();
        resetView();
    }

    private void onPhoneNumberValid(Phonenumber.PhoneNumber phoneNumber) {
        if (shouldManuallyVerifyNumber) return;
        notifyObserverOfValidInput(phoneNumber);
    }

    private void onManuallyValidatePhoneNumber() {
        String userInput = phoneNumberInput.getText();
        try {
            Phonenumber.PhoneNumber phoneNumber = phoneNumberUtil.parse(userInput,
                    selectedCountryCodeLocale.getLocale().getCountry());
            if (phoneNumberUtil.isValidNumber(phoneNumber)) {
                notifyObserverOfValidInput(phoneNumber);
            } else {
                showError();
            }
        } catch (NumberParseException e) {
            e.printStackTrace();
            showError();
        }
    }

    private void showError() {
        errorMessage.setVisibility(VISIBLE);
        shakeInError(phoneNumberInput);
    }

}
