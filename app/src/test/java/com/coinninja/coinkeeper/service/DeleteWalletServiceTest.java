package com.coinninja.coinkeeper.service;

import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.cn.service.PushNotificationDeviceManager;
import com.coinninja.coinkeeper.cn.service.PushNotificationEndpointManager;
import com.coinninja.coinkeeper.cn.wallet.SyncWalletManager;
import com.coinninja.coinkeeper.model.helpers.DaoSessionManager;
import com.coinninja.coinkeeper.model.helpers.UserHelper;
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient;
import com.coinninja.coinkeeper.util.DropbitIntents;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class DeleteWalletServiceTest {

    private DeleteWalletService service;

    @Mock
    private LocalBroadCastUtil localBroadCastUtil;

    @Mock
    private UserHelper userHelper;

    @Mock
    private DaoSessionManager daoSessionManager;

    @Mock
    private PushNotificationEndpointManager pushNotificationEndpointManager;

    @Mock
    private PushNotificationDeviceManager pushNotificationDeviceManager;

    @Mock
    private Analytics analytics;

    @Mock
    private SignedCoinKeeperApiClient apiClient;

    @Mock
    private SyncWalletManager syncWalletManager;

    @After
    public void tearDown() {
        service = null;
        userHelper = null;
        localBroadCastUtil = null;
        daoSessionManager = null;
        pushNotificationEndpointManager = null;
        pushNotificationDeviceManager = null;
        analytics = null;
        apiClient = null;
        syncWalletManager = null;
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        service = Robolectric.setupService(DeleteWalletService.class);
        service.daoSessionManager = daoSessionManager;
        service.userHelper = userHelper;
        service.localBroadCastUtil = localBroadCastUtil;
        service.analytics = analytics;
        service.syncWalletManager = syncWalletManager;
        service.apiClient = apiClient;
        service.pushNotificationEndpointManager = pushNotificationEndpointManager;
        service.pushNotificationDeviceManager = pushNotificationDeviceManager;
    }

    @Test
    public void disables_sync() {
        service.onHandleIntent(null);

        verify(syncWalletManager).cancelAllScheduledSync();
    }

    @Test
    public void flushes_analytics() {
        service.onHandleIntent(null);

        verify(analytics).flush();
    }

    @Test
    public void tracks_disabling_account() {
        service.onHandleIntent(null);

        verify(analytics).setUserProperty(Analytics.PROPERTY_HAS_DROPBIT_ME_ENABLED, false);
    }

    @Test
    public void sets_property_for_user_to_no_longer_has_a_balance() {
        service.onHandleIntent(null);

        verify(analytics).setUserProperty(Analytics.PROPERTY_HAS_BTC_BALANCE, false);
    }

    @Test
    public void sets_property_for_user_to_no_longer_have_backed_up_wallet() {
        service.onHandleIntent(null);

        verify(analytics).setUserProperty(Analytics.PROPERTY_HAS_WALLET_BACKUP, false);
    }

    @Test
    public void sets_property_for_user_to_no_longer_be_verified() {
        service.onHandleIntent(null);

        verify(analytics).setUserProperty(Analytics.PROPERTY_PHONE_VERIFIED, false);
    }

    @Test
    public void identifies_user_property_to_not_have_a_wallet() {
        service.onHandleIntent(null);

        verify(analytics).setUserProperty(Analytics.PROPERTY_HAS_WALLET, false);
    }

    @Test
    public void instructs_CN_to_reset_wallet() {
        service.onHandleIntent(null);

        verify(apiClient).resetWallet();
    }

    @Test
    public void resets_all_data() {
        service.onHandleIntent(null);

        verify(daoSessionManager).resetAll();
    }

    @Test
    public void notifies_delete_completed() {
        service.onHandleIntent(null);

        verify(localBroadCastUtil).sendBroadcast(DropbitIntents.ACTION_ON_WALLET_DELETED);
    }

    @Test
    public void send_analytics() {
        service.onHandleIntent(null);

        verify(analytics).trackEvent(Analytics.EVENT_WALLET_DELETE);
    }

    @Test
    public void removes_endpoint_locally_when_endpoint_is_present() {
        when(pushNotificationEndpointManager.hasEndpoint()).thenReturn(true);

        service.onHandleIntent(null);

        verify(pushNotificationEndpointManager).removeEndpoint();

    }

    @Test
    public void does_not_remove_endpoint_locally_when_endpoint_is_not_present() {
        when(pushNotificationEndpointManager.hasEndpoint()).thenReturn(false);

        service.onHandleIntent(null);

        verify(pushNotificationEndpointManager, times(0)).removeEndpoint();

    }

    @Test
    public void removes_endpoint_remote_when_endpoint_is_present() {
        when(pushNotificationEndpointManager.hasEndpoint()).thenReturn(true);

        service.onHandleIntent(null);

        verify(pushNotificationEndpointManager).unRegister();
    }

    @Test
    public void does_not_removes_endpoint_remote_when_endpoint_is_not_present() {
        when(pushNotificationEndpointManager.hasEndpoint()).thenReturn(false);

        service.onHandleIntent(null);

        verify(pushNotificationEndpointManager, times(0)).unRegister();
    }

    @Test
    public void removes_cn_device_id_locally() {
        service.onHandleIntent(null);

        verify(pushNotificationDeviceManager).removeCNDevice();
    }
}