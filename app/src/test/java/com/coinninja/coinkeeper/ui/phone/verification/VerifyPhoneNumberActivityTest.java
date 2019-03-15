package com.coinninja.coinkeeper.ui.phone.verification;

import android.view.MenuItem;

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

import static com.coinninja.matchers.ViewMatcher.isInvisible;
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
    private Analytics analytics;
    @Mock
    private ActivityNavigationUtil activityNavigationUtil;
    @Mock
    private ServiceWorkUtil serviceWorkUtil;
    @Mock
    private PhoneVerificationView phoneVerificationView;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        countryCodeLocals.add(new CountryCodeLocale(new Locale("en", "US"), 1));
        activityController = Robolectric.buildActivity(VerifyPhoneNumberActivity.class);
        activity = activityController.get();
        activityController.create();
        activity.analytics = analytics;
        activity.countryCodeLocales = countryCodeLocals;
        activity.activityNavigationUtil = activityNavigationUtil;
        activity.serviceWorkUtil = serviceWorkUtil;
        activity.phoneVerificationView = phoneVerificationView;
        activityController.start().resume().visible();
    }

    @After
    public void tearDown() {
        activity = null;
        activityController = null;
        analytics = null;
        activityNavigationUtil = null;
        phoneVerificationView = null;
    }

    @Test
    public void user_can_skip_verification_of_key_words() {
        MenuItem closeMenuItem = mock(MenuItem.class);
        when(closeMenuItem.getItemId()).thenReturn(R.id.action_skip_btn);

        activity.onOptionsItemSelected(closeMenuItem);

        verify(activityNavigationUtil).navigateToHome(activity);
        verify(analytics).trackEvent(Analytics.EVENT_PHONE_VERIFICATION_SKIPPED);
    }

    @Test
    public void sets_country_code_locales_on_view() {
        verify(phoneVerificationView).setCountryCodeLocals(countryCodeLocals);
    }

    @Test
    public void observes_valid_entry() {
        verify(phoneVerificationView).setCountryCodeLocals(countryCodeLocals);
    }

    @Test
    public void passes_phone_data_to_verification_screen() {
        Phonenumber.PhoneNumber phoneNumber = new Phonenumber.PhoneNumber();
        phoneNumber.setNationalNumber(3305555555L);
        phoneNumber.setCountryCode(1);
        PhoneNumber number = new PhoneNumber(phoneNumber);

        activity.onPhoneNumberValid(phoneNumber);

        verify(activityNavigationUtil).navigateToVerifyPhoneNumber(activity, number);
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
        activityController.pause();

        verify(phoneVerificationView).resetView();
    }
}