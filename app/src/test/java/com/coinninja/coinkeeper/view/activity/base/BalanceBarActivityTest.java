package com.coinninja.coinkeeper.view.activity.base;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.widget.TextView;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.model.db.TransactionsInvitesSummary;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.service.blockchain.BlockChainService;
import com.coinninja.coinkeeper.ui.transaction.history.TransactionHistoryActivity;
import com.coinninja.coinkeeper.util.CurrencyPreference;
import com.coinninja.coinkeeper.util.DefaultCurrencies;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.currency.USDCurrency;
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
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowApplication;

import java.util.List;

import static com.coinninja.android.helpers.Views.clickOn;
import static com.coinninja.android.helpers.Views.withId;
import static com.coinninja.matchers.TextViewMatcher.hasText;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNull;
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
    private ActivityController<TransactionHistoryActivity> activityController;
    private BlockChainService.BlockChainBinder binder;
    private ShadowApplication shadowApplication;

    @Mock
    private LocalBroadCastUtil localBroadCastUtil;

    @Mock
    private CurrencyPreference currencyPreference;

    @Mock
    private WalletHelper walletHelper;

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
        TestCoinKeeperApplication application = (TestCoinKeeperApplication) RuntimeEnvironment.application;
        application.walletHelper = walletHelper;
        binder = mock(BlockChainService.BlockChainBinder.class);
        shadowApplication = shadowOf(application);

        USDCurrency cachedPrice = new USDCurrency(650000L);
        when(walletHelper.getLatestPrice()).thenReturn(cachedPrice);
        when(walletHelper.getTransactionsLazily()).thenReturn(transactions);
        when(transactions.isEmpty()).thenReturn(true);
        DefaultCurrencies defaultCurrencies = new DefaultCurrencies(new BTCCurrency(), new USDCurrency());
        when(currencyPreference.getCurrenciesPreference()).thenReturn(defaultCurrencies);

        activityController = Robolectric.buildActivity(TransactionHistoryActivity.class).create();
        activity = activityController.get();
        activity.serviceBinder = binder;
        activity.localBroadCastUtil = localBroadCastUtil;
        activity.currencyPreference = currencyPreference;
        activity.walletHelper = walletHelper;
        shadowActivity = shadowOf(activity);
        start();
    }

    private void start() {
        activityController.start().resume();
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
        assertThat(activity.filter, IntentFilterMatchers.containsAction(Intents.ACTION_WALLET_SYNC_COMPLETE));
        assertThat(activity.filter, IntentFilterMatchers.containsAction(Intents.ACTION_BTC_PRICE_UPDATE));
    }

    @Test
    public void stops_observing_price_change_when_stopped() {
        start();

        activityController.pause().stop();

        verify(localBroadCastUtil).unregisterReceiver(activity.receiver);
    }

    @Test
    public void itUpdatesBTCPriceOnChange() {
        start();

        Intent intent = new Intent().setAction(Intents.ACTION_BTC_PRICE_UPDATE).
                putExtra(Intents.EXTRA_BITCOIN_PRICE, 1200L);
        activity.receiver.onReceive(activity, intent);

        assertThat(((TextView) activity.findViewById(R.id.drawer_action_price_text)).
                getText().toString(), equalTo("$12.00"));
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

        ActivityController<TransactionHistoryActivity> activityCreate = Robolectric.buildActivity(TransactionHistoryActivity.class).create();
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

        assertThat(withId(activity, R.id.primary_balance), hasText("0.00078237"));
        assertThat(withId(activity, R.id.alt_balance), hasText("$0.78"));
    }

    @Test
    public void sets_balances_of_BTC_from_DB__usd_primary() {
        DefaultCurrencies defaultCurrencies = new DefaultCurrencies(new USDCurrency(), new BTCCurrency());
        when(currencyPreference.getCurrenciesPreference()).thenReturn(defaultCurrencies);
        when(walletHelper.getBalance()).thenReturn(78237L);
        when(walletHelper.getLatestPrice()).thenReturn(new USDCurrency(1000.00D));

        start();

        assertThat(withId(activity, R.id.primary_balance), hasText("$0.78"));
        assertThat(withId(activity, R.id.alt_balance), hasText("0.00078237"));
    }

    @Test
    public void invalidates_balances_when_price_changes() {
        DefaultCurrencies defaultCurrencies = new DefaultCurrencies(new USDCurrency(), new BTCCurrency());
        when(currencyPreference.getCurrenciesPreference()).thenReturn(defaultCurrencies);
        when(walletHelper.getBalance()).thenReturn(78237L);
        when(walletHelper.getLatestPrice()).thenReturn(new USDCurrency(1000.00D)).thenReturn(new USDCurrency(1100.00D));
        start();

        activity.onPriceReceived(new USDCurrency(1100.00d));

        assertThat(withId(activity, R.id.primary_balance), hasText("$0.86"));
        assertThat(withId(activity, R.id.alt_balance), hasText("0.00078237"));
    }

    @Test
    public void invalidates_balances_when_wallet_syncs() {
        DefaultCurrencies defaultCurrencies = new DefaultCurrencies(new USDCurrency(), new BTCCurrency());
        when(currencyPreference.getCurrenciesPreference()).thenReturn(defaultCurrencies);
        when(walletHelper.getBalance()).thenReturn(78237L).thenReturn(80000L);
        when(walletHelper.getLatestPrice()).thenReturn(new USDCurrency(1000.00D));
        start();

        activity.receiver.onReceive(activity, new Intent(Intents.ACTION_WALLET_SYNC_COMPLETE));

        assertThat(withId(activity, R.id.primary_balance), hasText("$0.80"));
        assertThat(withId(activity, R.id.alt_balance), hasText("0.0008"));
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

        assertThat(withId(activity, R.id.primary_balance), hasText("$0.78"));
        assertThat(withId(activity, R.id.alt_balance), hasText("0.00078237"));

        clickOn(withId(activity, R.id.balance));

        assertThat(withId(activity, R.id.primary_balance), hasText("0.00078237"));
        assertThat(withId(activity, R.id.alt_balance), hasText("$0.78"));
    }

    @Test
    public void shows_btc_icon_when_primary() {
        DefaultCurrencies defaultCurrencies = new DefaultCurrencies(new BTCCurrency(), new USDCurrency());
        when(currencyPreference.getCurrenciesPreference()).thenReturn(defaultCurrencies);
        start();

        TextView primary = withId(activity, R.id.primary_balance);
        TextView alt = withId(activity, R.id.alt_balance);

        Drawable[] primaryCompoundDrawables = primary.getCompoundDrawables();
        assertThat(shadowOf(primaryCompoundDrawables[0]).getCreatedFromResId(), equalTo(R.drawable.ic_btc_icon));
        assertNull(primaryCompoundDrawables[1]);
        assertNull(primaryCompoundDrawables[2]);
        assertNull(primaryCompoundDrawables[3]);

        Drawable[] altCompoundDrawables = alt.getCompoundDrawables();
        assertNull(altCompoundDrawables[0]);
        assertNull(altCompoundDrawables[1]);
        assertNull(altCompoundDrawables[2]);
        assertNull(altCompoundDrawables[3]);
    }

    @Test
    public void shows_btc_icon_when_secondary() {
        DefaultCurrencies defaultCurrencies = new DefaultCurrencies(new USDCurrency(), new BTCCurrency());
        when(currencyPreference.getCurrenciesPreference()).thenReturn(defaultCurrencies);
        start();

        TextView primary = withId(activity, R.id.primary_balance);
        TextView alt = withId(activity, R.id.alt_balance);

        Drawable[] primaryCompoundDrawables = primary.getCompoundDrawables();
        assertNull(primaryCompoundDrawables[0]);
        assertNull(primaryCompoundDrawables[1]);
        assertNull(primaryCompoundDrawables[2]);
        assertNull(primaryCompoundDrawables[3]);

        Drawable[] altCompoundDrawables = alt.getCompoundDrawables();
        assertThat(shadowOf(altCompoundDrawables[0]).getCreatedFromResId(), equalTo(R.drawable.ic_btc_icon));
        assertNull(altCompoundDrawables[1]);
        assertNull(altCompoundDrawables[2]);
        assertNull(altCompoundDrawables[3]);
    }

    @Test
    public void unsubscribe_from_wallet_sync_broadcast_when_paused() {
        start();

        activity.onPause();

        verify(localBroadCastUtil).unregisterReceiver(activity.receiver);
    }

}