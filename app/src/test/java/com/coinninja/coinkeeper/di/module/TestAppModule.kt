package com.coinninja.coinkeeper.di.module

import android.annotation.SuppressLint
import android.app.Application
import android.content.ClipboardManager
import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import android.location.LocationManager
import android.os.Handler
import android.util.TypedValue

import androidx.core.os.ConfigurationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager

import com.coinninja.bindings.TransactionBuilder
import com.coinninja.coinkeeper.CoinKeeperApplication
import com.coinninja.coinkeeper.CoinKeeperLifecycleListener
import com.coinninja.coinkeeper.TestCoinKeeperApplication
import com.coinninja.coinkeeper.cn.account.AccountManager
import com.coinninja.coinkeeper.cn.service.YearlyHighViewModel
import com.coinninja.coinkeeper.cn.wallet.CNWalletManager
import com.coinninja.coinkeeper.cn.wallet.DataSigner
import com.coinninja.coinkeeper.cn.wallet.HDWallet
import com.coinninja.coinkeeper.cn.wallet.SyncWalletManager
import com.coinninja.coinkeeper.cn.wallet.dust.DustProtectionPreference
import com.coinninja.coinkeeper.cn.wallet.service.CNAddressLookupDelegate
import com.coinninja.coinkeeper.cn.wallet.service.CNServiceConnection
import com.coinninja.coinkeeper.cn.wallet.tx.TransactionFundingManager
import com.coinninja.coinkeeper.di.component.CoinKeeperComponent
import com.coinninja.coinkeeper.di.interfaces.ApplicationContext
import com.coinninja.coinkeeper.di.interfaces.BuildVersionName
import com.coinninja.coinkeeper.di.interfaces.CoinkeeperApplicationScope
import com.coinninja.coinkeeper.di.interfaces.CountryCodeLocales
import com.coinninja.coinkeeper.di.interfaces.DebugBuild
import com.coinninja.coinkeeper.di.interfaces.NumAddressesToCache
import com.coinninja.coinkeeper.di.interfaces.ThreadHandler
import com.coinninja.coinkeeper.di.interfaces.TimeOutHandler
import com.coinninja.coinkeeper.di.interfaces.UUID
import com.coinninja.coinkeeper.interactor.InternalNotificationsInteractor
import com.coinninja.coinkeeper.interactor.UserPreferences
import com.coinninja.coinkeeper.interfaces.Authentication
import com.coinninja.coinkeeper.interfaces.PinEntry
import com.coinninja.coinkeeper.model.db.Account
import com.coinninja.coinkeeper.model.helpers.DropbitAccountHelper
import com.coinninja.coinkeeper.model.helpers.UserHelper
import com.coinninja.coinkeeper.model.helpers.WalletHelper
import com.coinninja.coinkeeper.presenter.activity.InviteContactPresenter
import com.coinninja.coinkeeper.presenter.fragment.VerifyRecoveryWordsPresenter
import com.coinninja.coinkeeper.service.ContactLookupService
import com.coinninja.coinkeeper.service.client.BlockstreamClient
import com.coinninja.coinkeeper.service.runner.FailedBroadcastCleaner
import com.coinninja.coinkeeper.service.runner.FulfillSentInvitesRunner
import com.coinninja.coinkeeper.service.runner.HealthCheckTimerRunner
import com.coinninja.coinkeeper.service.runner.NegativeBalanceRunner
import com.coinninja.coinkeeper.service.runner.ReceivedInvitesStatusRunner
import com.coinninja.coinkeeper.service.runner.SyncIncomingInvitesRunner
import com.coinninja.coinkeeper.ui.account.verify.twitter.TwitterVerificationController
import com.coinninja.coinkeeper.ui.actionbar.ActionBarController
import com.coinninja.coinkeeper.ui.actionbar.managers.DrawerController
import com.coinninja.coinkeeper.ui.dropbit.me.DropbitMeConfiguration
import com.coinninja.coinkeeper.ui.settings.DeleteWalletPresenter
import com.coinninja.coinkeeper.ui.transaction.DefaultCurrencyChangeViewNotifier
import com.coinninja.coinkeeper.ui.transaction.SyncManagerViewNotifier
import com.coinninja.coinkeeper.ui.transaction.history.TransactionHistoryDataAdapter
import com.coinninja.coinkeeper.util.AnalyticUtil
import com.coinninja.coinkeeper.util.CoinNinjaContactResolver
import com.coinninja.coinkeeper.util.CurrencyPreference
import com.coinninja.coinkeeper.util.DefaultCurrencies
import com.coinninja.coinkeeper.util.ErrorLoggingUtil
import com.coinninja.coinkeeper.util.Hasher
import com.coinninja.coinkeeper.util.NotificationUtil
import com.coinninja.coinkeeper.util.PhoneNumberUtil
import com.coinninja.coinkeeper.util.RemoteAddressLocalCache
import com.coinninja.coinkeeper.util.analytics.Analytics
import com.coinninja.coinkeeper.util.android.ClipboardUtil
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil
import com.coinninja.coinkeeper.util.android.LocationUtil
import com.coinninja.coinkeeper.util.android.PermissionsUtil
import com.coinninja.coinkeeper.util.android.PreferencesUtil
import com.coinninja.coinkeeper.util.android.ServiceWorkUtil
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil
import com.coinninja.coinkeeper.util.android.app.JobIntentService.JobServiceScheduler
import com.coinninja.coinkeeper.util.crypto.BitcoinUtil
import com.coinninja.coinkeeper.util.uri.BitcoinUriBuilder
import com.coinninja.coinkeeper.view.widget.phonenumber.CountryCodeLocale
import com.coinninja.coinkeeper.view.widget.phonenumber.CountryCodeLocaleGenerator
import com.coinninja.messaging.MessageCryptor
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.Gson
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.squareup.picasso.Picasso

