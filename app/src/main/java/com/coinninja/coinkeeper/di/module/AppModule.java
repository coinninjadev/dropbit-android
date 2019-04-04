package com.coinninja.coinkeeper.di.module;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.job.JobScheduler;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.preference.PreferenceManager;
import android.util.TypedValue;

import com.coinninja.bindings.TransactionBuilder;
import com.coinninja.coinkeeper.BuildConfig;
import com.coinninja.coinkeeper.CoinKeeperApplication;
import com.coinninja.coinkeeper.CoinKeeperLifecycleListener;
import com.coinninja.coinkeeper.cn.wallet.HDWallet;
import com.coinninja.coinkeeper.cn.wallet.SyncWalletManager;
import com.coinninja.coinkeeper.di.interfaces.AnalyticsApiToken;
import com.coinninja.coinkeeper.di.interfaces.ApplicationContext;
import com.coinninja.coinkeeper.di.interfaces.BuildVersionName;
import com.coinninja.coinkeeper.di.interfaces.CoinkeeperApplicationScope;
import com.coinninja.coinkeeper.di.interfaces.CountryCodeLocales;
import com.coinninja.coinkeeper.di.interfaces.DebugBuild;
import com.coinninja.coinkeeper.di.interfaces.NumAddressesToCache;
import com.coinninja.coinkeeper.di.interfaces.ThreadHandler;
import com.coinninja.coinkeeper.di.interfaces.TimeOutHandler;
import com.coinninja.coinkeeper.di.interfaces.UUID;
import com.coinninja.coinkeeper.interactor.AuthenticationImpl;
import com.coinninja.coinkeeper.interactor.PinEntryImpl;
import com.coinninja.coinkeeper.interactor.PinInteractor;
import com.coinninja.coinkeeper.interfaces.Authentication;
import com.coinninja.coinkeeper.interfaces.PinEntry;
import com.coinninja.coinkeeper.model.TransactionNotificationMapper;
import com.coinninja.coinkeeper.model.db.Account;
import com.coinninja.coinkeeper.model.helpers.AddressHelper;
import com.coinninja.coinkeeper.model.helpers.DaoSessionManager;
import com.coinninja.coinkeeper.model.helpers.TransactionHelper;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.model.helpers.WordHelper;
import com.coinninja.coinkeeper.model.query.WalletQueryManager;
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient;
import com.coinninja.coinkeeper.service.runner.SharedMemoRetrievalRunner;
import com.coinninja.coinkeeper.ui.base.AndroidActivityBuilder;
import com.coinninja.coinkeeper.ui.base.AndroidFragmentBuilder;
import com.coinninja.coinkeeper.util.AnalyticUtil;
import com.coinninja.coinkeeper.util.CurrencyPreference;
import com.coinninja.coinkeeper.util.DefaultCurrencies;
import com.coinninja.coinkeeper.util.PhoneNumberUtil;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.android.PreferencesUtil;
import com.coinninja.coinkeeper.util.encryption.MessageEncryptor;
import com.coinninja.coinkeeper.util.uri.BitcoinUriBuilder;
import com.coinninja.coinkeeper.util.uuid.UuidFactory;
import com.coinninja.coinkeeper.view.widget.phonenumber.CountryCodeLocale;
import com.coinninja.coinkeeper.view.widget.phonenumber.CountryCodeLocaleGenerator;
import com.coinninja.messaging.MessageCryptor;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import java.util.List;
import java.util.Locale;

import androidx.core.os.ConfigurationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import dagger.Module;
import dagger.Provides;

@Module(includes = {AndroidActivityBuilder.class, AndroidFragmentBuilder.class})
public class AppModule {

    @Provides
    CoinKeeperApplication coinKeeperApplication(Application application) {
        return (CoinKeeperApplication) application;
    }

    @Provides
    @CoinkeeperApplicationScope
    @ApplicationContext
    Context context(CoinKeeperApplication application) {
        return application;
    }

    @Provides
    @CoinkeeperApplicationScope
    ContentResolver contentResolver(CoinKeeperApplication application) {
        return application.getContentResolver();
    }

    @Provides
    @CoinkeeperApplicationScope
    BitcoinUriBuilder bitcoinUrlBuilder() {
        return new BitcoinUriBuilder();
    }

    @Provides
    @CoinkeeperApplicationScope
    Analytics analytics(MixpanelAPI analyticsProvider) {
        return new AnalyticUtil(analyticsProvider).start();
    }

    @Provides
    com.google.i18n.phonenumbers.PhoneNumberUtil provideI18PhoneNumberUtil() {
        return com.google.i18n.phonenumbers.PhoneNumberUtil.getInstance();
    }

    @Provides
    TransactionNotificationMapper transactionNotificationMapper(PhoneNumberUtil phoneNumberUtil) {
        return new TransactionNotificationMapper(phoneNumberUtil);
    }

