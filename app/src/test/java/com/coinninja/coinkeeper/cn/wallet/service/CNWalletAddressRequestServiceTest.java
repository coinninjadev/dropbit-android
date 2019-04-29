package com.coinninja.coinkeeper.cn.wallet.service;

import android.content.Intent;

import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient;
import com.coinninja.coinkeeper.service.client.model.AddressLookupResult;
import com.coinninja.coinkeeper.util.CNLogger;
import com.coinninja.coinkeeper.util.DropbitIntents;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;
import com.coinninja.matchers.IntentMatcher;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Response;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class CNWalletAddressRequestServiceTest {

    ArrayList<AddressLookupResult> results = new ArrayList<>();
    AddressLookupResult result = new AddressLookupResult("-phone-hash-", "-address-", "-pub-key-");

    @Mock
    private SignedCoinKeeperApiClient apiClient;
    @Mock
    private LocalBroadCastUtil localBroadCastUtil;
    @Mock
    private CNLogger logger;

    private CNWalletAddressRequestService walletAddressRequestService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        walletAddressRequestService = Robolectric.setupService(CNWalletAddressRequestService.class);
        walletAddressRequestService.apiClient = apiClient;
        walletAddressRequestService.localBroadCastUtil = localBroadCastUtil;
        walletAddressRequestService.logger = logger;
        results.add(result);
        when(apiClient.queryWalletAddress("-phone-hash-")).thenReturn(Response.success(results));
    }

    @After
    public void tearDown() {
        apiClient = null;
        localBroadCastUtil = null;
        walletAddressRequestService = null;
    }

    @Test
    public void looks_up_address_for_phone_hash() {
        ArgumentCaptor<Intent> argumentCaptor = ArgumentCaptor.forClass(Intent.class);
        Intent intent = new Intent();
        intent.putExtra(DropbitIntents.EXTRA_PHONE_NUMBER_HASH, "-phone-hash-");

        walletAddressRequestService.onHandleIntent(intent);

        Intent successMessage = new Intent(DropbitIntents.ACTION_WALLET_ADDRESS_RETRIEVED);
        successMessage.putExtra(DropbitIntents.EXTRA_ADDRESS_LOOKUP_RESULT, result);

        verify(localBroadCastUtil).sendBroadcast(argumentCaptor.capture());
        Intent actualIntent = argumentCaptor.getValue();

        assertThat(actualIntent, IntentMatcher.equalTo(successMessage));
    }

    @Test
    public void sends_empty_result_back_when_failed() {
        ArgumentCaptor<Intent> argumentCaptor = ArgumentCaptor.forClass(Intent.class);
        Intent intent = new Intent();
        intent.putExtra(DropbitIntents.EXTRA_PHONE_NUMBER_HASH, "-phone-hash-");
        Response error = Response.error(400, ResponseBody.create(MediaType.get("plain/text"), ""));
        when(apiClient.queryWalletAddress("-phone-hash-")).thenReturn(
                error);

        walletAddressRequestService.onHandleIntent(intent);

        Intent successMessage = new Intent(DropbitIntents.ACTION_WALLET_ADDRESS_RETRIEVED);
        successMessage.putExtra(DropbitIntents.EXTRA_ADDRESS_LOOKUP_RESULT, new AddressLookupResult());

        verify(localBroadCastUtil).sendBroadcast(argumentCaptor.capture());
        Intent actualIntent = argumentCaptor.getValue();

        assertThat(actualIntent, IntentMatcher.equalTo(successMessage));
        verify(logger).logError(anyString(), anyString(), eq(error));
    }

    @Test
    public void does_not_run_when_nothing_to_do() {
        walletAddressRequestService.onHandleIntent(new Intent());

        verify(localBroadCastUtil, times(0)).sendBroadcast(any(Intent.class));
    }
}