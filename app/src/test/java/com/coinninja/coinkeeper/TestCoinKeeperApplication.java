package com.coinninja.coinkeeper;

import android.content.ClipboardManager;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.util.TypedValue;

import androidx.test.espresso.intent.Intents;

import com.coinninja.bindings.TransactionBuilder;
import com.coinninja.coinkeeper.cn.account.AccountManager;
import com.coinninja.coinkeeper.cn.service.YearlyHighViewModel;
import com.coinninja.coinkeeper.cn.wallet.CNWalletManager;
import com.coinninja.coinkeeper.cn.wallet.DataSigner;
import com.coinninja.coinkeeper.cn.wallet.HDWallet;
import com.coinninja.coinkeeper.cn.wallet.SyncWalletManager;
import com.coinninja.coinkeeper.cn.wallet.dust.DustProtectionPreference;
import com.coinninja.coinkeeper.cn.wallet.service.CNAddressLookupDelegate;
import com.coinninja.coinkeeper.cn.wallet.service.CNServiceConnection;
import com.coinninja.coinkeeper.cn.wallet.tx.TransactionFundingManager;
import com.coinninja.coinkeeper.di.component.CoinKeeperComponent;
import com.coinninja.coinkeeper.di.component.DaggerTestAppComponent;
import com.coinninja.coinkeeper.di.component.TestAppComponent;
import com.coinninja.coinkeeper.interactor.InternalNotificationsInteractor;
import com.coinninja.coinkeeper.interactor.UserPreferences;
import com.coinninja.coinkeeper.interfaces.Authentication;
import com.coinninja.coinkeeper.interfaces.PinEntry;
import com.coinninja.coinkeeper.model.PhoneNumber;
import com.coinninja.coinkeeper.model.db.Account;
import com.coinninja.coinkeeper.model.db.Wallet;
import com.coinninja.coinkeeper.model.helpers.DropbitAccountHelper;
import com.coinninja.coinkeeper.model.helpers.InternalNotificationHelper;
import com.coinninja.coinkeeper.model.helpers.TransactionHelper;
import com.coinninja.coinkeeper.model.helpers.UserHelper;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.presenter.activity.InviteContactPresenter;
import com.coinninja.coinkeeper.presenter.fragment.VerifyRecoveryWordsPresenter;
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
import com.coinninja.coinkeeper.ui.account.verify.twitter.TwitterVerificationController;
import com.coinninja.coinkeeper.ui.actionbar.ActionBarController;
import com.coinninja.coinkeeper.ui.actionbar.managers.DrawerController;
import com.coinninja.coinkeeper.ui.base.TestableActivity;
import com.coinninja.coinkeeper.ui.dropbit.me.DropbitMeConfiguration;
import com.coinninja.coinkeeper.ui.settings.DeleteWalletPresenter;
import com.coinninja.coinkeeper.util.CurrencyPreference;
import com.coinninja.coinkeeper.util.DefaultCurrencies;
import com.coinninja.coinkeeper.util.Hasher;
import com.coinninja.coinkeeper.util.LocalContactQueryUtil;
import com.coinninja.coinkeeper.util.NotificationUtil;
import com.coinninja.coinkeeper.util.PhoneNumberUtil;
import com.coinninja.coinkeeper.util.RemoteAddressLocalCache;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.android.ClipboardUtil;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;
import com.coinninja.coinkeeper.util.android.LocationUtil;
import com.coinninja.coinkeeper.util.android.PermissionsUtil;
import com.coinninja.coinkeeper.util.android.PreferencesUtil;
import com.coinninja.coinkeeper.util.android.ServiceWorkUtil;
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil;
import com.coinninja.coinkeeper.util.android.app.JobIntentService.JobServiceScheduler;
import com.coinninja.coinkeeper.util.crypto.BitcoinUtil;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.currency.USDCurrency;
import com.coinninja.coinkeeper.view.widget.phonenumber.CountryCodeLocale;
import com.coinninja.coinkeeper.view.widget.phonenumber.CountryCodeLocaleGenerator;
import com.coinninja.messaging.MessageCryptor;

