package com.coinninja.coinkeeper;

import android.content.ClipboardManager;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.TypedValue;

import com.coinninja.bindings.TransactionBuilder;
import com.coinninja.coinkeeper.cn.account.AccountManager;
import com.coinninja.coinkeeper.cn.wallet.CNWalletManager;
import com.coinninja.coinkeeper.cn.wallet.DataSigner;
import com.coinninja.coinkeeper.cn.wallet.HDWallet;
import com.coinninja.coinkeeper.cn.wallet.SyncWalletManager;
import com.coinninja.coinkeeper.cn.wallet.service.CNServiceConnection;
import com.coinninja.coinkeeper.cn.wallet.tx.TransactionFundingManager;
import com.coinninja.coinkeeper.di.component.AppComponent;
import com.coinninja.coinkeeper.di.component.DaggerTestAppComponent;
import com.coinninja.coinkeeper.di.component.TestAppComponent;
import com.coinninja.coinkeeper.interfaces.Authentication;
import com.coinninja.coinkeeper.interfaces.PinEntry;
import com.coinninja.coinkeeper.model.PhoneNumber;
import com.coinninja.coinkeeper.model.db.Account;
import com.coinninja.coinkeeper.model.db.Wallet;
import com.coinninja.coinkeeper.model.helpers.InternalNotificationHelper;
import com.coinninja.coinkeeper.model.helpers.TransactionHelper;
import com.coinninja.coinkeeper.model.helpers.UserHelper;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.qrscanner.QRScanManager;
import com.coinninja.coinkeeper.service.client.BlockchainClient;
import com.coinninja.coinkeeper.service.client.CoinKeeperApiClient;
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient;
import com.coinninja.coinkeeper.service.runner.FailedBroadcastCleaner;
import com.coinninja.coinkeeper.service.runner.FulfillSentInvitesRunner;
import com.coinninja.coinkeeper.service.runner.HealthCheckTimerRunner;
import com.coinninja.coinkeeper.service.runner.NegativeBalanceRunner;
import com.coinninja.coinkeeper.service.runner.ReceivedInvitesStatusRunner;
import com.coinninja.coinkeeper.service.runner.SyncIncomingInvitesRunner;
import com.coinninja.coinkeeper.service.tasks.CoinNinjaUserQueryTask;
import com.coinninja.coinkeeper.ui.actionbar.ActionBarController;
import com.coinninja.coinkeeper.util.CurrencyPreference;
import com.coinninja.coinkeeper.util.DefaultCurrencies;
import com.coinninja.coinkeeper.util.LocalContactQueryUtil;
import com.coinninja.coinkeeper.util.PhoneNumberUtil;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;
import com.coinninja.coinkeeper.util.android.LocationUtil;
import com.coinninja.coinkeeper.util.android.PermissionsUtil;
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil;
import com.coinninja.coinkeeper.util.android.app.JobIntentService.JobServiceScheduler;
import com.coinninja.coinkeeper.util.crypto.BitcoinUtil;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.currency.USDCurrency;
import com.coinninja.messaging.MessageCryptor;

import org.robolectric.TestLifecycleApplication;

import java.lang.reflect.Method;

import javax.inject.Inject;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * http://robolectric.org/custom-test-runner/
 */
