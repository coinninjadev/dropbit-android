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
import app.coinninja.cn.thunderdome.repository.ThunderDomeRepository
import app.dropbit.twitter.Twitter
import com.coinninja.coinkeeper.CoinKeeperApplication
import com.coinninja.coinkeeper.CoinKeeperLifecycleListener
import com.coinninja.coinkeeper.TestCoinKeeperApplication
import com.coinninja.coinkeeper.cn.account.AccountManager
import com.coinninja.coinkeeper.cn.service.YearlyHighViewModel
import com.coinninja.coinkeeper.cn.wallet.CNWalletManager
import com.coinninja.coinkeeper.cn.wallet.HDWalletWrapper
import com.coinninja.coinkeeper.cn.wallet.SyncWalletManager
import com.coinninja.coinkeeper.cn.wallet.dust.DustProtectionPreference
import com.coinninja.coinkeeper.cn.wallet.service.CNServiceConnection
import com.coinninja.coinkeeper.cn.wallet.tx.TransactionFundingManager
import com.coinninja.coinkeeper.di.component.CoinKeeperComponent
import com.coinninja.coinkeeper.di.interfaces.*
import com.coinninja.coinkeeper.di.interfaces.UUID
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
import com.coinninja.coinkeeper.service.runner.*
import com.coinninja.coinkeeper.ui.account.verify.twitter.TwitterVerificationController
import com.coinninja.coinkeeper.ui.dropbit.me.DropbitMeConfiguration
import com.coinninja.coinkeeper.ui.settings.DeleteWalletPresenter
import com.coinninja.coinkeeper.ui.transaction.DefaultCurrencyChangeViewNotifier
import com.coinninja.coinkeeper.ui.transaction.SyncManagerViewNotifier
import com.coinninja.coinkeeper.ui.transaction.history.TransactionHistoryDataAdapter
import com.coinninja.coinkeeper.util.*
import com.coinninja.coinkeeper.util.analytics.Analytics
import com.coinninja.coinkeeper.util.android.*
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil
import com.coinninja.coinkeeper.util.android.app.JobIntentService.JobServiceScheduler
import com.coinninja.coinkeeper.util.crypto.BitcoinUri
import com.coinninja.coinkeeper.util.crypto.BitcoinUtil
import com.coinninja.coinkeeper.view.widget.phonenumber.CountryCodeLocale
import com.coinninja.coinkeeper.view.widget.phonenumber.CountryCodeLocaleGenerator
import com.coinninja.messaging.MessageCryptor
import com.google.gson.Gson
import com.nhaarman.mockitokotlin2.mock
import dagger.Module
import dagger.Provides
import org.mockito.Mockito.mock
import java.util.*

@Module
class TestAppModule {

    @Provides
    internal fun bitcoinUriBuilder(app: TestCoinKeeperApplication): BitcoinUri.Builder {
        if (app.bitcoinUriBuilder == null)
            app.bitcoinUriBuilder = mock()
        return app.bitcoinUriBuilder
    }

    @Provides
    @LightningDepositAddress
    fun lightningDepositAddress(): String =  "--lightning-deposit-address--"

    @Provides
    internal fun coinKeeperApplication(application: Application): CoinKeeperApplication {
        return application as CoinKeeperApplication
    }

    @Provides
    internal fun testCoinKeeperApplication(application: Application): TestCoinKeeperApplication {
        return application as TestCoinKeeperApplication
    }

    @Provides
    internal fun coinKeeperComponent(): CoinKeeperComponent {
        return mock()
    }

    @Provides
    internal fun coinKeeperLifecycleListener(app: TestCoinKeeperApplication): CoinKeeperLifecycleListener {
        if (app.coinKeeperLifecycleListener == null)
            app.coinKeeperLifecycleListener = mock()
        return app.coinKeeperLifecycleListener
    }

