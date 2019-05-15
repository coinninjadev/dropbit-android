package com.coinninja.coinkeeper.util.android;

import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.cn.dropbit.DropBitService;
import com.coinninja.coinkeeper.cn.wallet.service.CNWalletAddressRequestService;
import com.coinninja.coinkeeper.model.PhoneNumber;
import com.coinninja.coinkeeper.service.RegisterUsersPhoneService;
import com.coinninja.coinkeeper.util.DropbitIntents;
import com.google.i18n.phonenumbers.Phonenumber;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.coinninja.matchers.IntentMatcher.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.robolectric.Shadows.shadowOf;

@RunWith(AndroidJUnit4.class)
public class ServiceWorkUtilTest {

    private ServiceWorkUtil serviceWorkUtil;
    private TestCoinKeeperApplication application;

    @Before
    public void setUp() {
        application = ApplicationProvider.getApplicationContext();
        serviceWorkUtil = new ServiceWorkUtil(application);
    }

    @After
    public void tearDown() {
        serviceWorkUtil = null;
        application = null;
    }

    @Test
    public void starts_cn_wallet_address_lookup_service() {
        String phoneNumberHash = "--hash--";

        serviceWorkUtil.lookupAddressForPhoneNumberHash(phoneNumberHash);

        Intent startedService = shadowOf(application).peekNextStartedService();
        Intent intent = new Intent(application, CNWalletAddressRequestService.class);
        intent.putExtra(DropbitIntents.EXTRA_PHONE_NUMBER_HASH, phoneNumberHash);
        assertThat(startedService, equalTo(intent));
    }


    @Test
    public void starts_service_to_register_users_phone() {
        Phonenumber.PhoneNumber phoneNumber = new Phonenumber.PhoneNumber();
        phoneNumber.setNationalNumber(3305555555L);
        phoneNumber.setCountryCode(1);
        PhoneNumber number = new PhoneNumber(phoneNumber);

        serviceWorkUtil.registerUsersPhone(number);

        Intent startedService = shadowOf(application).peekNextStartedService();
        Intent intent = new Intent(application, RegisterUsersPhoneService.class);
        intent.putExtra(DropbitIntents.EXTRA_PHONE_NUMBER, number);
        assertThat(startedService, equalTo(intent));
    }

    @Test
    public void disables_dropbit_me() {
        serviceWorkUtil.disableDropBitMe();

        Intent startedService = shadowOf(application).peekNextStartedService();
        Intent intent = new Intent(application, DropBitService.class);
        intent.setAction(DropbitIntents.ACTION_DROPBIT_ME_DISABLE_ACCOUNT);
        assertThat(startedService, equalTo(intent));
    }

    @Test
    public void enables_dropbit_me() {
        serviceWorkUtil.enableDropBitMe();

        Intent startedService = shadowOf(application).peekNextStartedService();
        Intent intent = new Intent(application, DropBitService.class);
        intent.setAction(DropbitIntents.ACTION_DROPBIT_ME_ENABLE_ACCOUNT);
        assertThat(startedService, equalTo(intent));
    }
}