import java.util.ArrayList
import java.util.Locale

import app.dropbit.commons.util.CoroutineContextProvider
import app.dropbit.commons.util.TestCoroutineContextProvider
import app.dropbit.twitter.Twitter
import dagger.Module
import dagger.Provides

import org.mockito.Mockito.mock

@Module
class TestAppModule {

    @Provides
    internal fun coroutineContextProvider(): CoroutineContextProvider {
        return TestCoroutineContextProvider()
    }

    @Provides
    internal fun firebaseInstanceId(): FirebaseInstanceId {
        return mock(FirebaseInstanceId::class.java)
    }

    @Provides
    internal fun coinNinjaContentResolver(permissionsUtil: PermissionsUtil, resolver: ContentResolver): CoinNinjaContactResolver {
        return CoinNinjaContactResolver(permissionsUtil, resolver)
    }

    @Provides
    internal fun picasso(): Picasso {
        return mock(Picasso::class.java)
    }


    @Provides
    internal fun analyticUtil(analyticsProvider: MixpanelAPI): AnalyticUtil {
        return AnalyticUtil(analyticsProvider)
    }

    @Provides
    internal fun errorLoggingUtil(): ErrorLoggingUtil {
        return ErrorLoggingUtil()
    }

    @Provides
    internal fun blockstreamClient(): BlockstreamClient {
        return BlockstreamClient.newInstance()
    }

    @Provides
    internal fun coinKeeperApplication(application: Application): CoinKeeperApplication {
        return application as CoinKeeperApplication
    }

    @Provides
    internal fun testCoinKeeperApplication(application: Application): TestCoinKeeperApplication {
        return application as TestCoinKeeperApplication
    }

    @Provides
    @CoinkeeperApplicationScope
    internal fun bitcoinUrlBuilder(): BitcoinUriBuilder {
        return BitcoinUriBuilder()
    }


    @Provides
    internal fun coinKeeperComponent(): CoinKeeperComponent {
        return mock(CoinKeeperComponent::class.java)
    }

    @Provides
    @CoinkeeperApplicationScope
    internal fun coinKeeperLifecycleListener(app: TestCoinKeeperApplication): CoinKeeperLifecycleListener {
        if (app.coinKeeperLifecycleListener == null)
            app.coinKeeperLifecycleListener = mock(CoinKeeperLifecycleListener::class.java)
        return app.coinKeeperLifecycleListener
    }

    @Provides
    internal fun provideI18PhoneNumberUtil(): com.google.i18n.phonenumbers.PhoneNumberUtil {
        return com.google.i18n.phonenumbers.PhoneNumberUtil.getInstance()
    }

    @Provides
    @CoinkeeperApplicationScope
    @ApplicationContext
    internal fun context(application: CoinKeeperApplication): Context {
        return application
    }

    @Provides
    @CoinkeeperApplicationScope
    internal fun contentResolver(application: TestCoinKeeperApplication): ContentResolver {
        return application.contentResolver
    }

