package com.coinninja.coinkeeper.di.module;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Handler;
import android.util.TypedValue;

import androidx.core.os.ConfigurationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.coinninja.bindings.TransactionBuilder;
import com.coinninja.coinkeeper.CoinKeeperApplication;
import com.coinninja.coinkeeper.CoinKeeperLifecycleListener;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.cn.account.AccountManager;
import com.coinninja.coinkeeper.cn.wallet.CNWalletManager;
import com.coinninja.coinkeeper.cn.wallet.DataSigner;
import com.coinninja.coinkeeper.cn.wallet.HDWallet;
import com.coinninja.coinkeeper.cn.wallet.SyncWalletManager;
import com.coinninja.coinkeeper.cn.wallet.service.CNAddressLookupDelegate;
import com.coinninja.coinkeeper.cn.wallet.service.CNServiceConnection;
import com.coinninja.coinkeeper.cn.wallet.tx.TransactionFundingManager;
import com.coinninja.coinkeeper.di.component.CoinKeeperComponent;
import com.coinninja.coinkeeper.di.component.TestAppComponent;
import com.coinninja.coinkeeper.di.interfaces.ApplicationContext;
import com.coinninja.coinkeeper.di.interfaces.BuildVersionName;
import com.coinninja.coinkeeper.di.interfaces.CoinkeeperApplicationScope;
import com.coinninja.coinkeeper.di.interfaces.CountryCodeLocales;
import com.coinninja.coinkeeper.di.interfaces.DebugBuild;
import com.coinninja.coinkeeper.di.interfaces.NumAddressesToCache;
import com.coinninja.coinkeeper.di.interfaces.ThreadHandler;
import com.coinninja.coinkeeper.di.interfaces.TimeOutHandler;
import com.coinninja.coinkeeper.di.interfaces.UUID;
import com.coinninja.coinkeeper.interactor.InternalNotificationsInteractor;
import com.coinninja.coinkeeper.interactor.UserPreferences;
import com.coinninja.coinkeeper.interfaces.Authentication;
import com.coinninja.coinkeeper.interfaces.PinEntry;
import com.coinninja.coinkeeper.model.db.Account;
import com.coinninja.coinkeeper.model.helpers.UserHelper;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.presenter.activity.InviteContactPresenter;
import com.coinninja.coinkeeper.presenter.fragment.VerifyRecoveryWordsPresenter;
import com.coinninja.coinkeeper.service.ContactLookupService;
import com.coinninja.coinkeeper.service.client.BlockstreamClient;
import com.coinninja.coinkeeper.service.runner.FailedBroadcastCleaner;
import com.coinninja.coinkeeper.service.runner.FulfillSentInvitesRunner;
import com.coinninja.coinkeeper.service.runner.HealthCheckTimerRunner;
import com.coinninja.coinkeeper.service.runner.NegativeBalanceRunner;
import com.coinninja.coinkeeper.service.runner.ReceivedInvitesStatusRunner;
import com.coinninja.coinkeeper.service.runner.SyncIncomingInvitesRunner;
import com.coinninja.coinkeeper.ui.actionbar.ActionBarController;
import com.coinninja.coinkeeper.ui.actionbar.managers.DrawerController;
import com.coinninja.coinkeeper.ui.dropbit.me.DropbitMeConfiguration;
import com.coinninja.coinkeeper.util.AnalyticUtil;
import com.coinninja.coinkeeper.util.CurrencyPreference;
import com.coinninja.coinkeeper.util.DefaultCurrencies;
import com.coinninja.coinkeeper.util.ErrorLoggingUtil;
import com.coinninja.coinkeeper.util.NotificationUtil;
import com.coinninja.coinkeeper.util.PhoneNumberUtil;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.android.ClipboardUtil;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;
import com.coinninja.coinkeeper.util.android.LocationUtil;
import com.coinninja.coinkeeper.util.android.PermissionsUtil;
import com.coinninja.coinkeeper.util.android.ServiceWorkUtil;
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil;
import com.coinninja.coinkeeper.util.android.app.JobIntentService.JobServiceScheduler;
import com.coinninja.coinkeeper.util.crypto.BitcoinUtil;
import com.coinninja.coinkeeper.util.uri.BitcoinUriBuilder;
import com.coinninja.coinkeeper.view.widget.phonenumber.CountryCodeLocale;
import com.coinninja.messaging.MessageCryptor;
import com.google.gson.Gson;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dagger.Module;
import dagger.Provides;

