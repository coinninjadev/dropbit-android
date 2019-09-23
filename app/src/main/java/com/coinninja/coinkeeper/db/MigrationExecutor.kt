package com.coinninja.coinkeeper.db

import android.util.Log
import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.db.CoinKeeperOpenHelper.Companion.TAG
import com.coinninja.coinkeeper.db.migrations.*
import org.greenrobot.greendao.database.Database
import javax.inject.Inject

@Mockable
class MigrationExecutor @Inject internal constructor() {
    fun performUpgrade(db: Database, oldVersion: Int, newVersion: Int) {
        Log.d(TAG, "|--- CoinKeeperOpenHelper --- DB on UPGRADE ---|")
        Log.d(TAG, "|------- Old Version of DB: $oldVersion")
        Log.d(TAG, "|------- New Version of DB: $newVersion")
        when (newVersion) {
            11 -> Migrate_V10_to_V11().runMigration(db, oldVersion)
            12 -> Migrate_V11_to_V12().runMigration(db, oldVersion)
            13 -> Migrate_V12_to_V13().runMigration(db, oldVersion)
            14 -> Migrate_V13_to_V14().runMigration(db, oldVersion)
            15 -> Migrate_V14_to_V15().runMigration(db, oldVersion)
            16 -> Migrate_V15_to_V16().runMigration(db, oldVersion)
            17 -> Migrate_V16_to_V17().runMigration(db, oldVersion)
            18 -> Migrate_V17_to_V18().runMigration(db, oldVersion)
            19 -> Migrate_V18_to_V19().runMigration(db, oldVersion)
            20 -> Migrate_V19_to_V20().runMigration(db, oldVersion)
            21 -> Migrate_V20_to_V21().runMigration(db, oldVersion)
            22 -> Migrate_V21_to_V22().runMigration(db, oldVersion)
            23 -> Migrate_V22_to_V23().runMigration(db, oldVersion)
            24 -> Migrate_V23_to_V24().runMigration(db, oldVersion)
            25 -> Migrate_V24_to_V25().runMigration(db, oldVersion)
            26 -> Migrate_V25_to_V26().runMigration(db, oldVersion)
            27 -> Migrate_V26_to_V27().runMigration(db, oldVersion)
            28 -> Migrate_V27_to_V28().runMigration(db, oldVersion)
            29 -> Migrate_V28_to_V29().runMigration(db, oldVersion)
            30 -> Migrate_V29_to_V30().runMigration(db, oldVersion)
            31 -> Migrate_V30_to_V31().runMigration(db, oldVersion)
            32 -> Migrate_V31_to_V32().runMigration(db, oldVersion)
            33 -> Migrate_V32_to_V33().runMigration(db, oldVersion)
            34 -> Migrate_V33_to_V34().runMigration(db, oldVersion)
            35 -> Migrate_V34_to_V35().runMigration(db, oldVersion)
            36 -> Migrate_V35_to_V36().runMigration(db, oldVersion)
            37 -> Migrate_V36_to_V37().runMigration(db, oldVersion)
        }
    }
}