package com.coinninja.coinkeeper.db.migrations

import com.coinninja.coinkeeper.db.AbstractMigration
import com.coinninja.coinkeeper.db.Migration
import org.greenrobot.greendao.database.Database

class Migrate_V39_to_V40 : AbstractMigration() {
    override val previousMigration: Migration = Migrate_V38_to_V39()
    override val migratedVersion: Int = 40
    override val targetVersion: Int = 39

    override fun applyMigration(db: Database, currentVersion: Int) {
        db.execSQL("alter table `WALLET` rename to `TEMP_WALLET`")

        db.execSQL("""
                CREATE TABLE IF NOT EXISTS `WALLET` (
                  `_id`               INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                  `HD_INDEX`          INTEGER                           NOT NULL,
                  `USER_ID`           INTEGER                           NOT NULL,
                  `LAST_SYNC`         INTEGER                           NOT NULL,
                  `INTERNAL_INDEX`    INTEGER                           NOT NULL,
                  `EXTERNAL_INDEX`    INTEGER                           NOT NULL,
                  `BALANCE`           INTEGER                           NOT NULL,
                  `SPENDABLE_BALANCE` INTEGER                           NOT NULL,
                  `BLOCK_TIP`         INTEGER                           NOT NULL,
                  `LAST_USDPRICE`     INTEGER                           NOT NULL,
                  `PURPOSE`           INTEGER                           NOT NULL,
                  `COIN_TYPE`         INTEGER                           NOT NULL,
                  `ACCOUNT_INDEX`     INTEGER                           NOT NULL,
                  `FLAGS`             INTEGER                           NOT NULL
                )
                """)

        db.execSQL("""
            insert into WALLET(`_id`,
                `HD_INDEX`,
                `USER_ID`,
                `LAST_SYNC`,
                `INTERNAL_INDEX`,
                `EXTERNAL_INDEX`,
                `BALANCE`,
                `SPENDABLE_BALANCE`,
                `BLOCK_TIP`,
                `LAST_USDPRICE`,
                `PURPOSE`,
                `COIN_TYPE`,
                `ACCOUNT_INDEX`,
                `FLAGS`)
            select `_id`,
                `HD_INDEX`,
                `USER_ID`,
                `LAST_SYNC`,
                `INTERNAL_INDEX`,
                `EXTERNAL_INDEX`,
                `BALANCE`,
                `SPENDABLE_BALANCE`,
                `BLOCK_TIP`,
                `LAST_USDPRICE`,
                `PURPOSE`,
                `COIN_TYPE`,
                `ACCOUNT_INDEX`,
                0
            from `TEMP_WALLET`
        """)

        db.execSQL("drop table `TEMP_WALLET` ")

        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'e345e174b2dabb682c69b4e3f7c46130')")
    }
}
