package com.coinninja.coinkeeper.view.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.di.interfaces.CountryCodeLocales;
import com.coinninja.coinkeeper.model.PhoneNumber;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.android.ServiceWorkUtil;
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil;
import com.coinninja.coinkeeper.view.activity.base.SecuredActivity;
import com.coinninja.coinkeeper.view.widget.phonenumber.CountryCodeLocale;
import com.coinninja.coinkeeper.view.widget.phonenumber.PhoneNumberInputView;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.List;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import dagger.android.AndroidInjection;

import static com.coinninja.android.helpers.Views.shakeInError;
import static com.coinninja.android.helpers.Views.withId;

public class VerifyPhoneNumberActivity extends SecuredActivity {

    @Inject
    @CountryCodeLocales
    List<CountryCodeLocale> countryCodeLocales;
    @Inject
    ActivityNavigationUtil activityNavigationUtil;
    @Inject
    ServiceWorkUtil serviceWorkUtil;

    @Inject
    PhoneNumberUtil phoneNumberUtil;

    private PhoneNumberInputView phoneNumberInputView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_phone);
        phoneNumberInputView = withId(this, R.id.phone_number_input);
    }

    @Override
    protected void onStart() {
        super.onStart();
        phoneNumberInputView.setOnExampleNumberChangeObserver(this::onExampleNumberChanged);
        phoneNumberInputView.setOnInvalidPhoneNumberObserver(this::onPhoneNumberInValid);
        phoneNumberInputView.setOnValidPhoneNumberObserver(this::onPhoneNumberValid);
    }


    @Override
    protected void onResume() {
        super.onResume();
        phoneNumberInputView.setCountryCodeLocals(countryCodeLocales);
    }

    @Override
    protected void onPause() {
        super.onPause();
        hideError();
        clearPhoneNumber();
    }

    private void hideError() {
        findViewById(R.id.error_message).setVisibility(View.INVISIBLE);
    }

    @Override
    public void onSkipClicked() {
        if (analytics != null) {
            analytics.trackEvent(Analytics.EVENT_PHONE_VERIFICATION_SKIPPED);
        }
        activityNavigationUtil.navigateToHome(this);
    }

    private void onPhoneNumberValid(Phonenumber.PhoneNumber phoneNumber) {
        PhoneNumber number = new PhoneNumber(phoneNumber);
        serviceWorkUtil.registerUsersPhone(number);
        activityNavigationUtil.navigateToVerifyPhoneNumber(this, number);
    }

    private void onPhoneNumberInValid(String text) {
        showError();
    }

    private void onExampleNumberChanged(String exampleNumber) {
        TextView exampleView = withId(this, R.id.example_number);
        exampleView.setText(getString(R.string.verify_phone_number_example, exampleNumber));
    }

    private void showError() {
        findViewById(R.id.error_message).setVisibility(View.VISIBLE);
        shakeInError(phoneNumberInputView);
    }

    private void clearPhoneNumber() {
        phoneNumberInputView.setText("");
    }
}