    @Provides
    internal fun provideI18PhoneNumberUtil(): com.google.i18n.phonenumbers.PhoneNumberUtil {
        return com.google.i18n.phonenumbers.PhoneNumberUtil.getInstance()
    }

    @Provides
    @ApplicationContext
    internal fun context(application: CoinKeeperApplication): Context {
        return application
    }

    @Provides
    internal fun contentResolver(application: TestCoinKeeperApplication): ContentResolver {
        return application.contentResolver
    }

    @Provides
    internal fun contactLookupService(): ContactLookupService {
        return ContactLookupService()
    }

    @Provides
    internal fun analytics(app: TestCoinKeeperApplication): Analytics {
        if (app.analytics == null)
            app.analytics = mock()
        return app.analytics
    }

    @Provides
    internal fun messageCryptor(app: TestCoinKeeperApplication): MessageCryptor {
        if (app.messageCryptor == null)
            app.messageCryptor = mock()
        return app.messageCryptor
    }

    @Provides
    internal fun phoneNumberUtil(app: TestCoinKeeperApplication): PhoneNumberUtil {
        if (app.phoneNumberUtil == null)
            app.phoneNumberUtil = mock()
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
            app.localBroadCastUtil = mock()
        return app.localBroadCastUtil
    }

    @Provides
    internal fun clipboardManager(app: TestCoinKeeperApplication): ClipboardManager {
        if (app.clipboardManager == null)
            app.clipboardManager = mock()
        return app.clipboardManager
    }

    @Provides
    internal fun hdWallet(app: TestCoinKeeperApplication): HDWalletWrapper {
        if (app.hdWallet == null)
            app.hdWallet = mock()
        return app.hdWallet
    }

    @Provides
    internal fun bitcoinUtil(app: TestCoinKeeperApplication): BitcoinUtil {
        if (app.bitcoinUtil == null)
            app.bitcoinUtil = mock()
        return app.bitcoinUtil
    }

    @Provides
    internal fun syncWalletManager(app: TestCoinKeeperApplication): SyncWalletManager {
        if (app.syncWalletManager == null) {
            app.syncWalletManager = mock()
        }
        return app.syncWalletManager
    }

    @Provides
    internal fun sharedPreferences(app: TestCoinKeeperApplication): SharedPreferences {
        if (app.sharedPreferences == null) {
            app.sharedPreferences = mock()
        }
        return app.sharedPreferences
    }

    @Provides
    internal fun walletHelper(app: TestCoinKeeperApplication): WalletHelper {
        if (app.walletHelper == null) {
            app.walletHelper = mock()
        }
        return app.walletHelper
    }

    @Provides
    internal fun account(app: TestCoinKeeperApplication): Account {
        if (app.account == null) {
            app.account = mock()
        }
        return app.account
    }

    @Provides
    internal fun jobServiceScheduler(app: TestCoinKeeperApplication): JobServiceScheduler {
        if (app.jobServiceScheduler == null) {
            app.jobServiceScheduler = mock()
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
            app.authentication = mock()
        }
        return app.authentication
    }

    @Provides
    internal fun pinEntry(app: TestCoinKeeperApplication): PinEntry {
        if (app.pinEntry == null) {
            app.pinEntry = mock()
        }
        return app.pinEntry
    }

    @Provides
    internal fun userHelper(app: TestCoinKeeperApplication): UserHelper {
        if (app.userHelper == null) {
            app.userHelper = mock()
        }
        return app.userHelper
    }

    @TimeOutHandler
    @Provides
    internal fun timeoutHandler(app: TestCoinKeeperApplication): Handler {
        if (app.handler == null) {
            app.handler = mock()
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
            app.cnWalletManager = mock()
        }
        return app.cnWalletManager
    }

    @Provides
    internal fun syncIncomingInvitesRunner(app: TestCoinKeeperApplication): SyncIncomingInvitesRunner {
        if (app.syncIncomingInvitesRunner == null) {
            app.syncIncomingInvitesRunner = mock()
        }
        return app.syncIncomingInvitesRunner
    }