    @Provides
    @CoinkeeperApplicationScope
    internal fun contactLookupService(): ContactLookupService {
        return ContactLookupService()
    }

    @Provides
    @CoinkeeperApplicationScope
    internal fun analytics(app: TestCoinKeeperApplication): Analytics {
        if (app.analytics == null)
            app.analytics = mock(Analytics::class.java)
        return app.analytics
    }

    @Provides
    internal fun messageCryptor(app: TestCoinKeeperApplication): MessageCryptor {
        if (app.messageCryptor == null)
            app.messageCryptor = mock(MessageCryptor::class.java)
        return app.messageCryptor
    }

    @Provides
    internal fun phoneNumberUtil(app: TestCoinKeeperApplication): PhoneNumberUtil {
        if (app.phoneNumberUtil == null)
            app.phoneNumberUtil = mock(PhoneNumberUtil::class.java)
        return app.phoneNumberUtil
    }

    @Provides
    @UUID
    @SuppressLint("HardwareIds")
    internal fun uuid(app: TestCoinKeeperApplication): String {
        return app.UUID
    }

    @Provides
    internal fun localBroadcastManager(@ApplicationContext context: Context): LocalBroadcastManager {
        // Robolectric will dispatch real broadcasts
        return LocalBroadcastManager.getInstance(context)
    }

    @Provides
    internal fun localBroadCastUtil(app: TestCoinKeeperApplication): LocalBroadCastUtil {
        if (app.localBroadCastUtil == null)
            app.localBroadCastUtil = mock(LocalBroadCastUtil::class.java)
        return app.localBroadCastUtil
    }

    @Provides
    internal fun clipboardManager(app: TestCoinKeeperApplication): ClipboardManager {
        if (app.clipboardManager == null)
            app.clipboardManager = mock(ClipboardManager::class.java)
        return app.clipboardManager
    }

    @Provides
    internal fun hdWallet(app: TestCoinKeeperApplication): HDWallet {
        if (app.hdWallet == null)
            app.hdWallet = mock(HDWallet::class.java)
        return app.hdWallet
    }

    @Provides
    internal fun bitcoinUtil(app: TestCoinKeeperApplication): BitcoinUtil {
        if (app.bitcoinUtil == null)
            app.bitcoinUtil = mock(BitcoinUtil::class.java)
        return app.bitcoinUtil
    }

    @Provides
    internal fun syncWalletManager(app: TestCoinKeeperApplication): SyncWalletManager {
        if (app.syncWalletManager == null) {
            app.syncWalletManager = mock(SyncWalletManager::class.java)
        }
        return app.syncWalletManager
    }

    @Provides
    internal fun sharedPreferences(app: TestCoinKeeperApplication): SharedPreferences {
        if (app.sharedPreferences == null) {
            app.sharedPreferences = mock(SharedPreferences::class.java)
        }
        return app.sharedPreferences
    }

    @Provides
    internal fun walletHelper(app: TestCoinKeeperApplication): WalletHelper {
        if (app.walletHelper == null) {
            app.walletHelper = mock(WalletHelper::class.java)
        }
        return app.walletHelper
    }

    @Provides
    internal fun account(app: TestCoinKeeperApplication): Account {
        if (app.account == null) {
            app.account = mock(Account::class.java)
        }
        return app.account
    }

    @Provides
    internal fun dataSigner(app: TestCoinKeeperApplication): DataSigner {
        if (app.dataSigner == null) {
            app.dataSigner = mock(DataSigner::class.java)
        }
        return app.dataSigner
    }

    @Provides
    internal fun jobServiceScheduler(app: TestCoinKeeperApplication): JobServiceScheduler {
        if (app.jobServiceScheduler == null) {
            app.jobServiceScheduler = mock(JobServiceScheduler::class.java)
        }
        return app.jobServiceScheduler
    }

    @BuildVersionName
    @Provides
    internal fun provideVersionName(app: TestCoinKeeperApplication): String {
        return app.version_name
    }

    @DebugBuild
    @Provides
    internal fun provideIsDebug(app: TestCoinKeeperApplication): Boolean {
        return app.debug
    }

    @Provides
    internal fun authentication(app: TestCoinKeeperApplication): Authentication {
        if (app.authentication == null) {
            app.authentication = mock(Authentication::class.java)
        }
        return app.authentication
    }

