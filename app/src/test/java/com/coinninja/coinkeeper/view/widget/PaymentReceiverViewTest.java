package com.coinninja.coinkeeper.view.widget;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.ui.base.TestableActivity;
import com.coinninja.coinkeeper.view.widget.phonenumber.CountryCodeLocale;
import com.coinninja.coinkeeper.view.widget.phonenumber.PhoneNumberInputView;
import com.coinninja.coinkeeper.view.widget.phonenumber.PhoneNumberInputView.OnInvalidPhoneNumberObserver;
import com.coinninja.coinkeeper.view.widget.phonenumber.PhoneNumberInputView.OnValidPhoneNumberObserver;
import com.google.i18n.phonenumbers.Phonenumber;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.coinninja.android.helpers.Views.withId;
import static com.coinninja.matchers.TextViewMatcher.hasText;
import static com.coinninja.matchers.ViewMatcher.isGone;
import static com.coinninja.matchers.ViewMatcher.isVisible;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class PaymentReceiverViewTest {
    private TestableActivity activity;
    private PaymentReceiverView paymentReceiverView;
    private List<CountryCodeLocale> countryCodeLocales;
    private PhoneNumberInputView phoneNumberInputView;
    private Button showPhoneInput;
    private ImageView scanButton;

    @Before
    public void setUp() {
        activity = Robolectric.setupActivity(TestableActivity.class);
        activity.appendLayout(R.layout.fragment_pay_dialog);
        phoneNumberInputView = withId(activity, R.id.phone_number_input);
        showPhoneInput = withId(activity, R.id.show_phone_input);
        scanButton = withId(activity, R.id.scan_button);
        paymentReceiverView = (PaymentReceiverView) showPhoneInput.getParent();
        countryCodeLocales = new ArrayList<>();
        countryCodeLocales.add(new CountryCodeLocale(new Locale("en", "GB"), 44));
        countryCodeLocales.add(new CountryCodeLocale(new Locale("en", "US"), 1));
    }

    @After
    public void tearDown() {
        activity = null;
        paymentReceiverView = null;
        countryCodeLocales = null;
        phoneNumberInputView = null;
        showPhoneInput = null;
        scanButton = null;
    }

    @Test
    public void inflates_view_with_phone_input() {
        assertNotNull(paymentReceiverView);
        assertNotNull(showPhoneInput);
    }

    @Test
    public void initial_configuration() {
        assertTrue(paymentReceiverView.isFocusable());
        assertTrue(paymentReceiverView.isClickable());
        assertFalse(paymentReceiverView.isFocusableInTouchMode());
        assertFalse(showPhoneInput.isFocusableInTouchMode());
    }

    @Test
    public void phone_input_view_hidden_by_default() {
        assertThat(phoneNumberInputView, isGone());
    }


    @Test
    public void sets_country_code_locales_on_phone_input() {
        paymentReceiverView.setCountryCodeLocales(countryCodeLocales);

        assertThat(phoneNumberInputView.getCountryCodeLocales(), equalTo(countryCodeLocales));
    }

    @Test
    public void setting_payment_address_assigns_button_label() {
        String paymentAddress = "--payment-address--";

        paymentReceiverView.setPaymentAddress(paymentAddress);

        assertThat(showPhoneInput, hasText(paymentAddress));
    }

    @Test
    public void setting_payment_address_hides_phone_input_shows_button() {
        String paymentAddress = "--payment-address--";
        showPhoneInput.performClick();

        paymentReceiverView.setPaymentAddress(paymentAddress);

        assertThat(phoneNumberInputView, isGone());
        assertThat(showPhoneInput, isVisible());
        assertThat(scanButton, isVisible());
    }

    @Test
    public void setting_payment_address_clears_phone_entry() {
        String paymentAddress = "--payment-address--";
        showPhoneInput.performClick();
        phoneNumberInputView.setText("+1 330-555-11");

        paymentReceiverView.setPaymentAddress(paymentAddress);

        assertThat(phoneNumberInputView.getText(), equalTo("+1"));
    }

    @Test
    public void showing_phone_number_clears_payment_address() {
        String paymentAddress = "--payment-address--";
        paymentReceiverView.setPaymentAddress(paymentAddress);

        paymentReceiverView.performClick();

        assertThat(showPhoneInput, hasText(""));
    }

    @Test
    public void allows_observation_of_valid_phone_number() {
        Phonenumber.PhoneNumber phoneNumber = new Phonenumber.PhoneNumber();
        phoneNumber.setCountryCode(1);
        phoneNumber.setNationalNumber(3305551111L);
        OnValidPhoneNumberObserver observer = mock(OnValidPhoneNumberObserver.class);
        showPhoneInput.performClick();
        paymentReceiverView.setOnValidPhoneNumberObserver(observer);

        phoneNumberInputView.setText("+1 330-555-1111");

        verify(observer).onValidPhoneNumber(phoneNumber);
    }

    @Test
    public void allows_observation_of_invalid_phone_number() {
        Phonenumber.PhoneNumber phoneNumber = new Phonenumber.PhoneNumber();
        phoneNumber.setCountryCode(1);
        phoneNumber.setNationalNumber(3305551111L);
        OnInvalidPhoneNumberObserver observer = mock(OnInvalidPhoneNumberObserver.class);
        showPhoneInput.performClick();
        paymentReceiverView.setOnInvalidPhoneNumberObserver(observer);

        phoneNumberInputView.setText("+1 000-000-0000");

        verify(observer).onInvalidPhoneNumber("+1 000-000-0000");
    }

    @Test
    public void provides_access_to_country_code_locales() {
        paymentReceiverView.setCountryCodeLocales(countryCodeLocales);

        assertThat(paymentReceiverView.getCountryCodeLocales(), equalTo(countryCodeLocales));
    }

    @Test
    public void provides_access_to_payment_address() {
        String phoneNumber = "+1 330-555-1111";

        phoneNumberInputView.setText(phoneNumber);

        assertThat(paymentReceiverView.getPhoneNumber(), equalTo(phoneNumber));
    }

    @Test
    public void provides_access_to_phone_number() {
        String paymentAddress = "--payment-address--";

        paymentReceiverView.setPaymentAddress(paymentAddress);

        assertThat(paymentReceiverView.getPaymentAddress(), equalTo(paymentAddress));
    }

    @Test
    public void can_clears_phone_number() {
        phoneNumberInputView.setText("+1 000-000-0000");
        showPhoneInput.setText("--text--");

        paymentReceiverView.clear();

        assertThat(phoneNumberInputView.getText(), equalTo("+1"));
        assertThat(showPhoneInput, hasText(""));
    }

    @Test
    public void forwards_click_to_correct_child() {
        View.OnClickListener phoneInputClick = mock(View.OnClickListener.class);
        phoneNumberInputView.setOnClickListener(phoneInputClick);
        assertThat(showPhoneInput, isVisible());

        paymentReceiverView.performClick();

        assertThat(phoneNumberInputView, isVisible());
        assertThat(showPhoneInput, hasText(""));
        assertThat(showPhoneInput, isGone());
        assertThat(scanButton, isGone());
        verify(phoneInputClick).onClick(any());
    }

    @Test
    public void clicking_scan_bubbles_event() {
        View.OnClickListener clickListener = mock(View.OnClickListener.class);
        paymentReceiverView.setOnScanObserver(clickListener);

        scanButton.performClick();

        verify(clickListener).onClick(scanButton);
    }
}