package com.coinninja.coinkeeper.db;

import android.content.Context;

import com.coinninja.coinkeeper.db.schema.V34_Schema;
import com.coinninja.coinkeeper.di.interfaces.ApplicationContext;
import com.coinninja.coinkeeper.model.db.DaoMaster;

import org.greenrobot.greendao.database.Database;

import javax.inject.Inject;

class DatabaseOpenHelper extends DaoMaster.OpenHelper {

    private static final String DB_NAME = "coin-ninja-db";
    private MigrationExecutor migrationExecutor;

    @Inject
    DatabaseOpenHelper(@ApplicationContext Context context, MigrationExecutor migrationExecutor) {
        super(context, DB_NAME);
        this.migrationExecutor = migrationExecutor;
    }

    @Override
    public void onCreate(Database db) {
        new V34_Schema().create(db);
        onUpgrade(db, V34_Schema.SCHEMA_VERSION, DaoMaster.SCHEMA_VERSION);
    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
        if (oldVersion < newVersion)
            migrationExecutor.performUpgrade(db, oldVersion, newVersion);
    }
}