    @Provides
    internal fun pinEntry(app: TestCoinKeeperApplication): PinEntry {
        if (app.pinEntry == null) {
            app.pinEntry = mock(PinEntry::class.java)
        }
        return app.pinEntry
    }

    @Provides
    internal fun userHelper(app: TestCoinKeeperApplication): UserHelper {
        if (app.userHelper == null) {
            app.userHelper = mock(UserHelper::class.java)
        }
        return app.userHelper
    }

    @TimeOutHandler
    @Provides
    internal fun timeoutHandler(app: TestCoinKeeperApplication): Handler {
        if (app.handler == null) {
            app.handler = mock(Handler::class.java)
        }
        return app.handler
    }

    @Provides
    @ThreadHandler
    internal fun threadHandler(app: TestCoinKeeperApplication): Handler {
        return timeoutHandler(app)
    }

    @Provides
    internal fun cnWalletManager(app: TestCoinKeeperApplication): CNWalletManager {
        if (app.cnWalletManager == null) {
            app.cnWalletManager = mock(CNWalletManager::class.java)
        }
        return app.cnWalletManager
    }

    @Provides
    internal fun syncIncomingInvitesRunner(app: TestCoinKeeperApplication): SyncIncomingInvitesRunner {
        if (app.syncIncomingInvitesRunner == null) {
            app.syncIncomingInvitesRunner = mock(SyncIncomingInvitesRunner::class.java)
        }
        return app.syncIncomingInvitesRunner
    }

    @Provides
    internal fun fulfillSentInvitesRunner(app: TestCoinKeeperApplication): FulfillSentInvitesRunner {
        if (app.fulfillSentInvitesRunner == null) {
            app.fulfillSentInvitesRunner = mock(FulfillSentInvitesRunner::class.java)
        }
        return app.fulfillSentInvitesRunner
    }

    @Provides
    internal fun receivedInvitesStatusRunner(app: TestCoinKeeperApplication): ReceivedInvitesStatusRunner {
        if (app.receivedInvitesStatusRunner == null) {
            app.receivedInvitesStatusRunner = mock(ReceivedInvitesStatusRunner::class.java)
        }
        return app.receivedInvitesStatusRunner
    }

    @Provides
    internal fun negativeBalanceRunner(app: TestCoinKeeperApplication): NegativeBalanceRunner {
        if (app.negativeBalanceRunner == null) {
            app.negativeBalanceRunner = mock(NegativeBalanceRunner::class.java)
        }
        return app.negativeBalanceRunner
    }

    @Provides
    internal fun failedBroadcastCleaner(app: TestCoinKeeperApplication): FailedBroadcastCleaner {
        if (app.failedBroadcastCleaner == null) {
            app.failedBroadcastCleaner = mock(FailedBroadcastCleaner::class.java)
        }
        return app.failedBroadcastCleaner
    }

    @Provides
    internal fun permissionsUtil(app: TestCoinKeeperApplication): PermissionsUtil {
        if (app.permissionsUtil == null) {
            app.permissionsUtil = mock(PermissionsUtil::class.java)
        }
        return app.permissionsUtil
    }

    @Provides
    internal fun healthCheckTimerRunner(app: TestCoinKeeperApplication): HealthCheckTimerRunner {
        if (app.healthCheckTimerRunner == null) {
            app.healthCheckTimerRunner = mock(HealthCheckTimerRunner::class.java)
        }
        return app.healthCheckTimerRunner

    }

    @Provides
    internal fun cnServiceConnection(app: TestCoinKeeperApplication): CNServiceConnection {
        if (app.cnServiceConnection == null) {
            app.cnServiceConnection = mock(CNServiceConnection::class.java)
        }
        return app.cnServiceConnection

    }


    @Provides
    internal fun transactionBuilder(app: TestCoinKeeperApplication): TransactionBuilder {
        if (app.transactionBuilder == null) {
            app.transactionBuilder = mock(TransactionBuilder::class.java)
        }
        return app.transactionBuilder
    }

    @NumAddressesToCache
    @Provides
    internal fun provideNumAddressesAhead(): Int {
        return 10
    }


    @Provides
    internal fun accountManager(app: TestCoinKeeperApplication): AccountManager {
        if (app.accountManager == null) {
            app.accountManager = mock(AccountManager::class.java)
        }
        return app.accountManager
    }


    @Provides
    internal fun typedValue(app: TestCoinKeeperApplication): TypedValue {
        if (app.typedValue == null) {
            app.typedValue = mock(TypedValue::class.java)
        }
        return app.typedValue
    }

