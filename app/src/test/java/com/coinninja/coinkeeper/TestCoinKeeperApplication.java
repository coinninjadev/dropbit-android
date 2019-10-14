package com.coinninja.coinkeeper;

import android.annotation.SuppressLint;
import android.content.ClipboardManager;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.util.ArraySet;
import android.util.TypedValue;
import android.view.View;

import androidx.test.espresso.intent.Intents;

import com.coinninja.coinkeeper.cn.account.AccountManager;
import com.coinninja.coinkeeper.cn.service.YearlyHighViewModel;
import com.coinninja.coinkeeper.cn.wallet.CNWalletManager;
import com.coinninja.coinkeeper.cn.wallet.HDWalletWrapper;
import com.coinninja.coinkeeper.cn.wallet.SyncWalletManager;
import com.coinninja.coinkeeper.cn.wallet.dust.DustProtectionPreference;
import com.coinninja.coinkeeper.cn.wallet.service.CNServiceConnection;
import com.coinninja.coinkeeper.cn.wallet.tx.TransactionFundingManager;
import com.coinninja.coinkeeper.di.component.CoinKeeperComponent;
import com.coinninja.coinkeeper.di.component.DaggerTestAppComponent;
import com.coinninja.coinkeeper.di.component.TestAppComponent;
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
import com.coinninja.coinkeeper.service.runner.NegativeBalanceRunner;
import com.coinninja.coinkeeper.service.runner.ReceivedInvitesStatusRunner;
import com.coinninja.coinkeeper.service.runner.SyncIncomingInvitesRunner;
import com.coinninja.coinkeeper.ui.account.verify.twitter.TwitterVerificationController;
import com.coinninja.coinkeeper.ui.base.TestableActivity;
import com.coinninja.coinkeeper.ui.dropbit.me.DropbitMeConfiguration;
import com.coinninja.coinkeeper.ui.settings.DeleteWalletPresenter;
import com.coinninja.coinkeeper.ui.transaction.DefaultCurrencyChangeViewNotifier;
import com.coinninja.coinkeeper.ui.transaction.SyncManagerViewNotifier;
import com.coinninja.coinkeeper.ui.transaction.history.TransactionHistoryDataAdapter;
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
import com.coinninja.coinkeeper.util.crypto.BitcoinUri;
import com.coinninja.coinkeeper.util.crypto.BitcoinUtil;
import com.coinninja.coinkeeper.view.widget.phonenumber.CountryCodeLocale;
import com.coinninja.coinkeeper.view.widget.phonenumber.CountryCodeLocaleGenerator;
import com.coinninja.messaging.MessageCryptor;

import org.robolectric.TestLifecycleApplication;
import org.robolectric.shadows.ShadowPackageManager;
import org.robolectric.util.ReflectionHelpers;

import java.lang.reflect.Method;
import java.util.List;

import javax.inject.Inject;

import app.dropbit.commons.currency.BTCCurrency;
import app.dropbit.commons.currency.USDCurrency;
import app.dropbit.twitter.Twitter;
import dagger.android.AndroidInjector;
import dagger.android.support.DaggerApplication;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.Robolectric.flushBackgroundThreadScheduler;
import static org.robolectric.Robolectric.flushForegroundThreadScheduler;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.shadows.ShadowApplication.runBackgroundTasks;
import static org.robolectric.shadows.ShadowLooper.runUiThreadTasksIncludingDelayedTasks;

/**
 * http://robolectric.org/custom-test-runner/
 */
public class TestCoinKeeperApplication extends CoinKeeperApplication implements TestLifecycleApplication {

    public AndroidInjector<DaggerApplication> injector;

