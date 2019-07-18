package com.coinninja.coinkeeper.di.module

import android.annotation.SuppressLint
import android.app.Application
import android.app.job.JobScheduler
import android.content.ClipboardManager
import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import android.location.LocationManager
import android.net.Uri
import android.os.Handler
import android.os.HandlerThread
import android.preference.PreferenceManager
import android.util.TypedValue
import androidx.core.os.ConfigurationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import app.dropbit.commons.util.CoroutineContextProvider
import app.dropbit.twitter.Twitter
import app.dropbit.twitter.TwitterProvider
import com.coinninja.bindings.SeedWordGenerator
import com.coinninja.bindings.TransactionBuilder
import com.coinninja.coinkeeper.BuildConfig
import com.coinninja.coinkeeper.CoinKeeperApplication
import com.coinninja.coinkeeper.CoinKeeperLifecycleListener
import com.coinninja.coinkeeper.cn.transaction.notification.TransactionNotificationMapper
import com.coinninja.coinkeeper.cn.wallet.HDWallet
import com.coinninja.coinkeeper.cn.wallet.SyncWalletManager
import com.coinninja.coinkeeper.di.component.AppComponent
import com.coinninja.coinkeeper.di.component.CoinKeeperComponent
import com.coinninja.coinkeeper.di.interfaces.*
import com.coinninja.coinkeeper.di.interfaces.UUID
import com.coinninja.coinkeeper.factory.DropBitMeUriProvider
import com.coinninja.coinkeeper.interactor.AuthenticationImpl
import com.coinninja.coinkeeper.interactor.PinEntryImpl
import com.coinninja.coinkeeper.interactor.PinInteractor
import com.coinninja.coinkeeper.interfaces.Authentication
import com.coinninja.coinkeeper.interfaces.PinEntry
import com.coinninja.coinkeeper.model.db.Account
import com.coinninja.coinkeeper.model.helpers.*
import com.coinninja.coinkeeper.model.query.WalletQueryManager
import com.coinninja.coinkeeper.service.WalletCreationIntentService
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient
import com.coinninja.coinkeeper.service.runner.SharedMemoRetrievalRunner
import com.coinninja.coinkeeper.util.*
import com.coinninja.coinkeeper.util.analytics.Analytics
import com.coinninja.coinkeeper.util.android.PreferencesUtil
import com.coinninja.coinkeeper.util.encryption.MessageEncryptor
import com.coinninja.coinkeeper.util.uri.BitcoinUriBuilder
import com.coinninja.coinkeeper.util.uuid.UuidFactory
import com.coinninja.coinkeeper.view.widget.phonenumber.CountryCodeLocale
import com.coinninja.coinkeeper.view.widget.phonenumber.CountryCodeLocaleGenerator
import com.coinninja.messaging.MessageCryptor
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.Gson
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.squareup.picasso.Picasso
import dagger.Module
import dagger.Provides
import java.util.*
import javax.inject.Named

@Module
class AppModule {

    internal val isProduction: Boolean
        @IsProduction
        @Provides
        get() = BuildConfig.IS_PRODUCTION

    @Provides
    @CoinkeeperApplicationScope
    internal fun seedWordGenerator(): SeedWordGenerator {
        return SeedWordGenerator()
    }

    @Provides
    internal fun coroutineContextProvider(): CoroutineContextProvider {
        return CoroutineContextProvider()
    }

    @Provides
    internal fun picasso(): Picasso {
        return Picasso.get()
    }

    @Provides
    internal fun firebaseInstanceId(): FirebaseInstanceId {
        return FirebaseInstanceId.getInstance()
    }

    @Provides
    internal fun errorLoggingUtil(): ErrorLoggingUtil {
        return ErrorLoggingUtil()
    }

    @Provides
    internal fun analyticUtil(analyticsProvider: MixpanelAPI): Analytics {
        return AnalyticUtil(analyticsProvider).start()
    }

    @Provides
    internal fun walletCreationIntentService(): WalletCreationIntentService {
        return WalletCreationIntentService()
    }

    @Provides
    internal fun coinKeeperApplication(application: Application): CoinKeeperApplication {
        return application as CoinKeeperApplication
    }

    @Provides
    internal fun coinKeeperComponent(appComponent: AppComponent): CoinKeeperComponent {
        return appComponent
    }

    @Provides
    @CoinkeeperApplicationScope
    @ApplicationContext
    internal fun context(application: CoinKeeperApplication): Context {
        return application
    }

    @Provides
    @CoinkeeperApplicationScope
    internal fun contentResolver(application: CoinKeeperApplication): ContentResolver {
        return application.contentResolver
    }

    @Provides
    @CoinkeeperApplicationScope
    internal fun bitcoinUrlBuilder(): BitcoinUriBuilder {
        return BitcoinUriBuilder()
    }

    @Provides
    internal fun provideI18PhoneNumberUtil(): com.google.i18n.phonenumbers.PhoneNumberUtil {
        return com.google.i18n.phonenumbers.PhoneNumberUtil.getInstance()
    }

    @Provides
    internal fun sharedMemoRetrievalRunner(transactionHelper: TransactionHelper,
                                           signedCoinKeeperApiClient: SignedCoinKeeperApiClient,
                                           messageEncryptor: MessageEncryptor,
                                           transactionNotificationMapper: TransactionNotificationMapper,
                                           daoSessionManager: DaoSessionManager, dropbitAccountHelper: DropbitAccountHelper): SharedMemoRetrievalRunner {
        return SharedMemoRetrievalRunner(transactionHelper,
                signedCoinKeeperApiClient, messageEncryptor, transactionNotificationMapper,
                daoSessionManager, dropbitAccountHelper)
    }

