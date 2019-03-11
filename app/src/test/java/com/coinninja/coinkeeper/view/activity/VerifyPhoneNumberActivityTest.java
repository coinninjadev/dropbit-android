package com.coinninja.coinkeeper.view.activity;

import android.app.AlertDialog;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.model.PhoneNumber;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.android.ServiceWorkUtil;
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil;
import com.coinninja.coinkeeper.view.widget.phonenumber.CountryCodeLocale;
import com.coinninja.coinkeeper.view.widget.phonenumber.PhoneNumberInputView;
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
import org.robolectric.shadows.ShadowAlertDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.coinninja.android.helpers.Views.withId;
import static com.coinninja.matchers.TextViewMatcher.hasText;
import static com.coinninja.matchers.ViewMatcher.isInvisible;
import static com.coinninja.matchers.ViewMatcher.isVisible;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class VerifyPhoneNumberActivityTest {


    List<CountryCodeLocale> countryCodeLocals = new ArrayList();
    private VerifyPhoneNumberActivity activity;
    private ActivityController<VerifyPhoneNumberActivity> activityController;
    @Mock
    private Analytics analytics;
    @Mock
    private ActivityNavigationUtil activityNavigationUtil;
    @Mock
    private ServiceWorkUtil serviceWorkUtil;
    private PhoneNumberInputView phoneNumberInputView;

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
        activityController.start().resume().visible();
        phoneNumberInputView = withId(activity, R.id.phone_number_input);
    }

    @After
    public void tearDown() {
        activity = null;
        activityController = null;
        analytics = null;
        activityNavigationUtil = null;
        phoneNumberInputView = null;
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
    public void passes_phone_data_to_verification_service() {
        Phonenumber.PhoneNumber phoneNumber = new Phonenumber.PhoneNumber();
        phoneNumber.setNationalNumber(3305555555L);
        phoneNumber.setCountryCode(1);
        PhoneNumber number = new PhoneNumber(phoneNumber);

        phoneNumberInputView.setText("3305555555");

        verify(activityNavigationUtil).navigateToVerifyPhoneNumber(activity, number);
    }

    @Test
    public void starts_verify_phone_number_service() {
        Phonenumber.PhoneNumber phoneNumber = new Phonenumber.PhoneNumber();
        phoneNumber.setNationalNumber(3305555555L);
        phoneNumber.setCountryCode(1);
        PhoneNumber number = new PhoneNumber(phoneNumber);

        phoneNumberInputView.setText("3305555555");

        verify(serviceWorkUtil).registerUsersPhone(number);;
    }

    @Test
    public void hides_error_on_pause() {
        View view = withId(activity, R.id.error_message);
        view.setVisibility(View.VISIBLE);

        activityController.pause();

        assertThat(view, isInvisible());
    }

    @Test
    public void shows_error_message_on_error() {
        View view = withId(activity, R.id.error_message);
        view.setVisibility(View.INVISIBLE);

        phoneNumberInputView.setText("0005555555");

        assertThat(view, isVisible());
    }

    @Test
    public void clears_text_on_invalid_input_except_cc() {
        phoneNumberInputView.setText("0005555555");

        assertThat(phoneNumberInputView.getText(), equalTo("+1"));
    }

    @Test
    public void clears_input_when_resumed_except_cc() {
        phoneNumberInputView.setText("33055555");

        activityController.pause();
        activityController.resume();

        assertThat(phoneNumberInputView.getText(), equalTo("+1"));
    }

    @Test
    public void updates_example_phone_number_when_changing() {
        countryCodeLocals.add(new CountryCodeLocale(new Locale("en", "GB"), 44));
        phoneNumberInputView.setCountryCodeLocals(countryCodeLocals);
        TextView example = withId(activity, R.id.example_number);
        assertThat(example, hasText("Example: +1 201-555-0123"));

        withId(activity, R.id.phone_number_view_country_codes).performClick();
        AlertDialog latestDialog = (AlertDialog) ShadowAlertDialog.getLatestDialog();
        shadowOf(latestDialog.getListView()).performItemClick(1);

        assertThat(example, hasText("Example: +44 7400 123456"));
    }
}