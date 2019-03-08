package com.coinninja.coinkeeper.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.coinninja.coinkeeper.db.schema.V27_Schema;
import com.coinninja.coinkeeper.model.db.DaoMaster;

import org.greenrobot.greendao.database.Database;

public class CoinKeeperOpenHelper extends DaoMaster.OpenHelper {

    public static final String TAG = CoinKeeperOpenHelper.class.getSimpleName();
    private MigrationExecutor migrationExecutor;

    public CoinKeeperOpenHelper(Context context, String name, MigrationExecutor migrationExecutor) {
        super(context, name);
        this.migrationExecutor = migrationExecutor;
    }

    public CoinKeeperOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
    }

    @Override
    public void onCreate(Database db) {
        new V27_Schema().create(db);
        onUpgrade(db, 27, DaoMaster.SCHEMA_VERSION);
    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion)
            migrationExecutor.performUpgrade(db, oldVersion, newVersion);
    }

}
