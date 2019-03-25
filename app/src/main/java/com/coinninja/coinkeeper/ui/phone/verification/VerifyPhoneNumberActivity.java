package com.coinninja.coinkeeper.ui.phone.verification;

import android.os.Bundle;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.di.interfaces.CountryCodeLocales;
import com.coinninja.coinkeeper.model.PhoneNumber;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.android.ServiceWorkUtil;
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil;
import com.coinninja.coinkeeper.view.activity.base.SecuredActivity;
import com.coinninja.coinkeeper.view.widget.phonenumber.CountryCodeLocale;
import com.coinninja.coinkeeper.view.widget.phonenumber.PhoneVerificationView;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.List;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import dagger.android.AndroidInjection;

import static com.coinninja.android.helpers.Views.withId;

public class VerifyPhoneNumberActivity extends SecuredActivity {

    @Inject
    @CountryCodeLocales
    List<CountryCodeLocale> countryCodeLocales;
    @Inject
    ActivityNavigationUtil activityNavigationUtil;
    @Inject
    ServiceWorkUtil serviceWorkUtil;

    PhoneVerificationView phoneVerificationView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_phone);
        phoneVerificationView = withId(this, R.id.phone_verification_view);
    }

    @Override
    protected void onStart() {
        super.onStart();
        phoneVerificationView.setOnValidPhoneNumberObserver(this::onPhoneNumberValid);
    }


    @Override
    protected void onResume() {
        super.onResume();
        phoneVerificationView.setCountryCodeLocals(countryCodeLocales);
    }

    @Override
    protected void onPause() {
        super.onPause();
        phoneVerificationView.resetView();
    }


    @Override
    public void onSkipClicked() {
        if (analytics != null) {
            analytics.trackEvent(Analytics.EVENT_PHONE_VERIFICATION_SKIPPED);
        }
        activityNavigationUtil.navigateToHome(this);
    }

    void onPhoneNumberValid(Phonenumber.PhoneNumber phoneNumber) {
        PhoneNumber number = new PhoneNumber(phoneNumber);
        serviceWorkUtil.registerUsersPhone(number);
        activityNavigationUtil.navigateToVerifyPhoneNumberCode(this, number);
    }

}
