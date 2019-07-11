package com.coinninja.coinkeeper.view.activity.base;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;

import androidx.test.core.app.ApplicationProvider;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.model.db.TransactionsInvitesSummary;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.service.blockchain.BlockChainService;
import com.coinninja.coinkeeper.ui.actionbar.managers.DrawerController;
import com.coinninja.coinkeeper.ui.home.HomeActivity;
import com.coinninja.coinkeeper.util.CurrencyPreference;
import com.coinninja.coinkeeper.util.DefaultCurrencies;
import com.coinninja.coinkeeper.util.DropbitIntents;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.currency.USDCurrency;
import com.coinninja.coinkeeper.view.widget.DefaultCurrencyDisplayView;
import com.coinninja.matchers.IntentFilterMatchers;

import org.greenrobot.greendao.query.LazyList;
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
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowApplication;

import java.util.List;

import static com.coinninja.android.helpers.Views.clickOn;
import static com.coinninja.android.helpers.Views.withId;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class BalanceBarActivityTest {

    private BalanceBarActivity activity;
    private ShadowActivity shadowActivity;
    private ActivityController<HomeActivity> activityController;
    private BlockChainService.BlockChainBinder binder;
    private ShadowApplication shadowApplication;

    @Mock
    private LocalBroadCastUtil localBroadCastUtil;

    @Mock
    private CurrencyPreference currencyPreference;

    @Mock
    private WalletHelper walletHelper;

    @Mock
    private DrawerController drawerController;

    @Mock
    private LazyList<TransactionsInvitesSummary> transactions;

    @After
    public void tearDown() throws Exception {
        activity = null;
        shadowActivity = null;
        activityController = null;
        binder = null;
        shadowApplication = null;
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        TestCoinKeeperApplication application = ApplicationProvider.getApplicationContext();
        application.walletHelper = walletHelper;
        application.localBroadCastUtil = localBroadCastUtil;
        application.currencyPreference = currencyPreference;
        application.drawerController = drawerController;

        binder = mock(BlockChainService.BlockChainBinder.class);
        shadowApplication = shadowOf(application);

        USDCurrency cachedPrice = new USDCurrency(650000L);
        when(walletHelper.getLatestPrice()).thenReturn(cachedPrice);
        when(walletHelper.getTransactionsLazily()).thenReturn(transactions);
        when(transactions.isEmpty()).thenReturn(true);
        DefaultCurrencies defaultCurrencies = new DefaultCurrencies(new BTCCurrency(), new USDCurrency());
        when(currencyPreference.getCurrenciesPreference()).thenReturn(defaultCurrencies);

        activityController = Robolectric.buildActivity(HomeActivity.class).create();
        activity = activityController.get();
        activity.serviceBinder = binder;
        shadowActivity = shadowOf(activity);
    }

    @Test
    public void unbinds_service_when_stopped() {
        start();
        activityController.destroy();

        List<ServiceConnection> unboundServiceConnections = shadowApplication.getUnboundServiceConnections();
        assertThat(unboundServiceConnections.size(), equalTo(1));
        assertThat(unboundServiceConnections.get(0), equalTo(activity));
    }

    @Test
    public void clicking_balance_bar_toggles_default_currency_preference() {
        DefaultCurrencies defaultCurrencies = new DefaultCurrencies(new USDCurrency(), new BTCCurrency());
        when(currencyPreference.toggleDefault()).thenReturn(
                new DefaultCurrencies(new BTCCurrency(), new USDCurrency()));
        when(currencyPreference.getCurrenciesPreference()).thenReturn(defaultCurrencies);
        start();

        clickOn(withId(activity, R.id.balance));

        verify(currencyPreference).toggleDefault();
    }

    @Test
    public void observes_price_change_when_resumed() {
        start();

        verify(localBroadCastUtil, atLeast(1)).registerReceiver(eq(activity.receiver), any(IntentFilter.class));
        assertThat(activity.filter, IntentFilterMatchers.containsAction(DropbitIntents.ACTION_WALLET_SYNC_COMPLETE));
        assertThat(activity.filter, IntentFilterMatchers.containsAction(DropbitIntents.ACTION_BTC_PRICE_UPDATE));
    }

    @Test
    public void stops_observing_price_change_when_stopped() {
        start();

        activityController.pause().stop();

        verify(localBroadCastUtil).unregisterReceiver(activity.receiver);
    }

    @Test
    public void updates_price_when_changed() {
        start();
        Intent intent = new Intent().setAction(DropbitIntents.ACTION_BTC_PRICE_UPDATE).
                putExtra(DropbitIntents.EXTRA_BITCOIN_PRICE, 1200L);

        activity.receiver.onReceive(activity, intent);

        verify(drawerController).updatePriceOfBtcDisplay(new USDCurrency(1200L));
    }

    @Test
    public void binds_to_block_chain_service_on_create() {
        start();

        Intent nextStartedService = shadowActivity.getNextStartedService();

        assertThat(nextStartedService.getComponent().getClassName(),
                equalTo(BlockChainService.class.getName()));
    }

    @Test
    public void fetchesNewBTCPriceIntelOnBind() {
        when(binder.isBinderAlive()).thenReturn(true);
        BlockChainService service = mock(BlockChainService.class);
        when(binder.getService()).thenReturn(service);

        start();

        ActivityController<HomeActivity> activityCreate = Robolectric.buildActivity(HomeActivity.class).create();
        BalanceBarActivity activity = activityCreate.get();
        activity.serviceBinder = binder;

        activity.onServiceConnected(mock(ComponentName.class), binder);

        verify(service).fetchCurrentState();
    }

    @Test
    public void sets_balances_of_BTC_from_DB__btc_primary() {
        DefaultCurrencies defaultCurrencies = new DefaultCurrencies(new BTCCurrency(), new USDCurrency());
        when(currencyPreference.getCurrenciesPreference()).thenReturn(defaultCurrencies);
        when(walletHelper.getBalance()).thenReturn(78237L);
        when(walletHelper.getLatestPrice()).thenReturn(new USDCurrency(1000.00D));

        start();

        DefaultCurrencyDisplayView view = withId(activity, R.id.balance);

        assertThat(view.getTotalCrypto().toLong(), equalTo(78237L));
        assertThat(view.getFiatValue().toLong(), equalTo(78L));
    }

    @Test
    public void sets_balances_of_BTC_from_DB__usd_primary() {
        DefaultCurrencies defaultCurrencies = new DefaultCurrencies(new USDCurrency(), new BTCCurrency());
        when(currencyPreference.getCurrenciesPreference()).thenReturn(defaultCurrencies);
        when(walletHelper.getBalance()).thenReturn(78237L);
        when(walletHelper.getLatestPrice()).thenReturn(new USDCurrency(1000.00D));

        start();

        DefaultCurrencyDisplayView view = withId(activity, R.id.balance);
        assertThat(view.getTotalCrypto().toLong(), equalTo(78237L));
        assertThat(view.getFiatValue().toLong(), equalTo(78L));
    }

    @Test
    public void invalidates_balances_when_price_changes() {
        DefaultCurrencies defaultCurrencies = new DefaultCurrencies(new USDCurrency(), new BTCCurrency());
        when(currencyPreference.getCurrenciesPreference()).thenReturn(defaultCurrencies);
        when(walletHelper.getBalance()).thenReturn(78237L);
        when(walletHelper.getLatestPrice()).thenReturn(new USDCurrency(1000.00D)).thenReturn(new USDCurrency(1100.00D));
        start();

        activity.onPriceReceived(new USDCurrency(1100.00d));

        DefaultCurrencyDisplayView view = withId(activity, R.id.balance);
        assertThat(view.getTotalCrypto().toLong(), equalTo(78237L));
        assertThat(view.getFiatValue().toLong(), equalTo(86L));
    }

    @Test
    public void invalidates_balances_when_wallet_syncs() {
        DefaultCurrencies defaultCurrencies = new DefaultCurrencies(new USDCurrency(), new BTCCurrency());
        when(currencyPreference.getCurrenciesPreference()).thenReturn(defaultCurrencies);
        when(walletHelper.getBalance()).thenReturn(78237L).thenReturn(80000L);
        when(walletHelper.getLatestPrice()).thenReturn(new USDCurrency(1000.00D));
        start();

        activity.receiver.onReceive(activity, new Intent(DropbitIntents.ACTION_WALLET_SYNC_COMPLETE));

        DefaultCurrencyDisplayView view = withId(activity, R.id.balance);
        assertThat(view.getTotalCrypto().toLong(), equalTo(80000L));
        assertThat(view.getFiatValue().toLong(), equalTo(80L));
    }

    @Test
    public void toggling_default_currency_invalidates_display() {
        when(walletHelper.getBalance()).thenReturn(78237L);
        when(walletHelper.getLatestPrice()).thenReturn(new USDCurrency(1000.00D));
        DefaultCurrencies defaultCurrencies = new DefaultCurrencies(new USDCurrency(), new BTCCurrency());
        when(currencyPreference.toggleDefault()).thenReturn(
                new DefaultCurrencies(new BTCCurrency(), new USDCurrency()));
        when(currencyPreference.getCurrenciesPreference()).thenReturn(defaultCurrencies);
        start();
        DefaultCurrencyDisplayView view = withId(activity, R.id.balance);

        assertThat(view.getPrimaryCurrencyText(), equalTo("$0.78"));
        assertThat(view.getSecondaryCurrencyText(), equalTo("0.00078237"));

        clickOn(withId(activity, R.id.balance));

        assertThat(view.getPrimaryCurrencyText(), equalTo("0.00078237"));
        assertThat(view.getSecondaryCurrencyText(), equalTo("$0.78"));
    }

    @Test
    public void unsubscribe_from_wallet_sync_broadcast_when_paused() {
        start();

        activity.onPause();

        verify(localBroadCastUtil).unregisterReceiver(activity.receiver);
    }

    private void start() {
        activityController.start().resume().visible();
    }

}