import static org.mockito.Mockito.mock;

@Module
public class TestAppModule {

    @Provides
    AnalyticUtil analyticUtil(MixpanelAPI analyticsProvider) {
        return new AnalyticUtil(analyticsProvider);
    }

    @Provides
    ErrorLoggingUtil errorLoggingUtil() {
        return new ErrorLoggingUtil();
    }

    @Provides
    BlockstreamClient blockstreamClient() {
        return BlockstreamClient.newInstance();
    }

    @Provides
    CoinKeeperApplication coinKeeperApplication(Application application) {
        return (CoinKeeperApplication) application;
    }

    @Provides
    TestCoinKeeperApplication testCoinKeeperApplication(Application application) {
        return (TestCoinKeeperApplication) application;
    }

    @Provides
    @CoinkeeperApplicationScope
    BitcoinUriBuilder bitcoinUrlBuilder() {
        return new BitcoinUriBuilder();
    }


    @Provides
    CoinKeeperComponent coinKeeperComponent(TestAppComponent appComponent) {
        return appComponent;
    }

    @Provides
    @CoinkeeperApplicationScope
    CoinKeeperLifecycleListener coinKeeperLifecycleListener(TestCoinKeeperApplication app) {
        if (app.coinKeeperLifecycleListener == null)
            app.coinKeeperLifecycleListener = mock(CoinKeeperLifecycleListener.class);
        return app.coinKeeperLifecycleListener;
    }

    @Provides
    com.google.i18n.phonenumbers.PhoneNumberUtil provideI18PhoneNumberUtil() {
        return com.google.i18n.phonenumbers.PhoneNumberUtil.getInstance();
    }

    @Provides
    @CoinkeeperApplicationScope
    @ApplicationContext
    Context context(CoinKeeperApplication application) {
        return application;
    }

    @Provides
    @CoinkeeperApplicationScope
    ContentResolver contentResolver(TestCoinKeeperApplication application) {
        return application.getContentResolver();
    }

    @Provides
    @CoinkeeperApplicationScope
    ContactLookupService contactLookupService() {
        return new ContactLookupService();
    }

    @Provides
    @CoinkeeperApplicationScope
    Analytics analytics(TestCoinKeeperApplication app) {
        if (app.analytics == null)
            app.analytics = mock(Analytics.class);
        return app.analytics;
    }

    @Provides
    MessageCryptor messageCryptor(TestCoinKeeperApplication app) {
        if (app.messageCryptor == null)
            app.messageCryptor = mock(MessageCryptor.class);
        return app.messageCryptor;
    }

    @Provides
    PhoneNumberUtil phoneNumberUtil(TestCoinKeeperApplication app) {
        if (app.phoneNumberUtil == null)
            app.phoneNumberUtil = mock(PhoneNumberUtil.class);
        return app.phoneNumberUtil;
    }

    @Provides
    @UUID
    @SuppressLint("HardwareIds")
    String uuid(TestCoinKeeperApplication app) {
        return app.UUID;
    }

    @Provides
    LocalBroadcastManager localBroadcastManager(@ApplicationContext Context context) {
        // Robolectric will dispatch real broadcasts
        return LocalBroadcastManager.getInstance(context);
    }

    @Provides
    LocalBroadCastUtil localBroadCastUtil(TestCoinKeeperApplication app) {
        if (app.localBroadCastUtil == null)
            app.localBroadCastUtil = mock(LocalBroadCastUtil.class);
        return app.localBroadCastUtil;
    }