public class TestCoinKeeperApplication extends CoinKeeperApplication implements TestLifecycleApplication {
    public QRScanManager qrScanManager = mock(QRScanManager.class);
    public LocalContactQueryUtil localContactQueryUtil = mock(LocalContactQueryUtil.class);
    public CoinNinjaUserQueryTask coinNinjaUserQueryTask = mock(CoinNinjaUserQueryTask.class);
    @Inject
    public Analytics analytics;
    public ClipboardManager clipboardManager;
    public LocalBroadCastUtil localBroadCastUtil;
    @Inject
    public HDWallet hdWallet;
    public String UUID = "--UUID--";
    public SyncWalletManager syncWalletManager;
    @Inject
    public BitcoinUtil bitcoinUtil;
    public SharedPreferences sharedPreferences;
    @Inject
    public WalletHelper walletHelper;
    @Inject
    public Account account;
    public DataSigner dataSigner;
    public JobServiceScheduler jobServiceScheduler;
    public TestAppComponent injector;
    public String version_name = "1.2.1";
    public boolean debug = true;
    @Inject
    public Authentication authentication;
    @Inject
    public PinEntry pinEntry;
    @Inject
    public UserHelper userHelper;
    public Handler handler;
    @Inject
    public CNWalletManager cnWalletManager;
    public SyncIncomingInvitesRunner syncIncomingInvitesRunner;
    public ReceivedInvitesStatusRunner receivedInvitesStatusRunner;
    public FulfillSentInvitesRunner fulfillSentInvitesRunner;
    public NegativeBalanceRunner negativeBalanceRunner;
    public FailedBroadcastCleaner failedBroadcastCleaner;
    @Inject
    public PermissionsUtil permissionsUtil;
    @Inject
    public SignedCoinKeeperApiClient signedCoinKeeperApiClient;
    public CoinKeeperApiClient coinKeeperApiClient;
    public HealthCheckTimerRunner healthCheckTimerRunner;
    public Wallet wallet = mock(Wallet.class);
    public CNServiceConnection cnServiceConnection = mock(CNServiceConnection.class);
    public TransactionBuilder transactionBuilder = mock(TransactionBuilder.class);
    public TransactionHelper transactionHelper = mock(TransactionHelper.class);
    public BlockchainClient blockchainClient;
    public InternalNotificationHelper internalNotificationHelper = mock(InternalNotificationHelper.class);
    public TypedValue typedValue;
    public ActivityNavigationUtil activityNavigationUtil;
    public ActionBarController actionBarController;
    public MessageCryptor messageCryptor;
    public PhoneNumberUtil phoneNumberUtil;
    public CoinKeeperLifecycleListener coinKeeperLifecycleListener;
    public AccountManager accountManager;
    @Inject
    public CurrencyPreference currencyPreference;
    public DefaultCurrencies defaultCurrencies = new DefaultCurrencies(new BTCCurrency(), new USDCurrency());
    public TransactionFundingManager transactionFundingManager;
    public LocationUtil locationUtil;

    @Override
    public AppComponent getAppComponent() {
        return injector;
    }

    @Override
    protected void createComponent() {
        injector = DaggerTestAppComponent.builder().application(this).build();
        injector.inject(this);
        appComponent = injector;
    }

    @Override
    protected void notifyOfStart() {
    }

    @Override
    public void beforeTest(Method method) {
        when(walletHelper.getUserAccount()).thenReturn(account);
        PhoneNumber phoneNumber = new PhoneNumber("+15550123456");
        when(account.getPhoneNumber()).thenReturn(phoneNumber);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(currencyPreference.getCurrenciesPreference()).thenReturn(defaultCurrencies);

        actionBarController = mock(ActionBarController.class);
    }

    @Override
    public void prepareTest(Object test) {

    }

    @Override
    public void afterTest(Method method) {
        locationUtil = null;
        activityNavigationUtil = null;
        transactionFundingManager = null;
        defaultCurrencies = null;
        currencyPreference = null;
        accountManager = null;
        internalNotificationHelper = null;
        messageCryptor = null;
        phoneNumberUtil = null;
        version_name = null;
        transactionHelper = null;
        blockchainClient = null;
        transactionBuilder = null;
        healthCheckTimerRunner = null;
        coinKeeperApiClient = null;
        qrScanManager = null;
        signedCoinKeeperApiClient = null;
        permissionsUtil = null;
        coinNinjaUserQueryTask = null;
        failedBroadcastCleaner = null;
        negativeBalanceRunner = null;
        fulfillSentInvitesRunner = null;
        receivedInvitesStatusRunner = null;
        syncIncomingInvitesRunner = null;
        handler = null;
        cnWalletManager = null;
        pinEntry = null;
        userHelper = null;
        analytics = null;
        authentication = null;
        clipboardManager = null;
        localContactQueryUtil = null;
        localBroadCastUtil = null;
        hdWallet = null;
        syncWalletManager = null;
        UUID = null;
        bitcoinUtil = null;
        sharedPreferences = null;
        walletHelper = null;
        account = null;
        dataSigner = null;
        jobServiceScheduler = null;
        wallet = null;
        cnServiceConnection = null;
        typedValue = null;
        actionBarController = null;
        coinKeeperLifecycleListener = null;
    }

}