    @Provides
    internal fun mixpanelAPI(@ApplicationContext context: Context, @AnalyticsApiToken token: String): MixpanelAPI {
        return MixpanelAPI.getInstance(context, token)
    }

    @Provides
    internal fun pinEntry(pinInteractor: PinInteractor): PinEntry {
        return PinEntryImpl(pinInteractor)
    }

    @Provides
    internal fun messageEncryptor(hdWallet: HDWallet, addressHelper: AddressHelper, messageCryptor: MessageCryptor): MessageEncryptor {
        return MessageEncryptor(hdWallet, addressHelper, messageCryptor)
    }

    @Provides
    internal fun messageCryptor(): MessageCryptor {
        return MessageCryptor()
    }

    @Provides
    @AnalyticsApiToken
    internal fun analyticsApiToken(): String {
        return BuildConfig.ANALYTICS_TOKEN
    }

    @Provides
    @UUID
    @SuppressLint("HardwareIds")
    internal fun uuid(uuidFactory: UuidFactory): String {
        return uuidFactory.provideUuid()
    }

    @Provides
    internal fun clipboardManager(@ApplicationContext context: Context): ClipboardManager {
        return context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    @Provides
    internal fun localBroadcastManager(@ApplicationContext context: Context): LocalBroadcastManager {
        return LocalBroadcastManager.getInstance(context)
    }

    @Provides
    @CoinkeeperApplicationScope
    internal fun walletHelper(daoSessionManager: DaoSessionManager, wordHelper: WordHelper, walletQueryManager: WalletQueryManager, feesManager: FeesManager): WalletHelper {
        return WalletHelper(daoSessionManager, walletQueryManager, wordHelper, feesManager)
    }

    @Provides
    internal fun account(walletHelper: WalletHelper): Account {
        return walletHelper.userAccount
    }

    @Provides
    internal fun sharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }

    @CoinkeeperApplicationScope
    @Provides
    internal fun authentication(@ApplicationContext context: Context, preferencesUtil: PreferencesUtil, syncWalletManager: SyncWalletManager, @TimeOutHandler handler: Handler, coinKeeperLifecycleListener: CoinKeeperLifecycleListener): Authentication {
        return AuthenticationImpl(context, preferencesUtil, handler, syncWalletManager, coinKeeperLifecycleListener)
    }

    @BuildVersionName
    @Provides
    internal fun provideBuildVersionName(): String {
        return BuildConfig.VERSION_NAME
    }

    @NumAddressesToCache
    @Provides
    internal fun providesNumAddressesAhead(): Int {
        return BuildConfig.NUM_ADDRESSES_AHEAD
    }

    @DebugBuild
    @Provides
    internal fun providesIsDebug(): Boolean {
        return BuildConfig.DEBUG
    }

    @Provides
    internal fun jobScheduler(@ApplicationContext context: Context): JobScheduler {
        return context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
    }

    @Provides
    internal fun cnPhoneNumberUtil(): PhoneNumberUtil {
        return PhoneNumberUtil()
    }

    @Provides
    @TimeOutHandler
    internal fun timeoutHandler(): Handler {
        val timeout_handler = HandlerThread("timeout handler")
        timeout_handler.start()
        return Handler(timeout_handler.looper)
    }

    @Provides
    @ThreadHandler
    internal fun threadHandler(): Handler {
        val timeout_handler = HandlerThread(java.util.UUID.randomUUID().toString())
        timeout_handler.start()
        return Handler(timeout_handler.looper)
    }

    @Provides
    internal fun transactionBuilder(walletHelper: WalletHelper): TransactionBuilder {
        return TransactionBuilder(walletHelper.seedWords)
    }

    @Provides
    internal fun typedValue(): TypedValue {
        return TypedValue()
    }

    @CoinkeeperApplicationScope
    @Provides
    @CountryCodeLocales
    internal fun provideCountryCodeLocales(countryCodeLocaleGenerator: CountryCodeLocaleGenerator): List<CountryCodeLocale> {
        return countryCodeLocaleGenerator.generate()
    }

    @CoinkeeperApplicationScope
    @Provides
    @Named("countryCodeLocales")
    internal fun provideCountryCodeLocalesNamed(countryCodeLocaleGenerator: CountryCodeLocaleGenerator): List<CountryCodeLocale> {
        return countryCodeLocaleGenerator.generate()
    }

    @CoinkeeperApplicationScope
    @Provides
    internal fun locale(@ApplicationContext context: Context): Locale {
        return ConfigurationCompat.getLocales(context.resources.configuration).get(0)
    }

    @Provides
    internal fun provideDefaultCurrency(currencyPreference: CurrencyPreference): DefaultCurrencies {
        return currencyPreference.currenciesPreference
    }

    @Provides
    internal fun provideLocationManager(@ApplicationContext context: Context): LocationManager {
        return context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    @Provides
    internal fun gson(): Gson {
        return Gson()
    }

    @DropbitMeUri
    @Provides
    internal fun dropbitMeUri(dropBitMeUriProvider: DropBitMeUriProvider): Uri {
        return dropBitMeUriProvider.provideUri()
    }

    @Provides
    internal fun twitter(@ApplicationContext context: Context): Twitter {
        return TwitterProvider.provide(context)
    }
}
