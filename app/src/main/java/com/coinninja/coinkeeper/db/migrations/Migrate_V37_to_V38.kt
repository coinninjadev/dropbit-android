package com.coinninja.coinkeeper.db.migrations

import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.coinninja.coinkeeper.db.AbstractMigration
import com.coinninja.coinkeeper.db.Migration
import org.greenrobot.greendao.database.Database

class Migrate_V37_to_V38 : AbstractMigration() {
    override val previousMigration: Migration = Migrate_V36_to_V37()
    override val migratedVersion: Int = 38
    override val targetVersion: Int = 37

    override fun applyMigration(db: Database, currentVersion: Int) {
        db.execSQL("alter table TRANSACTION_SUMMARY rename to TEMP_TRANSACTION_SUMMARY")

        Log.w("upgrade v38", "create temp table")

        db.execSQL("""
         CREATE TABLE IF NOT EXISTS `TRANSACTION_SUMMARY` (
          `_id`                             INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
          `WALLET_ID`                       INTEGER,
          `TRANSACTIONS_INVITES_SUMMARY_ID` INTEGER,
          `TXID`                            TEXT                              NOT NULL,
          `SOUGHT_NOTIFICATION`             INTEGER                           NOT NULL,
          `FEE`                             INTEGER,
          `TX_TIME`                         INTEGER,
          `NUM_CONFIRMATIONS`               INTEGER                           NOT NULL,
          `BLOCKHASH`                       TEXT                              NOT NULL,
          `NUM_INPUTS`                      INTEGER                           NOT NULL,
          `NUM_OUTPUTS`                     INTEGER                           NOT NULL,
          `BLOCKHEIGHT`                     INTEGER,
          `HISTORIC_PRICE`                  INTEGER                           NOT NULL,
          `TRANSACTION_NOTIFICATION_ID`     INTEGER,
          `MEM_POOL_STATE`                  INTEGER                           NOT NULL,
          `IS_LIGHTNING_WITHDRAW`           INTEGER                           NOT NULL,
          FOREIGN KEY (`WALLET_ID`) REFERENCES `WALLET` (`_id`)
            ON UPDATE NO ACTION
            ON DELETE NO ACTION,
          FOREIGN KEY (`TRANSACTIONS_INVITES_SUMMARY_ID`) REFERENCES `TRANSACTIONS_INVITES_SUMMARY` (`_id`)
            ON UPDATE NO ACTION
            ON DELETE NO ACTION
        )   
        """)

        Log.w("upgrade v38", "create create table")

        db.execSQL("""
            insert into `TRANSACTION_SUMMARY` 
            (
                `_id`,
                 `WALLET_ID`,
                 `TRANSACTIONS_INVITES_SUMMARY_ID`,
                 `TXID`,
                 `SOUGHT_NOTIFICATION`,
                 `FEE`,
                 `TX_TIME`,
                 `NUM_CONFIRMATIONS`,
                 `BLOCKHASH`,
                 `NUM_INPUTS`,
                 `NUM_OUTPUTS`,
                 `BLOCKHEIGHT`,
                 `HISTORIC_PRICE`,
                 `TRANSACTION_NOTIFICATION_ID`,
                 `MEM_POOL_STATE`,
                 `IS_LIGHTNING_WITHDRAW`
            )
            select 
                `_id`,
                `WALLET_ID`,
                `TRANSACTIONS_INVITES_SUMMARY_ID`,
                `TXID`,
                `SOUGHT_NOTIFICATION`,
                `FEE`,
                `TX_TIME`,
                `NUM_CONFIRMATIONS`,
                `BLOCKHASH`,
                `NUM_INPUTS`,
                `NUM_OUTPUTS`,
                `BLOCKHEIGHT`,
                `HISTORIC_PRICE`,
                `TRANSACTION_NOTIFICATION_ID`,
                `MEM_POOL_STATE`,
                0
            from TEMP_TRANSACTION_SUMMARY
        """)
        Log.w("upgrade v38", "populate from old data")

        db.execSQL("drop table TEMP_TRANSACTION_SUMMARY")
        Log.w("upgrade v38", "remove temp table")

        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'fb7656fe776f84c0dafb448328cc31e7')");
        Log.w("upgrade v38", "update room version")
    }
}
