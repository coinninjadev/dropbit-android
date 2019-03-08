package com.coinninja.coinkeeper.util.android;

import android.content.Intent;

import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.cn.wallet.service.CNWalletAddressRequestService;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.matchers.IntentMatcher;

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

    TestCoinKeeperApplication application;
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

    }


}