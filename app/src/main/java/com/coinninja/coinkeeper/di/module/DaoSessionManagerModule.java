package com.coinninja.coinkeeper.di.module;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings;

import com.coinninja.coinkeeper.BuildConfig;
import com.coinninja.coinkeeper.db.CoinKeeperOpenHelper;
import com.coinninja.coinkeeper.di.interfaces.AppSecret;
import com.coinninja.coinkeeper.di.interfaces.ApplicationContext;
import com.coinninja.coinkeeper.di.interfaces.CoinkeeperApplicationScope;
import com.coinninja.coinkeeper.di.interfaces.DBEncryption;
import com.coinninja.coinkeeper.di.interfaces.DefaultSecret;
import com.coinninja.coinkeeper.model.db.DaoMaster;
import com.coinninja.coinkeeper.model.helpers.DaoSessionManager;
import com.coinninja.coinkeeper.util.Hasher;

import org.greenrobot.greendao.database.Database;

import dagger.Module;
import dagger.Provides;

@Module(includes = {AppModule.class})
public class DaoSessionManagerModule {

    @Provides
    @CoinkeeperApplicationScope
    DaoSessionManager daoSessionManager(DaoMaster daoMaster) {
        return new DaoSessionManager(daoMaster, BuildConfig.PURPOSE, BuildConfig.COIN_TYPE, BuildConfig.ACCOUNT_INDEX).connect();
    }

    @Provides
    Database database(CoinKeeperOpenHelper coinKeeperOpenHelper) {
        return coinKeeperOpenHelper.getWritableDatabase();
    }

    @Provides
    DaoMaster daoMaster(Database database) {
        return new DaoMaster(database);
    }

    @Provides
    @DBEncryption
    boolean withEncryption() {
        return BuildConfig.CN_DB_ENCRYPTION_ENABLED;
    }

    @SuppressLint("HardwareIds")
    @Provides
    @AppSecret
    String secret(@ApplicationContext Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    @Provides
    @DefaultSecret
    String defaultSecret() {
        return BuildConfig.DEFAULT_SALT;
    }
}
