package com.coinninja.coinkeeper.cn.dropbit;

import com.coinninja.coinkeeper.model.helpers.DropbitAccountHelper;
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient;
import com.coinninja.coinkeeper.service.client.model.CNUserPatch;
import com.coinninja.coinkeeper.util.CNLogger;
import com.coinninja.coinkeeper.util.DropbitIntents;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Response;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DropBitMeServiceManagerTest {
    @Mock
    SignedCoinKeeperApiClient apiClient;

    @Mock
    LocalBroadCastUtil localBroadCastUtil;

    @Mock
    Analytics analytics;

    @Mock
    DropbitAccountHelper dropbitAccountHelper;

    @Mock
    CNLogger cnLogger;

    @InjectMocks
    DropBitMeServiceManager dropBitMeServiceManager;

    @After
    public void tearDown() {
        cnLogger = null;
        dropBitMeServiceManager = null;
        dropbitAccountHelper = null;
        localBroadCastUtil = null;
        apiClient = null;
        analytics = null;
    }

    @Test
    public void disables_account() {
        CNUserPatch cnUserPatch = new CNUserPatch(false);
        Response response = Response.success(cnUserPatch);
        when(apiClient.disableDropBitMeAccount()).thenReturn(response);

        dropBitMeServiceManager.disableAccount();

        verify(dropbitAccountHelper).updateUserAccount(cnUserPatch);
        verify(localBroadCastUtil).sendBroadcast(DropbitIntents.ACTION_DROPBIT_ME_ACCOUNT_DISABLED);
        verify(analytics).trackEvent(Analytics.EVENT_DROPBIT_ME_DISABLED);
        verify(analytics).setUserProperty(Analytics.PROPERTY_HAS_DROPBIT_ME_ENABLED, false);
    }

    @Test
    public void disables_account__logs_non_success() {
        Response response = Response.error(400, ResponseBody.create(MediaType.parse("plain/text"), ""));
        when(apiClient.disableDropBitMeAccount()).thenReturn(response);

        dropBitMeServiceManager.disableAccount();

        verifyZeroInteractions(localBroadCastUtil);
        verify(cnLogger).logError(DropBitMeServiceManager.class.getName(), "-- Failed to disable account", response);
        verifyZeroInteractions(analytics);
    }

    @Test
    public void enables_account() {
        CNUserPatch cnUserPatch = new CNUserPatch(true);
        Response response = Response.success(cnUserPatch);
        when(apiClient.enableDropBitMeAccount()).thenReturn(response);

        dropBitMeServiceManager.enableAccount();

        verify(dropbitAccountHelper).updateUserAccount(cnUserPatch);
        verify(localBroadCastUtil).sendBroadcast(DropbitIntents.ACTION_DROPBIT_ME_ACCOUNT_ENABLED);
        verify(analytics).trackEvent(Analytics.EVENT_DROPBIT_ME_ENABLED);
        verify(analytics).setUserProperty(Analytics.PROPERTY_HAS_DROPBIT_ME_ENABLED, true);
    }

    @Test
    public void enables_account__logs_non_success() {
        Response response = Response.error(400, ResponseBody.create(MediaType.parse("plain/text"), ""));
        when(apiClient.enableDropBitMeAccount()).thenReturn(response);

        dropBitMeServiceManager.enableAccount();

        verifyZeroInteractions(localBroadCastUtil);
        verify(cnLogger).logError(DropBitMeServiceManager.class.getName(), "-- Failed to enable account", response);
        verifyZeroInteractions(analytics);
    }

}