    @Provides
    internal fun fulfillSentInvitesRunner(app: TestCoinKeeperApplication): FulfillSentInvitesRunner {
        if (app.fulfillSentInvitesRunner == null) {
            app.fulfillSentInvitesRunner = mock()
        }
        return app.fulfillSentInvitesRunner
    }

    @Provides
    internal fun receivedInvitesStatusRunner(app: TestCoinKeeperApplication): ReceivedInvitesStatusRunner {
        if (app.receivedInvitesStatusRunner == null) {
            app.receivedInvitesStatusRunner = mock()
        }
        return app.receivedInvitesStatusRunner
    }

    @Provides
    internal fun negativeBalanceRunner(app: TestCoinKeeperApplication): NegativeBalanceRunner {
        if (app.negativeBalanceRunner == null) {
            app.negativeBalanceRunner = mock()
        }
        return app.negativeBalanceRunner
    }

    @Provides
    internal fun failedBroadcastCleaner(app: TestCoinKeeperApplication): FailedBroadcastCleaner {
        if (app.failedBroadcastCleaner == null) {
            app.failedBroadcastCleaner = mock()
        }
        return app.failedBroadcastCleaner
    }

    @Provides
    internal fun permissionsUtil(app: TestCoinKeeperApplication): PermissionsUtil {
        if (app.permissionsUtil == null) {
            app.permissionsUtil = mock()
        }
        return app.permissionsUtil
    }

    @Provides
    internal fun cnServiceConnection(app: TestCoinKeeperApplication): CNServiceConnection {
        if (app.cnServiceConnection == null) {
            app.cnServiceConnection = mock()
        }
        return app.cnServiceConnection

    }

    @NumAddressesToCache
    @Provides
    internal fun provideNumAddressesAhead(): Int {
        return 10
    }


    @Provides
    internal fun accountManager(app: TestCoinKeeperApplication): AccountManager {
        if (app.accountManager == null) {
            app.accountManager = mock()
        }
        return app.accountManager
    }


    @Provides
    internal fun typedValue(app: TestCoinKeeperApplication): TypedValue {
        if (app.typedValue == null) {
            app.typedValue = mock()
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
            app.transactionFundingManager = mock()
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
            app.currencyPreference = mock()
        }
        return app.currencyPreference
    }

    @Provides
    internal fun defaultCurrencies(app: TestCoinKeeperApplication): DefaultCurrencies {
        if (app.defaultCurrencies == null) {
            app.defaultCurrencies = mock()
        }
        return app.defaultCurrencies
    }

    @Provides
    internal fun provideLocationManager(): LocationManager {
        return mock()
    }

    @Provides
    internal fun activityNavigationUtil(app: TestCoinKeeperApplication): ActivityNavigationUtil {
        if (app.activityNavigationUtil == null) {
            app.activityNavigationUtil = mock()
        }
        return app.activityNavigationUtil
    }

    @Provides
    internal fun locationUtil(app: TestCoinKeeperApplication): LocationUtil {
        if (app.locationUtil == null) {
            app.locationUtil = mock()
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
            app.inviteContactPresenter = mock()
        }
        return app.inviteContactPresenter
    }

    @Provides
    internal fun clipboardUtil(app: TestCoinKeeperApplication): ClipboardUtil {
        if (app.clipboardUtil == null) {
            app.clipboardUtil = mock()
        }

        return app.clipboardUtil
    }

    @Provides
    internal fun userPreferences(app: TestCoinKeeperApplication): UserPreferences {
        if (app.userPreferences == null) {
            app.userPreferences = mock()
        }
        return app.userPreferences
    }

    @Provides
    internal fun verifyRecoveryWordsPresenter(app: TestCoinKeeperApplication): VerifyRecoveryWordsPresenter {
        if (app.verifyRecoveryWordsPresenter == null) {
            app.verifyRecoveryWordsPresenter = mock()
        }
        return app.verifyRecoveryWordsPresenter
    }

