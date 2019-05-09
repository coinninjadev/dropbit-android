package com.coinninja.coinkeeper.ui.phone.verification;

import android.view.MenuItem;

import androidx.test.core.app.ApplicationProvider;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.model.PhoneNumber;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.android.ServiceWorkUtil;
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil;
import com.coinninja.coinkeeper.view.widget.phonenumber.CountryCodeLocale;
import com.coinninja.coinkeeper.view.widget.phonenumber.PhoneVerificationView;
import com.google.i18n.phonenumbers.Phonenumber;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.coinninja.android.helpers.Views.withId;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class VerifyPhoneNumberActivityTest {


    private List<CountryCodeLocale> countryCodeLocals = new ArrayList();
    private VerifyPhoneNumberActivity activity;
    private ActivityController<VerifyPhoneNumberActivity> activityController;
    @Mock
    private ActivityNavigationUtil activityNavigationUtil;
    @Mock
    private ServiceWorkUtil serviceWorkUtil;
    private TestCoinKeeperApplication application;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        application = ApplicationProvider.getApplicationContext();
        application.countryCodeLocales = countryCodeLocals;
        application.activityNavigationUtil = activityNavigationUtil;
        application.serviceWorkUtil = serviceWorkUtil;
        countryCodeLocals.add(new CountryCodeLocale(new Locale("en", "US"), 1));

        activityController = Robolectric.buildActivity(VerifyPhoneNumberActivity.class);
        activity = activityController.get();
        activityController.setup();
    }

    @After
    public void tearDown() {
        activity = null;
        activityController = null;
        application = null;
        activityNavigationUtil = null;
    }

    @Test
    public void user_can_skip_verification_of_key_words() {
        MenuItem closeMenuItem = mock(MenuItem.class);
        when(closeMenuItem.getItemId()).thenReturn(R.id.action_skip_btn);

        activity.onSkipClicked();

        verify(activityNavigationUtil).navigateToHome(activity);
        verify(application.analytics).trackEvent(Analytics.EVENT_PHONE_VERIFICATION_SKIPPED);
    }

    @Test
    public void sets_country_code_locales_on_view() {
        PhoneVerificationView phoneVerificationView = withId(activity, R.id.phone_verification_view);
        assertThat(phoneVerificationView.getCountryCodeLocales().size(), equalTo(1));
    }

    @Test
    public void passes_phone_data_to_verification_screen() {
        Phonenumber.PhoneNumber phoneNumber = new Phonenumber.PhoneNumber();
        phoneNumber.setNationalNumber(3305555555L);
        phoneNumber.setCountryCode(1);
        PhoneNumber number = new PhoneNumber(phoneNumber);

        activity.onPhoneNumberValid(phoneNumber);

        verify(activityNavigationUtil).navigateToVerifyPhoneNumberCode(activity, number);
    }

    @Test
    public void starts_verify_phone_number_service() {
        Phonenumber.PhoneNumber phoneNumber = new Phonenumber.PhoneNumber();
        phoneNumber.setNationalNumber(3305555555L);
        phoneNumber.setCountryCode(1);
        PhoneNumber number = new PhoneNumber(phoneNumber);

        activity.onPhoneNumberValid(phoneNumber);

        verify(serviceWorkUtil).registerUsersPhone(number);
    }

    @Test
    public void hides_error_on_pause() {
        activity.phoneVerificationView = mock(PhoneVerificationView.class);

        activityController.pause();

        verify(activity.phoneVerificationView).resetView();
    }
}