    @Provides
    SharedMemoRetrievalRunner sharedMemoRetrievalRunner(Analytics analytics,
                                                        TransactionHelper transactionHelper,
                                                        SignedCoinKeeperApiClient signedCoinKeeperApiClient,
                                                        MessageEncryptor messageEncryptor,
                                                        TransactionNotificationMapper transactionNotificationMapper,
                                                        DaoSessionManager daoSessionManager, WalletHelper walletHelper) {
        return new SharedMemoRetrievalRunner(analytics, transactionHelper,
                signedCoinKeeperApiClient, messageEncryptor, transactionNotificationMapper,
                daoSessionManager, walletHelper);
    }

    @Provides
    MixpanelAPI mixpanelAPI(@ApplicationContext Context context, @AnalyticsApiToken String token) {
        return MixpanelAPI.getInstance(context, token);
    }

    @Provides
    PinEntry pinEntry(PinInteractor pinInteractor) {
        return new PinEntryImpl(pinInteractor);
    }

    @Provides
    MessageEncryptor messageEncryptor(HDWallet hdWallet, AddressHelper addressHelper, MessageCryptor messageCryptor) {
        return new MessageEncryptor(hdWallet, addressHelper, messageCryptor);
    }

    @Provides
    MessageCryptor messageCryptor() {
        return new MessageCryptor();
    }

    @Provides
    @AnalyticsApiToken
    String analyticsApiToken() {
        return BuildConfig.ANALYTICS_TOKEN;
    }

    @Provides
    @UUID
    @SuppressLint("HardwareIds")
    String uuid(UuidFactory uuidFactory) {
        return uuidFactory.provideUuid();
    }

    @Provides
    ClipboardManager clipboardManager(@ApplicationContext Context context) {
        return (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
    }

    @Provides
    LocalBroadcastManager localBroadcastManager(@ApplicationContext Context context) {
        return LocalBroadcastManager.getInstance(context);
    }

    @Provides
    @CoinkeeperApplicationScope
    WalletHelper walletHelper(DaoSessionManager daoSessionManager, WordHelper wordHelper, WalletQueryManager walletQueryManager) {
        return new WalletHelper(daoSessionManager, walletQueryManager, wordHelper);
    }

    @Provides
    Account account(WalletHelper walletHelper) {
        return walletHelper.getUserAccount();
    }

    @Provides
    SharedPreferences sharedPreferences(@ApplicationContext Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    @CoinkeeperApplicationScope
    @Provides
    Authentication authentication(@ApplicationContext Context context, PreferencesUtil preferencesUtil, SyncWalletManager syncWalletManager, @TimeOutHandler Handler handler, CoinKeeperLifecycleListener coinKeeperLifecycleListener) {
        return new AuthenticationImpl(context, preferencesUtil, handler, syncWalletManager, coinKeeperLifecycleListener);
    }

    @BuildVersionName
    @Provides
    String provideBuildVersionName() {
        return BuildConfig.VERSION_NAME;
    }

    @NumAddressesToCache
    @Provides
    int providesNumAddressesAhead() {
        return BuildConfig.NUM_ADDRESSES_AHEAD;
    }

    @DebugBuild
    @Provides
    boolean providesIsDebug() {
        return BuildConfig.DEBUG;
    }

    @Provides
    JobScheduler jobScheduler(@ApplicationContext Context context) {
        return (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
    }

    @Provides
    PhoneNumberUtil cnPhoneNumberUtil() {
        return new PhoneNumberUtil();
    }

    @Provides
    @TimeOutHandler
    Handler timeoutHandler() {
        HandlerThread timeout_handler = new HandlerThread("timeout handler");
        timeout_handler.start();
        return new Handler(timeout_handler.getLooper());
    }

    @Provides
    @ThreadHandler
    Handler threadHandler() {
        HandlerThread timeout_handler = new HandlerThread(java.util.UUID.randomUUID().toString());
        timeout_handler.start();
        return new Handler(timeout_handler.getLooper());
    }

    @Provides
    TransactionBuilder transactionBuilder(WalletHelper walletHelper) {
        return new TransactionBuilder(walletHelper.getSeedWords());
    }

    @Provides
    TypedValue typedValue() {
        return new TypedValue();
    }

    @CoinkeeperApplicationScope
    @CountryCodeLocales
    @Provides
    List<CountryCodeLocale> provideCountryCodeLocales(CountryCodeLocaleGenerator countryCodeLocaleGenerator) {
        return countryCodeLocaleGenerator.generate();
    }

    @CoinkeeperApplicationScope
    @Provides
    Locale locale(@ApplicationContext Context context) {
        return ConfigurationCompat.getLocales(context.getResources().getConfiguration()).get(0);
    }

    @Provides
    DefaultCurrencies provideDefaultCurrency(CurrencyPreference currencyPreference) {
        return currencyPreference.getCurrenciesPreference();
    }

}