    @Provides
    internal fun notificationUtil(app: TestCoinKeeperApplication): NotificationUtil {
        if (app.notificationUtil == null) {
            app.notificationUtil = mock()
        }
        return app.notificationUtil
    }

    @Provides
    internal fun dropbitMeConfiguration(app: TestCoinKeeperApplication): DropbitMeConfiguration {
        if (app.dropbitMeConfiguration == null) {
            app.dropbitMeConfiguration = mock()
        }
        return app.dropbitMeConfiguration

    }

    @Provides
    internal fun serviceWorkUtil(app: TestCoinKeeperApplication): ServiceWorkUtil {
        if (app.serviceWorkUtil == null) {
            app.serviceWorkUtil = mock()
        }
        return app.serviceWorkUtil
    }

    @Provides
    internal fun remoteAddressLocalCache(app: TestCoinKeeperApplication): RemoteAddressLocalCache {
        if (app.remoteAddressLocalCache == null) {
            app.remoteAddressLocalCache = mock()
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
            app.twitter = mock()
        }
        return app.twitter
    }

    @Provides
    internal fun hasher(app: TestCoinKeeperApplication): Hasher {
        if (app.hasher == null) {
            app.hasher = mock()
        }
        return app.hasher
    }

    @Provides
    internal fun twitterVerificationController(app: TestCoinKeeperApplication): TwitterVerificationController {
        if (app.twitterVerificationController == null) {
            app.twitterVerificationController = mock()
        }
        return app.twitterVerificationController
    }

    @Provides
    internal fun preferencesUtil(app: TestCoinKeeperApplication): PreferencesUtil {
        if (app.preferencesUtil == null) {
            app.preferencesUtil = mock()
        }
        return app.preferencesUtil
    }

    @Provides
    internal fun countryCodeLocaleGenerator(app: TestCoinKeeperApplication): CountryCodeLocaleGenerator {
        if (app.countryCodeLocaleGenerator == null) {
            app.countryCodeLocaleGenerator = mock()
        }
        return app.countryCodeLocaleGenerator!!
    }

    @Provides
    internal fun dustProtectionPreference(app: TestCoinKeeperApplication): DustProtectionPreference {
        if (app.dustProtectionPreference == null) {
            app.dustProtectionPreference = mock()
        }
        return app.dustProtectionPreference
    }

    @Provides
    internal fun deleteWalletPresenter(app: TestCoinKeeperApplication): DeleteWalletPresenter {
        if (app.deleteWalletPresenter == null) {
            app.deleteWalletPresenter = mock()
        }
        return app.deleteWalletPresenter
    }

    @Provides
    internal fun yearlyHighViewModel(app: TestCoinKeeperApplication): YearlyHighViewModel {
        if (app.yearlyHighViewModel == null) {
            app.yearlyHighViewModel = mock()
        }
        return app.yearlyHighViewModel
    }

    @Provides
    internal fun defaultCurrencyChangeViewNotifier(app: TestCoinKeeperApplication): DefaultCurrencyChangeViewNotifier {
        if (app.defaultCurrencyChangeViewNotifier == null) {
            app.defaultCurrencyChangeViewNotifier = mock()
        }
        return app.defaultCurrencyChangeViewNotifier
    }

    @Provides
    internal fun transactionHistoryDataAdapter(app: TestCoinKeeperApplication): TransactionHistoryDataAdapter {
        if (app.transactionHistoryDataAdapter == null) {
            app.transactionHistoryDataAdapter = mock()
        }
        return app.transactionHistoryDataAdapter
    }

    @Provides
    internal fun syncManagerViewNotifier(app: TestCoinKeeperApplication): SyncManagerViewNotifier {
        if (app.syncManagerViewNotifier == null) {
            app.syncManagerViewNotifier = mock()
        }
        return app.syncManagerViewNotifier
    }
}
