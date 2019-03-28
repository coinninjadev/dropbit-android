package com.coinninja.coinkeeper.db;

import com.coinninja.coinkeeper.di.interfaces.DBEncryption;

import org.greenrobot.greendao.database.Database;

import javax.inject.Inject;

public class CoinKeeperOpenHelper {
    public static final String TAG = CoinKeeperOpenHelper.class.getSimpleName();
    private final DatabaseOpenHelper databaseOpenHelper;
    private final DatabaseSecretProvider databaseSecretProvider;
    private final boolean withEncryption;


    @Inject
    CoinKeeperOpenHelper(DatabaseOpenHelper databaseOpenHelper, DatabaseSecretProvider databaseSecretProvider,
                         @DBEncryption boolean withEncryption) {
        this.databaseOpenHelper = databaseOpenHelper;
        this.databaseSecretProvider = databaseSecretProvider;
        this.withEncryption = withEncryption;
    }

    public Database getWritableDatabase() {
        if (withEncryption) {
            return getEncryptedWritableDb();
        } else {
            return databaseOpenHelper.getWritableDb();
        }
    }

    private Database getEncryptedWritableDb() {
        try {
            return databaseOpenHelper.getEncryptedWritableDb(databaseSecretProvider.getSecret());
        } catch (net.sqlcipher.database.SQLiteException ex) {
            return databaseOpenHelper.getEncryptedWritableDb(databaseSecretProvider.getDefault());
        }
    }


}