    @Provides
    ClipboardManager clipboardManager(TestCoinKeeperApplication app) {
        if (app.clipboardManager == null)
            app.clipboardManager = mock(ClipboardManager.class);
        return app.clipboardManager;
    }

    @Provides
    HDWallet hdWallet(TestCoinKeeperApplication app) {
        if (app.hdWallet == null)
            app.hdWallet = mock(HDWallet.class);
        return app.hdWallet;
    }

    @Provides
    BitcoinUtil bitcoinUtil(TestCoinKeeperApplication app) {
        if (app.bitcoinUtil == null)
            app.bitcoinUtil = mock(BitcoinUtil.class);
        return app.bitcoinUtil;
    }

    @Provides
    SyncWalletManager syncWalletManager(TestCoinKeeperApplication app) {
        if (app.syncWalletManager == null) {
            app.syncWalletManager = mock(SyncWalletManager.class);
        }
        return app.syncWalletManager;
    }

    @Provides
    SharedPreferences sharedPreferences(TestCoinKeeperApplication app) {
        if (app.sharedPreferences == null) {
            app.sharedPreferences = mock(SharedPreferences.class);
        }
        return app.sharedPreferences;
    }

    @Provides
    WalletHelper walletHelper(TestCoinKeeperApplication app) {
        if (app.walletHelper == null) {
            app.walletHelper = mock(WalletHelper.class);
        }
        return app.walletHelper;
    }

    @Provides
    Account account(TestCoinKeeperApplication app) {
        if (app.account == null) {
            app.account = mock(Account.class);
        }
        return app.account;
    }

    @Provides
    DataSigner dataSigner(TestCoinKeeperApplication app) {
        if (app.dataSigner == null) {
            app.dataSigner = mock(DataSigner.class);
        }
        return app.dataSigner;
    }

    @Provides
    JobServiceScheduler jobServiceScheduler(TestCoinKeeperApplication app) {
        if (app.jobServiceScheduler == null) {
            app.jobServiceScheduler = mock(JobServiceScheduler.class);
        }
        return app.jobServiceScheduler;
    }

    @BuildVersionName
    @Provides
    String provideVersionName(TestCoinKeeperApplication app) {
        return app.version_name;
    }

    @DebugBuild
    @Provides
    boolean provideIsDebug(TestCoinKeeperApplication app) {
        return app.debug;
    }

    @Provides
    Authentication authentication(TestCoinKeeperApplication app) {
        if (app.authentication == null) {
            app.authentication = mock(Authentication.class);
        }
        return app.authentication;
    }

    @Provides
    PinEntry pinEntry(TestCoinKeeperApplication app) {
        if (app.pinEntry == null) {
            app.pinEntry = mock(PinEntry.class);
        }
        return app.pinEntry;
    }

    @Provides
    UserHelper userHelper(TestCoinKeeperApplication app) {
        if (app.userHelper == null) {
            app.userHelper = mock(UserHelper.class);
        }
        return app.userHelper;
    }

    @TimeOutHandler
    @Provides
    Handler timeoutHandler(TestCoinKeeperApplication app) {
        if (app.handler == null) {
            app.handler = mock(Handler.class);
        }
        return app.handler;
    }

    @Provides
    @ThreadHandler
    Handler threadHandler(TestCoinKeeperApplication app) {
        return timeoutHandler(app);
    }

    @Provides
    CNWalletManager cnWalletManager(TestCoinKeeperApplication app) {
        if (app.cnWalletManager == null) {
            app.cnWalletManager = mock(CNWalletManager.class);
        }
        return app.cnWalletManager;
    }

    //TODO DELETE BELOW

    @Provides
    SyncIncomingInvitesRunner syncIncomingInvitesRunner(TestCoinKeeperApplication app) {
        if (app.syncIncomingInvitesRunner == null) {
            app.syncIncomingInvitesRunner = mock(SyncIncomingInvitesRunner.class);
        }
        return app.syncIncomingInvitesRunner;
    }