    @CountryCodeLocales
    @Provides
    internal fun provideCountryCodeLocales(app: TestCoinKeeperApplication): List<CountryCodeLocale> {
        if (app.countryCodeLocales == null) {
            app.countryCodeLocales = ArrayList()
        }
        return app.countryCodeLocales!!
    }

    @Provides
    internal fun transactionFundingManager(app: TestCoinKeeperApplication): TransactionFundingManager {
        if (app.transactionFundingManager == null) {
            app.transactionFundingManager = mock(TransactionFundingManager::class.java)
        }

        return app.transactionFundingManager
    }

    @CoinkeeperApplicationScope
    @Provides
    internal fun locale(@ApplicationContext context: Context): Locale {
        return ConfigurationCompat.getLocales(context.resources.configuration).get(0)
    }

    @Provides
    internal fun currencyPreference(app: TestCoinKeeperApplication): CurrencyPreference {
        if (app.currencyPreference == null) {
            app.currencyPreference = mock(CurrencyPreference::class.java)
        }
        return app.currencyPreference
    }

    @Provides
    internal fun defaultCurrencies(app: TestCoinKeeperApplication): DefaultCurrencies {
        if (app.defaultCurrencies == null) {
            app.defaultCurrencies = mock(DefaultCurrencies::class.java)
        }
        return app.defaultCurrencies
    }

    @Provides
    internal fun provideLocationManager(): LocationManager {
        return mock(LocationManager::class.java)
    }

    @Provides
    internal fun activityNavigationUtil(app: TestCoinKeeperApplication): ActivityNavigationUtil {
        if (app.activityNavigationUtil == null) {
            app.activityNavigationUtil = mock(ActivityNavigationUtil::class.java)
        }
        return app.activityNavigationUtil
    }

    @Provides
    internal fun locationUtil(app: TestCoinKeeperApplication): LocationUtil {
        if (app.locationUtil == null) {
            app.locationUtil = mock(LocationUtil::class.java)
        }
        return app.locationUtil
    }

    @Provides
    internal fun gson(): Gson {
        return Gson()
    }

    @Provides
    internal fun InviteContactPresenter(app: TestCoinKeeperApplication): InviteContactPresenter {
        if (app.inviteContactPresenter == null) {
            app.inviteContactPresenter = mock(InviteContactPresenter::class.java)
        }
        return app.inviteContactPresenter
    }

    @Provides
    internal fun cnAddressLookupDelegate(app: TestCoinKeeperApplication): CNAddressLookupDelegate {
        if (app.cnAddressLookupDelegae == null) {
            app.cnAddressLookupDelegae = mock(CNAddressLookupDelegate::class.java)
        }
        return app.cnAddressLookupDelegae
    }

    @Provides
    internal fun clipboardUtil(app: TestCoinKeeperApplication): ClipboardUtil {
        if (app.clipboardUtil == null) {
            app.clipboardUtil = mock(ClipboardUtil::class.java)
        }

        return app.clipboardUtil
    }

    @Provides
    internal fun userPreferences(app: TestCoinKeeperApplication): UserPreferences {
        if (app.userPreferences == null) {
            app.userPreferences = mock(UserPreferences::class.java)
        }
        return app.userPreferences
    }

    @Provides
    internal fun verifyRecoveryWordsPresenter(app: TestCoinKeeperApplication): VerifyRecoveryWordsPresenter {
        if (app.verifyRecoveryWordsPresenter == null) {
            app.verifyRecoveryWordsPresenter = mock(VerifyRecoveryWordsPresenter::class.java)
        }
        return app.verifyRecoveryWordsPresenter
    }

    @Provides
    internal fun notificationUtil(app: TestCoinKeeperApplication): NotificationUtil {
        if (app.notificationUtil == null) {
            app.notificationUtil = mock(NotificationUtil::class.java)
        }
        return app.notificationUtil
    }

    @Provides
    internal fun dropbitMeConfiguration(app: TestCoinKeeperApplication): DropbitMeConfiguration {
        if (app.dropbitMeConfiguration == null) {
            app.dropbitMeConfiguration = mock(DropbitMeConfiguration::class.java)
        }
        return app.dropbitMeConfiguration

    }

    @Provides
    internal fun internalNotificationsInteractor(app: TestCoinKeeperApplication): InternalNotificationsInteractor {
        if (app.internalNotificationsInteractor == null) {
            app.internalNotificationsInteractor = mock(InternalNotificationsInteractor::class.java)
        }
        return app.internalNotificationsInteractor
    }

