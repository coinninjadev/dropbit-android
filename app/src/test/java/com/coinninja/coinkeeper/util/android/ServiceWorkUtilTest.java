package com.coinninja.coinkeeper.util.android;

import android.content.Intent;

import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.cn.wallet.service.CNWalletAddressRequestService;
import com.coinninja.coinkeeper.model.PhoneNumber;
import com.coinninja.coinkeeper.service.RegisterUsersPhoneService;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.matchers.IntentMatcher;
import com.google.i18n.phonenumbers.Phonenumber;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class ServiceWorkUtilTest {

    private TestCoinKeeperApplication application;
    private ServiceWorkUtil serviceWorkUtil;

    @Before
    public void setUp() {
        application = (TestCoinKeeperApplication) RuntimeEnvironment.application;
        serviceWorkUtil = new ServiceWorkUtil(application);
    }

    @After
    public void tearDown() {
        application = null;
        serviceWorkUtil = null;
    }

    @Test
    public void starts_cn_wallet_address_lookup_service() {
        String phoneNumberHash = "--hash--";
        ShadowApplication shadowApplication = shadowOf(application);

        serviceWorkUtil.lookupAddressForPhoneNumberHash(phoneNumberHash);

        Intent nextStartedService = shadowApplication.getNextStartedService();
        Intent intent = new Intent(application, CNWalletAddressRequestService.class);
        intent.putExtra(Intents.EXTRA_PHONE_NUMBER_HASH, phoneNumberHash);
        assertThat(nextStartedService, IntentMatcher.equalTo(intent));

        Phonenumber.PhoneNumber phoneNumber = new Phonenumber.PhoneNumber();
        phoneNumber.setNationalNumber(3305555555L);
        phoneNumber.setCountryCode(1);

        Intent serviceIntent = new Intent(application, RegisterUsersPhoneService.class);
        serviceIntent.putExtra(Intents.EXTRA_PHONE_NUMBER, phoneNumber);
        assertThat(nextStartedService, IntentMatcher.equalTo(intent));
    }

    @Test
    public void starts_service_to_register_users_phone() {
        Phonenumber.PhoneNumber phoneNumber = new Phonenumber.PhoneNumber();
        phoneNumber.setNationalNumber(3305555555L);
        phoneNumber.setCountryCode(1);
        PhoneNumber number = new PhoneNumber(phoneNumber);

        serviceWorkUtil.registerUsersPhone(number);

        ShadowApplication shadowApplication = shadowOf(application);
        Intent nextStartedService = shadowApplication.getNextStartedService();
        Intent intent = new Intent(application, RegisterUsersPhoneService.class);
        intent.putExtra(Intents.EXTRA_PHONE_NUMBER, number);
        assertThat(nextStartedService, IntentMatcher.equalTo(intent));
    }


}