    @Provides
    FulfillSentInvitesRunner fulfillSentInvitesRunner(TestCoinKeeperApplication app) {
        if (app.fulfillSentInvitesRunner == null) {
            app.fulfillSentInvitesRunner = mock(FulfillSentInvitesRunner.class);
        }
        return app.fulfillSentInvitesRunner;
    }

    @Provides
    ReceivedInvitesStatusRunner receivedInvitesStatusRunner(TestCoinKeeperApplication app) {
        if (app.receivedInvitesStatusRunner == null) {
            app.receivedInvitesStatusRunner = mock(ReceivedInvitesStatusRunner.class);
        }
        return app.receivedInvitesStatusRunner;
    }

    @Provides
    NegativeBalanceRunner negativeBalanceRunner(TestCoinKeeperApplication app) {
        if (app.negativeBalanceRunner == null) {
            app.negativeBalanceRunner = mock(NegativeBalanceRunner.class);
        }
        return app.negativeBalanceRunner;
    }

    @Provides
    FailedBroadcastCleaner failedBroadcastCleaner(TestCoinKeeperApplication app) {
        if (app.failedBroadcastCleaner == null) {
            app.failedBroadcastCleaner = mock(FailedBroadcastCleaner.class);
        }
        return app.failedBroadcastCleaner;
    }

    @Provides
    PermissionsUtil permissionsUtil(TestCoinKeeperApplication app) {
        if (app.permissionsUtil == null) {
            app.permissionsUtil = mock(PermissionsUtil.class);
        }
        return app.permissionsUtil;
    }

    @Provides
    HealthCheckTimerRunner healthCheckTimerRunner(TestCoinKeeperApplication app) {
        if (app.healthCheckTimerRunner == null) {
            app.healthCheckTimerRunner = mock(HealthCheckTimerRunner.class);
        }
        return app.healthCheckTimerRunner;

    }

    @Provides
    CNServiceConnection cnServiceConnection(TestCoinKeeperApplication app) {
        if (app.cnServiceConnection == null) {
            app.cnServiceConnection = mock(CNServiceConnection.class);
        }
        return app.cnServiceConnection;

    }


    @Provides
    TransactionBuilder transactionBuilder(TestCoinKeeperApplication app) {
        if (app.transactionBuilder == null) {
            app.transactionBuilder = mock(TransactionBuilder.class);
        }
        return app.transactionBuilder;
    }

    @NumAddressesToCache
    @Provides
    int provideNumAddressesAhead() {
        return 10;
    }


    @Provides
    AccountManager accountManager(TestCoinKeeperApplication app) {
        if (app.accountManager == null) {
            app.accountManager = mock(AccountManager.class);
        }
        return app.accountManager;
    }


    @Provides
    TypedValue typedValue(TestCoinKeeperApplication app) {
        if (app.typedValue == null) {
            app.typedValue = mock(TypedValue.class);
        }
        return app.typedValue;
    }

    @CountryCodeLocales
    @Provides
    List<CountryCodeLocale> provideCountryCodeLocales(TestCoinKeeperApplication app) {
        if (app.countryCodeLocales == null) {
            app.countryCodeLocales = new ArrayList<>();
        }
        return app.countryCodeLocales;
    }

    @Provides
    TransactionFundingManager transactionFundingManager(TestCoinKeeperApplication app) {
        if (app.transactionFundingManager == null) {
            app.transactionFundingManager = mock(TransactionFundingManager.class);
        }

        return app.transactionFundingManager;
    }

    @CoinkeeperApplicationScope
    @Provides
    Locale locale(@ApplicationContext Context context) {
        return ConfigurationCompat.getLocales(context.getResources().getConfiguration()).get(0);
    }

    @Provides
    CurrencyPreference currencyPreference(TestCoinKeeperApplication app) {
        if (app.currencyPreference == null) {
            app.currencyPreference = mock(CurrencyPreference.class);
        }
        return app.currencyPreference;
    }

