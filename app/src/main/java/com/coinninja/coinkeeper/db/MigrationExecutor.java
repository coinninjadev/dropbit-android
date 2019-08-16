package com.coinninja.coinkeeper.db;

import android.util.Log;

import com.coinninja.coinkeeper.db.migrations.Migrate_V10_to_V11;
import com.coinninja.coinkeeper.db.migrations.Migrate_V11_to_V12;
import com.coinninja.coinkeeper.db.migrations.Migrate_V12_to_V13;
import com.coinninja.coinkeeper.db.migrations.Migrate_V13_to_V14;
import com.coinninja.coinkeeper.db.migrations.Migrate_V14_to_V15;
import com.coinninja.coinkeeper.db.migrations.Migrate_V15_to_V16;
import com.coinninja.coinkeeper.db.migrations.Migrate_V16_to_V17;
import com.coinninja.coinkeeper.db.migrations.Migrate_V17_to_V18;
import com.coinninja.coinkeeper.db.migrations.Migrate_V18_to_V19;
import com.coinninja.coinkeeper.db.migrations.Migrate_V19_to_V20;
import com.coinninja.coinkeeper.db.migrations.Migrate_V20_to_V21;
import com.coinninja.coinkeeper.db.migrations.Migrate_V21_to_V22;
import com.coinninja.coinkeeper.db.migrations.Migrate_V22_to_V23;
import com.coinninja.coinkeeper.db.migrations.Migrate_V23_to_V24;
import com.coinninja.coinkeeper.db.migrations.Migrate_V24_to_V25;
import com.coinninja.coinkeeper.db.migrations.Migrate_V25_to_V26;
import com.coinninja.coinkeeper.db.migrations.Migrate_V26_to_V27;
import com.coinninja.coinkeeper.db.migrations.Migrate_V27_to_V28;
import com.coinninja.coinkeeper.db.migrations.Migrate_V28_to_V29;
import com.coinninja.coinkeeper.db.migrations.Migrate_V29_to_V30;
import com.coinninja.coinkeeper.db.migrations.Migrate_V30_to_V31;
import com.coinninja.coinkeeper.db.migrations.Migrate_V31_to_V32;
import com.coinninja.coinkeeper.db.migrations.Migrate_V32_to_V33;
import com.coinninja.coinkeeper.db.migrations.Migrate_V33_to_V34;
import com.coinninja.coinkeeper.db.migrations.Migrate_V34_to_V35;
import com.coinninja.coinkeeper.db.migrations.Migrate_V35_to_V36;

import org.greenrobot.greendao.database.Database;

import javax.inject.Inject;

public class MigrationExecutor {
    @Inject
    MigrationExecutor() {

    }

    public void performUpgrade(Database db, int oldVersion, int newVersion) {

        Log.d(CoinKeeperOpenHelper.TAG, "|--- CoinKeeperOpenHelper --- DB on UPGRADE ---|");
        Log.d(CoinKeeperOpenHelper.TAG, "|------- Old Version of DB: " + String.valueOf(oldVersion));
        Log.d(CoinKeeperOpenHelper.TAG, "|------- New Version of DB: " + String.valueOf(newVersion));
        switch (newVersion) {
            case 11:
                new Migrate_V10_to_V11().runMigration(db, oldVersion);
                break;
            case 12:
                new Migrate_V11_to_V12().runMigration(db, oldVersion);
                break;
            case 13:
                new Migrate_V12_to_V13().runMigration(db, oldVersion);
                break;
            case 14:
                new Migrate_V13_to_V14().runMigration(db, oldVersion);
                break;
            case 15:
                new Migrate_V14_to_V15().runMigration(db, oldVersion);
                break;
            case 16:
                new Migrate_V15_to_V16().runMigration(db, oldVersion);
                break;
            case 17:
                new Migrate_V16_to_V17().runMigration(db, oldVersion);
                break;
            case 18:
                new Migrate_V17_to_V18().runMigration(db, oldVersion);
                break;
            case 19:
                new Migrate_V18_to_V19().runMigration(db, oldVersion);
                break;
            case 20:
                new Migrate_V19_to_V20().runMigration(db, oldVersion);
                break;
            case 21:
                new Migrate_V20_to_V21().runMigration(db, oldVersion);
                break;
            case 22:
                new Migrate_V21_to_V22().runMigration(db, oldVersion);
                break;
            case 23:
                new Migrate_V22_to_V23().runMigration(db, oldVersion);
                break;
            case 24:
                new Migrate_V23_to_V24().runMigration(db, oldVersion);
                break;
            case 25:
                new Migrate_V24_to_V25().runMigration(db, oldVersion);
                break;
            case 26:
                new Migrate_V25_to_V26().runMigration(db, oldVersion);
                break;
            case 27:
                new Migrate_V26_to_V27().runMigration(db, oldVersion);
                break;
            case 28:
                new Migrate_V27_to_V28().runMigration(db, oldVersion);
                break;
            case 29:
                new Migrate_V28_to_V29().runMigration(db, oldVersion);
                break;
            case 30:
                new Migrate_V29_to_V30().runMigration(db, oldVersion);
                break;
            case 31:
                new Migrate_V30_to_V31().runMigration(db, oldVersion);
                break;
            case 32:
                new Migrate_V31_to_V32().runMigration(db, oldVersion);
                break;
            case 33:
                new Migrate_V32_to_V33().runMigration(db, oldVersion);
                break;
            case 34:
                new Migrate_V33_to_V34().runMigration(db, oldVersion);
                break;
            case 35:
                new Migrate_V34_to_V35().runMigration(db, oldVersion);
                break;
            case 36:
                new Migrate_V35_to_V36().runMigration(db, oldVersion);
                break;
        }
    }
}