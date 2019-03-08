package com.coinninja.coinkeeper.view.activity.base;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.widget.TextView;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.interactor.InternalNotificationsInteractor;
import com.coinninja.coinkeeper.model.db.Wallet;
import com.coinninja.coinkeeper.model.helpers.UserHelper;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.service.blockchain.BlockChainService;
import com.coinninja.coinkeeper.service.client.model.TransactionFee;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;
import com.coinninja.coinkeeper.util.currency.USDCurrency;
import com.coinninja.coinkeeper.view.activity.CalculatorActivity;
import com.coinninja.coinkeeper.view.activity.TransactionHistoryActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowApplication;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class BalanceBarActivityTest {

    private BalanceBarActivity activity;
    private Wallet wallet;
    private InternalNotificationsInteractor interactor;
    private ShadowActivity shadowActivity;
    private TransactionFee cachedLatestFee;
    private ActivityController<CalculatorActivity> activityController;
    private BlockChainService.BlockChainBinder binder;
    private ShadowApplication shadowApplication;

    @Mock
    private LocalBroadCastUtil localBroadCastUtil;

    @After
    public void tearDown() throws Exception {
        activity = null;
        wallet = null;
        interactor = null;
        shadowActivity = null;
        cachedLatestFee = null;
        activityController = null;
        binder = null;
        shadowApplication = null;
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        TestCoinKeeperApplication application = (TestCoinKeeperApplication) RuntimeEnvironment.application;
        binder = mock(BlockChainService.BlockChainBinder.class);
        UserHelper mockUser = application.getUser();
        WalletHelper mockWalletHelper = mockUser.getWalletHelper();
        shadowApplication = shadowOf(application);
        wallet = mock(Wallet.class);
        interactor = mock(InternalNotificationsInteractor.class);
        when(wallet.getBalance()).thenReturn(78237L);
        when(mockUser.getPrimaryWallet()).thenReturn(wallet);

        cachedLatestFee = new TransactionFee(1.1, 2.2, 3.3);
        when(mockWalletHelper.getLatestFee()).thenReturn(cachedLatestFee);
        USDCurrency cachedPrice = new USDCurrency(650000L);
        when(mockWalletHelper.getLatestPrice()).thenReturn(cachedPrice);

        activityController = Robolectric.buildActivity(CalculatorActivity.class).create();
        activity = activityController.get();
        activity.serviceBinder = binder;
        activity.localBroadCastUtil = localBroadCastUtil;
        shadowActivity = shadowOf(activity);
        ((CalculatorActivity) activity).setNotifications(interactor);
        activityController.resume().start();
    }

    @Test
    public void unbinds_service_when_stopped() {
        activityController.destroy();

        List<ServiceConnection> unboundServiceConnections = shadowApplication.getUnboundServiceConnections();
        assertThat(unboundServiceConnections.size(), equalTo(1));
        assertThat(unboundServiceConnections.get(0), equalTo(activity));
    }

    @Test
    public void clicking_balance_launches_transaction_history_with_single_top() {

        activity.findViewById(R.id.balance).performClick();

        Intent nextStartedActivity = shadowActivity.getNextStartedActivity();
        assertThat(nextStartedActivity.getComponent().getClassName(), equalTo(TransactionHistoryActivity.class.getName()));
        assertThat(nextStartedActivity.getFlags(), equalTo(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
    }

    @Test
    public void observes_price_change_when_resumed() {
        verify(localBroadCastUtil).registerReceiver(activity.receiver, activity.filter);
    }

    @Test
    public void stops_observing_price_change_when_stopped() {
        activityController.pause().stop();

        verify(localBroadCastUtil).unregisterReceiver(activity.receiver);
    }

    @Test
    public void itUpdatesBTCPriceOnChange() {
        Intent intent = new Intent().setAction(Intents.ACTION_BTC_PRICE_UPDATE).
                putExtra(Intents.EXTRA_BITCOIN_PRICE, 1200L);

        activity.receiver.onReceive(activity, intent);

        assertThat(((TextView) activity.findViewById(R.id.drawer_action_price_text)).
                getText().toString(), equalTo("$12.00"));
    }

    @Test
    public void ShowsCachedPriceAndFeesOnCreate() {
        assertThat(activity.getTransactionFee(), equalTo(cachedLatestFee));
        assertThat(((TextView) activity.findViewById(R.id.drawer_action_price_text)).
                getText().toString(), equalTo("$6,500.00"));
    }

    @Test
    public void binds_to_blockchain_service_on_create() {
        Intent nextStartedService = shadowActivity.getNextStartedService();

        assertThat(nextStartedService.getComponent().getClassName(),
                equalTo(BlockChainService.class.getName()));
    }

    @Test
    public void fetchesNewBTCPriceIntelOnBind() {
        when(binder.isBinderAlive()).thenReturn(true);
        BlockChainService service = mock(BlockChainService.class);
        when(binder.getService()).thenReturn(service);

        ActivityController<CalculatorActivity> activityCreate = Robolectric.buildActivity(CalculatorActivity.class).create();
        BalanceBarActivity activity = activityCreate.get();
        activity.serviceBinder = binder;

        activity.onServiceConnected(mock(ComponentName.class), binder);

        verify(service).fetchCurrentState();
    }

    @Test
    public void sets_balance_of_BTC_from_DB() {
        ActivityController<CalculatorActivity> activityCreate = Robolectric.buildActivity(CalculatorActivity.class).create();
        CalculatorActivity activity = activityCreate.get();

        activity.setNotifications(interactor);

        activityCreate.resume().start();

        assertThat(((TextView) activity.findViewById(R.id.primary_balance)).getText().toString(),
                equalTo("0.00078237 BTC"));
    }

    @Test
    public void sets_usd_balance_when_price_recieved() {
        ActivityController<CalculatorActivity> activityCreate = Robolectric.buildActivity(CalculatorActivity.class).create();
        CalculatorActivity activity = activityCreate.get();
        activity.setNotifications(interactor);

        activityCreate.resume().start();
        activity.onPriceReceived(new USDCurrency(8910.2d));

        assertThat(((TextView) activity.findViewById(R.id.alt_balance)).getText().toString(),
                equalTo("$6.97"));
    }


    @Test
    public void updates_btc_balance_when_notified() {
        assertThat(((TextView) activity.findViewById(R.id.primary_balance)).getText().toString(),
                equalTo("0.00078237 BTC"));
        when(wallet.getBalance()).thenReturn(178237L);

        activity.sendBroadcast(new Intent(Intents.ACTION_WALLET_SYNC_COMPLETE));

        assertThat(((TextView) activity.findViewById(R.id.primary_balance)).getText().toString(),
                equalTo("0.00178237 BTC"));

    }

    @Test
    public void converts_btc_to_usd_on_balance_change() {
        assertThat(((TextView) activity.findViewById(R.id.primary_balance)).getText().toString(),
                equalTo("0.00078237 BTC"));
        activity.onPriceReceived(new USDCurrency(8910.2d));
        when(wallet.getBalance()).thenReturn(178237L);

        activity.sendBroadcast(new Intent(Intents.ACTION_WALLET_SYNC_COMPLETE));


        assertThat(((TextView) activity.findViewById(R.id.alt_balance)).getText().toString(),
                equalTo("$15.88"));
    }

    @Test
    public void unsubscribes_from_wallet_sync_bradcast_when_paused() {
        assertThat(((TextView) activity.findViewById(R.id.primary_balance)).getText().toString(),
                equalTo("0.00078237 BTC"));

        activity.onPriceReceived(new USDCurrency(8910.2d));
        when(wallet.getBalance()).thenReturn(178237L);

        activity.onPause();
        activity.sendBroadcast(new Intent(Intents.ACTION_WALLET_SYNC_COMPLETE));


        assertThat(((TextView) activity.findViewById(R.id.alt_balance)).getText().toString(),
                equalTo("$6.97"));
    }
}