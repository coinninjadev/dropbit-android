package com.coinninja.coinkeeper.di.module;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;

import com.coinninja.coinkeeper.BuildConfig;
import com.coinninja.coinkeeper.db.CoinKeeperOpenHelper;
import com.coinninja.coinkeeper.db.MigrationExecutor;
import com.coinninja.coinkeeper.di.interfaces.AppSecret;
import com.coinninja.coinkeeper.di.interfaces.ApplicationContext;
import com.coinninja.coinkeeper.di.interfaces.CoinkeeperApplicationScope;
import com.coinninja.coinkeeper.di.interfaces.DBEncryption;
import com.coinninja.coinkeeper.model.db.DaoMaster;
import com.coinninja.coinkeeper.model.helpers.DaoSessionManager;
import com.coinninja.coinkeeper.util.Hasher;
import com.coinninja.coinkeeper.util.Intents;

import org.greenrobot.greendao.database.Database;

import dagger.Module;
import dagger.Provides;

@Module(includes = {AppModule.class})
public class DaoSessionManagerModule {

    @Provides
    @CoinkeeperApplicationScope
    DaoSessionManager daoSessionManager(DaoMaster daoMaster) {
        return new DaoSessionManager(daoMaster).connect();
    }

    @Provides
    Database database(CoinKeeperOpenHelper coinKeeperOpenHelper, @AppSecret char[] secret, @DBEncryption boolean withEncryption) {
        if (withEncryption) {
            return coinKeeperOpenHelper.getEncryptedWritableDb(secret);
        } else {
            return coinKeeperOpenHelper.getWritableDb();
        }
    }

    @Provides
    CoinKeeperOpenHelper coinKeeperOpenHelper(@ApplicationContext Context context) {
        return new CoinKeeperOpenHelper(context, Intents.DB_NAME, new MigrationExecutor());
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
    char[] secret(@ApplicationContext Context context, Hasher hasher) {
        String secret;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            secret = BuildConfig.DEFAULT_SALT;
        } else {
            secret = hasher.hash(Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID));
        }
        return secret.toCharArray();
    }

}
