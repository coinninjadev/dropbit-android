package com.coinninja.coinkeeper.di.module

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import app.coinninja.cn.persistance.DropbitConnectionCallback
import app.coinninja.cn.persistance.DropbitDatabase
import com.coinninja.coinkeeper.BuildConfig
import com.coinninja.coinkeeper.cn.wallet.WalletConfiguration
import com.coinninja.coinkeeper.db.*
import com.coinninja.coinkeeper.db.DatabaseOpenHelper
import com.coinninja.coinkeeper.di.interfaces.*
import com.coinninja.coinkeeper.model.db.DaoMaster
import com.coinninja.coinkeeper.model.helpers.DaoSessionManager
import com.coinninja.coinkeeper.util.Hasher
import com.commonsware.cwac.saferoom.SafeHelperFactory
import dagger.Module
import dagger.Provides
import org.greenrobot.greendao.database.Database

@Module(includes = [AppModule::class])
class DaoSessionManagerModule {

    @Provides
    @CoinkeeperApplicationScope
    internal fun daoSessionManager(daoMaster: DaoMaster, walletConfiguration: WalletConfiguration): DaoSessionManager {
        return DaoSessionManager(daoMaster, walletConfiguration).connect()
    }

    @Provides
    internal fun database(coinKeeperOpenHelper: CoinKeeperOpenHelper): Database {
        return coinKeeperOpenHelper.writableDatabase
    }

    @Provides
    internal fun daoMaster(database: Database): DaoMaster {
        return DaoMaster(database)
    }

    @Provides
    @DBEncryption
    internal fun withEncryption(): Boolean {
        return BuildConfig.CN_DB_ENCRYPTION_ENABLED
    }

    @SuppressLint("HardwareIds")
    @Provides
    @AppSecret
    internal fun secret(@ApplicationContext context: Context): CharArray {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID).toCharArray()
    }

    @Provides
    @DefaultSecret
    internal fun defaultSecret(): CharArray {
        return BuildConfig.DEFAULT_SALT.toCharArray()
    }

    @Provides
    internal fun coinkeeperOpenHandler(databaseOpenHelper: DatabaseOpenHelper,
                                       databaseSecretProvider: DatabaseSecretProvider,
                                       upgradeDBFormatStorage: UpgradeDBFormatStorage,
                                       @DBEncryption withEncryption: Boolean
    ): CoinKeeperOpenHelper {
        return CoinKeeperOpenHelper(databaseOpenHelper, databaseSecretProvider, upgradeDBFormatStorage, withEncryption)
    }

    @Provides
    internal fun databaseOpenHelper(@ApplicationContext context: Context): DatabaseOpenHelper {
        return DatabaseOpenHelper(context, MigrationExecutor(), DaoMaster.SCHEMA_VERSION)
    }

    @Provides
    internal fun databaseSecretProvider(@AppSecret secret: CharArray, @DefaultSecret defaultSecret: CharArray): DatabaseSecretProvider {
        return DatabaseSecretProvider(Hasher(), secret, defaultSecret)
    }

    @Provides
    internal fun dropbitDatabase(@ApplicationContext context: Context,
                                 secretProvider: DatabaseSecretProvider,
                                 @DBEncryption withEncryption: Boolean): DropbitDatabase =
            if (withEncryption) {
                try {
                    val factory = SafeHelperFactory(secretProvider.secret)
                    DropbitDatabase.getDatabase(context, COIN_KEEPER_DB_NAME, DropbitConnectionCallback(), factory)
                } catch (ex: net.sqlcipher.database.SQLiteException) {
                    val factory = SafeHelperFactory(secretProvider.default)
                    DropbitDatabase.getDatabase(context, COIN_KEEPER_DB_NAME, DropbitConnectionCallback(), factory)
                }
            } else {
                DropbitDatabase.getDatabase(context, COIN_KEEPER_DB_NAME, DropbitConnectionCallback())
            }

    companion object {
        private const val DROPBIT_DB_NAME = "dropbit_db"
        private const val COIN_KEEPER_DB_NAME = "coin-ninja-db"

    }
}