import org.jetbrains.annotations.Nullable;
import org.robolectric.TestLifecycleApplication;
import org.robolectric.shadows.ShadowPackageManager;

import java.lang.reflect.Method;
import java.util.List;

import javax.inject.Inject;

import app.dropbit.twitter.Twitter;
import dagger.android.AndroidInjector;
import dagger.android.support.DaggerApplication;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

/**
 * http://robolectric.org/custom-test-runner/
 */
public class TestCoinKeeperApplication extends CoinKeeperApplication implements TestLifecycleApplication {

    public static AndroidInjector<DaggerApplication> injector;

    public YearlyHighViewModel yearlyHighViewModel;
    public PreferencesUtil preferencesUtil;
    public QRScanManager qrScanManager = mock(QRScanManager.class);
    public LocalContactQueryUtil localContactQueryUtil = mock(LocalContactQueryUtil.class);
    public LocalContactQueryUtil coinNinjaUserQueryTask = mock(LocalContactQueryUtil.class);
    public Hasher hasher;
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
    public CNAddressLookupDelegate cnAddressLookupDelegae;
    public ClipboardUtil clipboardUtil;
    public UserPreferences userPreferences;
    public List<CountryCodeLocale> countryCodeLocales;
    public InviteContactPresenter inviteContactPresenter;
    public VerifyRecoveryWordsPresenter verifyRecoveryWordsPresenter;
    public NotificationUtil notificationUtil;
    public DropbitMeConfiguration dropbitMeConfiguration;
    public InternalNotificationsInteractor internalNotificationsInteractor;
    public DrawerController drawerController;
    public ServiceWorkUtil serviceWorkUtil;
    public RemoteAddressLocalCache remoteAddressLocalCache;
    public DropbitAccountHelper dropbitAccountHelper;
    public DustProtectionPreference dustProtectionPreference;
    public Twitter twitter;
    public TwitterVerificationController twitterVerificationController;
    @Nullable
    public CountryCodeLocaleGenerator countryCodeLocaleGenerator;
    public DeleteWalletPresenter deleteWalletPresenter;

    @Override
    public void onCreate() {
        super.onCreate();
        ShadowPackageManager shadowPackageManager = shadowOf(getApplicationContext().getPackageManager());
        ActivityInfo activityInfo = new ActivityInfo();
        activityInfo.name = TestableActivity.class.getName();
        activityInfo.packageName = getPackageName();
        shadowPackageManager.addOrUpdateActivity(activityInfo);
    }

    @Override
    protected AndroidInjector<? extends DaggerApplication> applicationInjector() {
        injector = DaggerTestAppComponent.builder().application(this).build();
        ((TestAppComponent) injector).inject(this);
        appComponent = (CoinKeeperComponent) injector;
        return injector;
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
        try {
            Intents.init();
        } catch (IllegalStateException e) {
            // pass
        }

    }

    @Override
    public void afterTest(Method method) {
        try {
            Intents.release();
        } catch (IllegalStateException e) {
            // pass
        } catch (NullPointerException e) {
            // pass
        }
        USDCurrency.MAX_DOLLAR_AMOUNT = Long.MAX_VALUE;
        twitter = null;
        locationUtil = null;
        injector = null;
        appComponent = null;
        dropbitMeConfiguration = null;
        notificationUtil = null;
        verifyRecoveryWordsPresenter = null;
        activityNavigationUtil = null;
        transactionFundingManager = null;
        defaultCurrencies = null;
        currencyPreference = null;
        accountManager = null;
        internalNotificationHelper = null;
        countryCodeLocales = null;
        clipboardUtil = null;
        messageCryptor = null;
        cnAddressLookupDelegae = null;
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
        userPreferences = null;
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
        inviteContactPresenter = null;
        internalNotificationsInteractor = null;
        drawerController = null;
        serviceWorkUtil = null;
        remoteAddressLocalCache = null;
        dropbitAccountHelper = null;
        hasher = null;
        twitterVerificationController = null;
        preferencesUtil = null;
        countryCodeLocales = null;
        countryCodeLocaleGenerator = null;
        deleteWalletPresenter = null;
        dustProtectionPreference = null;
        yearlyHighViewModel = null;
    }
}