    public DefaultCurrencyChangeViewNotifier defaultCurrencyChangeViewNotifier;
    public TransactionHistoryDataAdapter transactionHistoryDataAdapter;
    public SyncManagerViewNotifier syncManagerViewNotifier;
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
    public HDWalletWrapper hdWallet;
    public String UUID = "--UUID--";
    public SyncWalletManager syncWalletManager;
    @Inject
    public BitcoinUtil bitcoinUtil;
    public BitcoinUri.Builder bitcoinUriBuilder;
    public SharedPreferences sharedPreferences;
    @Inject
    public WalletHelper walletHelper;
    @Inject
    public Account account;
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
    public Wallet wallet = mock(Wallet.class);
    public CNServiceConnection cnServiceConnection = mock(CNServiceConnection.class);
    public TransactionHelper transactionHelper = mock(TransactionHelper.class);
    public BlockchainClient blockchainClient;
    public InternalNotificationHelper internalNotificationHelper = mock(InternalNotificationHelper.class);
    public TypedValue typedValue;
    public ActivityNavigationUtil activityNavigationUtil;
    public MessageCryptor messageCryptor;
    public PhoneNumberUtil phoneNumberUtil;
    public CoinKeeperLifecycleListener coinKeeperLifecycleListener;
    public AccountManager accountManager;
    @Inject
    public CurrencyPreference currencyPreference;
    public DefaultCurrencies defaultCurrencies = new DefaultCurrencies(new BTCCurrency(), new USDCurrency());
    public TransactionFundingManager transactionFundingManager;
    public LocationUtil locationUtil;
    public ClipboardUtil clipboardUtil;
    public UserPreferences userPreferences;
    public List<CountryCodeLocale> countryCodeLocales;
    public InviteContactPresenter inviteContactPresenter;
    public VerifyRecoveryWordsPresenter verifyRecoveryWordsPresenter;
    public NotificationUtil notificationUtil;
    public DropbitMeConfiguration dropbitMeConfiguration;
    public ServiceWorkUtil serviceWorkUtil;
    public RemoteAddressLocalCache remoteAddressLocalCache;
    public DropbitAccountHelper dropbitAccountHelper;
    public DustProtectionPreference dustProtectionPreference;
    public Twitter twitter;
    public TwitterVerificationController twitterVerificationController;
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
        when(walletHelper.getSpendableBalance()).thenReturn(new BTCCurrency(0));
        when(walletHelper.getLatestPrice()).thenReturn(new USDCurrency());
        PhoneNumber phoneNumber = new PhoneNumber("+15550123456");
        when(account.getPhoneNumber()).thenReturn(phoneNumber);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(currencyPreference.getCurrenciesPreference()).thenReturn(defaultCurrencies);
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
        injector = null;
        USDCurrency.maxLongValue = Long.MAX_VALUE;
        twitter = null;
        locationUtil = null;
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
        phoneNumberUtil = null;
        version_name = null;
        transactionHelper = null;
        blockchainClient = null;
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
        jobServiceScheduler = null;
        wallet = null;
        cnServiceConnection = null;
        typedValue = null;
        coinKeeperLifecycleListener = null;
        inviteContactPresenter = null;
        serviceWorkUtil = null;
        remoteAddressLocalCache = null;
        dropbitAccountHelper = null;
        hasher = null;
        twitterVerificationController = null;
        preferencesUtil = null;
        countryCodeLocaleGenerator = null;
        deleteWalletPresenter = null;
        dustProtectionPreference = null;
        yearlyHighViewModel = null;
        defaultCurrencyChangeViewNotifier = null;
        transactionHistoryDataAdapter = null;
        syncManagerViewNotifier = null;
        try {
            resetBackgroundThread();
        } catch (Exception e) {
            e.printStackTrace();
        }
        resetWindowManager();
        finishThreads();
    }


    public void finishThreads() {
        runBackgroundTasks();
        flushForegroundThreadScheduler();
        flushBackgroundThreadScheduler();
        runUiThreadTasksIncludingDelayedTasks();
    }

    // https://github.com/robolectric/robolectric/pull/1741
    private void resetBackgroundThread() throws Exception {
        Class<?> btclass = Class.forName("com.android.internal.os.BackgroundThread");
        Object backgroundThreadSingleton = ReflectionHelpers.getStaticField(btclass, "sInstance");
        if (backgroundThreadSingleton != null) {
            btclass.getMethod("quit").invoke(backgroundThreadSingleton);
            ReflectionHelpers.setStaticField(btclass, "sInstance", null);
            ReflectionHelpers.setStaticField(btclass, "sHandler", null);
        }
    }

    // https://github.com/robolectric/robolectric/issues/2068#issue-109132096
    @SuppressLint("NewApi")
    private void resetWindowManager() {
        Class<?> clazz = ReflectionHelpers.loadClass(getClass().getClassLoader(), "android.view.WindowManagerGlobal");
        Object instance = ReflectionHelpers.callStaticMethod(clazz, "getInstance");

        // We essentially duplicate what's in {@link WindowManagerGlobal#closeAll} with what's below.
        // The closeAll method has a bit of a bug where it's iterating through the "roots" but
        // bases the number of objects to iterate through by the number of "views." This can result in
        // an {@link java.lang.IndexOutOfBoundsException} being thrown.
        Object lock = ReflectionHelpers.getField(instance, "mLock");

        List<Object> roots = ReflectionHelpers.getField(instance, "mRoots");
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (lock) {
            for (int i = 0; i < roots.size(); i++) {
                ReflectionHelpers.callInstanceMethod(instance, "removeViewLocked",
                        ReflectionHelpers.ClassParameter.from(int.class, i),
                        ReflectionHelpers.ClassParameter.from(boolean.class, false));
            }
        }

        // Views will still be held by this array. We need to clear it out to ensure
        // everything is released.
        ArraySet<View> dyingViews = ReflectionHelpers.getField(instance, "mDyingViews");
        dyingViews.clear();
    }
}


