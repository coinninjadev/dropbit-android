package com.coinninja.coinkeeper.di.module

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import app.coinninja.cn.persistance.DropbitConnectionCallback
import app.coinninja.cn.persistance.DropbitDatabase
import com.coinninja.coinkeeper.BuildConfig
import com.coinninja.coinkeeper.db.CoinKeeperOpenHelper
import com.coinninja.coinkeeper.db.DatabaseSecretProvider
import com.coinninja.coinkeeper.di.interfaces.*
import com.coinninja.coinkeeper.model.db.DaoMaster
import com.coinninja.coinkeeper.model.helpers.DaoSessionManager
import com.commonsware.cwac.saferoom.SafeHelperFactory
import dagger.Module
import dagger.Provides
import org.greenrobot.greendao.database.Database

@Module(includes = [AppModule::class])
class DaoSessionManagerModule {

    @Provides
    @CoinkeeperApplicationScope
    internal fun daoSessionManager(daoMaster: DaoMaster): DaoSessionManager {
        return DaoSessionManager(daoMaster, BuildConfig.PURPOSE, BuildConfig.COIN_TYPE, BuildConfig.ACCOUNT_INDEX).connect()
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
    internal fun secret(@ApplicationContext context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    @Provides
    @DefaultSecret
    internal fun defaultSecret(): String {
        return BuildConfig.DEFAULT_SALT
    }

    @Provides
    internal fun dropbitDatabase(@ApplicationContext context: Context,
                                 secretProvider: DatabaseSecretProvider,
                                 @DBEncryption withEncryption: Boolean): DropbitDatabase =
            if (withEncryption) {
                try {
                    val factory = SafeHelperFactory(secretProvider.secret.toByteArray())
                    DropbitDatabase.getDatabase(context, COIN_KEEPER_DB_NAME, DropbitConnectionCallback(), factory)
                } catch (ex: net.sqlcipher.database.SQLiteException) {
                    val factory = SafeHelperFactory(secretProvider.default.toByteArray())
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