    @Provides
    internal fun serviceWorkUtil(app: TestCoinKeeperApplication): ServiceWorkUtil {
        if (app.serviceWorkUtil == null) {
            app.serviceWorkUtil = mock(ServiceWorkUtil::class.java)
        }
        return app.serviceWorkUtil
    }

    @Provides
    internal fun remoteAddressLocalCache(app: TestCoinKeeperApplication): RemoteAddressLocalCache {
        if (app.remoteAddressLocalCache == null) {
            app.remoteAddressLocalCache = mock(RemoteAddressLocalCache::class.java)
        }
        return app.remoteAddressLocalCache
    }

    @Provides
    internal fun dropbitAccountHelper(app: TestCoinKeeperApplication): DropbitAccountHelper {
        if (app.dropbitAccountHelper == null) {
            app.dropbitAccountHelper = mock(DropbitAccountHelper::class.java)
        }
        return app.dropbitAccountHelper
    }

    @Provides
    internal fun twitter(app: TestCoinKeeperApplication): Twitter {
        if (app.twitter == null) {
            app.twitter = mock(Twitter::class.java)
        }
        return app.twitter
    }

    @Provides
    internal fun hasher(app: TestCoinKeeperApplication): Hasher {
        if (app.hasher == null) {
            app.hasher = mock(Hasher::class.java)
        }
        return app.hasher
    }

    @Provides
    internal fun twitterVerificationController(app: TestCoinKeeperApplication): TwitterVerificationController {
        if (app.twitterVerificationController == null) {
            app.twitterVerificationController = mock(TwitterVerificationController::class.java)
        }
        return app.twitterVerificationController
    }

    @Provides
    internal fun preferencesUtil(app: TestCoinKeeperApplication): PreferencesUtil {
        if (app.preferencesUtil == null) {
            app.preferencesUtil = mock(PreferencesUtil::class.java)
        }
        return app.preferencesUtil
    }

    @Provides
    internal fun countryCodeLocaleGenerator(app: TestCoinKeeperApplication): CountryCodeLocaleGenerator {
        if (app.countryCodeLocaleGenerator == null) {
            app.countryCodeLocaleGenerator = mock(CountryCodeLocaleGenerator::class.java)
        }
        return app.countryCodeLocaleGenerator!!
    }

    @Provides
    internal fun dustProtectionPreference(app: TestCoinKeeperApplication): DustProtectionPreference {
        if (app.dustProtectionPreference == null) {
            app.dustProtectionPreference = mock(DustProtectionPreference::class.java)
        }
        return app.dustProtectionPreference
    }

    @Provides
    internal fun deleteWalletPresenter(app: TestCoinKeeperApplication): DeleteWalletPresenter {
        if (app.deleteWalletPresenter == null) {
            app.deleteWalletPresenter = mock(DeleteWalletPresenter::class.java)
        }
        return app.deleteWalletPresenter
    }

    @Provides
    internal fun yearlyHighViewModel(app: TestCoinKeeperApplication): YearlyHighViewModel {
        if (app.yearlyHighViewModel == null) {
            app.yearlyHighViewModel = mock(YearlyHighViewModel::class.java)
        }
        return app.yearlyHighViewModel
    }

    @Provides
    internal fun defaultCurrencyChangeViewNotifier(app: TestCoinKeeperApplication): DefaultCurrencyChangeViewNotifier {
        if (app.defaultCurrencyChangeViewNotifier == null) {
            app.defaultCurrencyChangeViewNotifier = mock(DefaultCurrencyChangeViewNotifier::class.java)
        }
        return app.defaultCurrencyChangeViewNotifier
    }

    @Provides
    internal fun transactionHistoryDataAdapter(app: TestCoinKeeperApplication): TransactionHistoryDataAdapter {
        if (app.transactionHistoryDataAdapter == null) {
            app.transactionHistoryDataAdapter = mock(TransactionHistoryDataAdapter::class.java)
        }
        return app.transactionHistoryDataAdapter
    }

    @Provides
    internal fun syncManagerViewNotifier(app: TestCoinKeeperApplication): SyncManagerViewNotifier {
        if (app.syncManagerViewNotifier == null) {
            app.syncManagerViewNotifier = mock(SyncManagerViewNotifier::class.java)
        }
        return app.syncManagerViewNotifier
    }
}