    @Provides
    DefaultCurrencies defaultCurrencies(TestCoinKeeperApplication app) {
        if (app.defaultCurrencies == null) {
            app.defaultCurrencies = mock(DefaultCurrencies.class);
        }
        return app.defaultCurrencies;
    }

    @Provides
    LocationManager provideLocationManager() {
        return mock(LocationManager.class);
    }

    @Provides
    ActivityNavigationUtil activityNavigationUtil(TestCoinKeeperApplication app) {
        if (app.activityNavigationUtil == null) {
            app.activityNavigationUtil = mock(ActivityNavigationUtil.class);
        }
        return app.activityNavigationUtil;
    }

    @Provides
    LocationUtil locationUtil(TestCoinKeeperApplication app) {
        if (app.locationUtil == null) {
            app.locationUtil = mock(LocationUtil.class);
        }
        return app.locationUtil;
    }

    @Provides
    Gson gson() {
        return new Gson();
    }

    @Provides
    InviteContactPresenter InviteContactPresenter(TestCoinKeeperApplication app) {
        if (app.inviteContactPresenter == null) {
            app.inviteContactPresenter = mock(InviteContactPresenter.class);
        }
        return app.inviteContactPresenter;
    }

    @Provides
    CNAddressLookupDelegate cnAddressLookupDelegate(TestCoinKeeperApplication app) {
        if (app.cnAddressLookupDelegae == null) {
            app.cnAddressLookupDelegae = mock(CNAddressLookupDelegate.class);
        }
        return app.cnAddressLookupDelegae;
    }

    @Provides
    ClipboardUtil clipboardUtil(TestCoinKeeperApplication app) {
        if (app.clipboardUtil == null) {
            app.clipboardUtil = mock(ClipboardUtil.class);
        }

        return app.clipboardUtil;
    }

    @Provides
    UserPreferences userPreferences(TestCoinKeeperApplication app) {
        if (app.userPreferences == null) {
            app.userPreferences = mock(UserPreferences.class);
        }
        return app.userPreferences;
    }

    @Provides
    VerifyRecoveryWordsPresenter verifyRecoveryWordsPresenter(TestCoinKeeperApplication app) {
        if (app.verifyRecoveryWordsPresenter == null) {
            app.verifyRecoveryWordsPresenter = mock(VerifyRecoveryWordsPresenter.class);
        }
        return app.verifyRecoveryWordsPresenter;
    }

    @Provides
    NotificationUtil notificationUtil(TestCoinKeeperApplication app) {
        if (app.notificationUtil == null) {
            app.notificationUtil = mock(NotificationUtil.class);
        }
        return app.notificationUtil;
    }

    @Provides
    DropbitMeConfiguration dropbitMeConfiguration(TestCoinKeeperApplication app) {
        if (app.dropbitMeConfiguration == null) {
            app.dropbitMeConfiguration = mock(DropbitMeConfiguration.class);
        }
        return app.dropbitMeConfiguration;

    }

    @Provides
    InternalNotificationsInteractor internalNotificationsInteractor(TestCoinKeeperApplication app) {
        if (app.internalNotificationsInteractor == null) {
            app.internalNotificationsInteractor = mock(InternalNotificationsInteractor.class);
        }
        return app.internalNotificationsInteractor;
    }

    @Provides
    ServiceWorkUtil serviceWorkUtil(TestCoinKeeperApplication app) {
        if (app.serviceWorkUtil == null) {
            app.serviceWorkUtil = mock(ServiceWorkUtil.class);
        }
        return app.serviceWorkUtil;
    }


    @Provides
    ActionBarController actionBarController(TestCoinKeeperApplication app) {
        if (app.actionBarController == null) {
            app.actionBarController = mock(ActionBarController.class);
        }
        return app.actionBarController;
    }

    @Provides
    DrawerController drawerController(TestCoinKeeperApplication app) {
        if (app.drawerController == null) {
            app.drawerController = mock(DrawerController.class);
        }
        return app.drawerController;
    }
}
