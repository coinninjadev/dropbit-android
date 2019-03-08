package com.coinninja.coinkeeper.db.migrations;

import com.coinninja.coinkeeper.db.AbstractMigration;
import com.coinninja.coinkeeper.db.Migration;

import org.greenrobot.greendao.database.Database;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Migrate_V26_to_V27 extends AbstractMigration {
    /**
     * // DataGrip Query
     * BEGIN TRANSACTION
     * <p>
     * ALTER TABLE TRANSACTION_SUMMARY
     * RENAME TO TEMP_TRANSACTION_SUMMARY;
     * <p>
     * CREATE TABLE TRANSACTION_SUMMARY (
     * _id                             INTEGER PRIMARY KEY,
     * TXID                            TEXT UNIQUE,
     * TRANSACTIONS_INVITES_SUMMARY_ID INTEGER,
     * SOUGHT_NOTIFICATION             INTEGER NOT NULL,
     * WALLET_ID                       INTEGER NOT NULL,
     * FEE                             INTEGER NOT NULL,
     * TX_TIME                         INTEGER NOT NULL,
     * NUM_CONFIRMATIONS               INTEGER NOT NULL,
     * BLOCKHASH                       TEXT,
     * TO_NAME                         TEXT,
     * TO_PHONE_NUMBER                 TEXT,
     * NUM_INPUTS                      INTEGER NOT NULL,
     * NUM_OUTPUTS                     INTEGER NOT NULL,
     * BLOCKHEIGHT                     INTEGER NOT NULL,
     * HISTORIC_PRICE                  INTEGER NOT NULL,
     * MEM_POOL_STATE                  INTEGER,
     * TRANSACTION_NOTIFICATION_ID     INTEGER
     * );
     * <p>
     * insert into TRANSACTION_SUMMARY
     * select _id,
     * TXID,
     * TRANSACTIONS_INVITES_SUMMARY_ID,
     * SOUGHT_NOTIFICATION,
     * WALLET_ID,
     * FEE,
     * TX_TIME,
     * NUM_CONFIRMATIONS,
     * BLOCKHASH,
     * TO_NAME,
     * TO_PHONE_NUMBER,
     * NUM_INPUTS,
     * NUM_OUTPUTS,
     * BLOCKHEIGHT,
     * HISTORIC_PRICE,
     * MEM_POOL_STATE,
     * TRANSACTION_NOTIFICATION_ID
     * from TEMP_TRANSACTION_SUMMARY;
     * <p>
     * drop table TEMP_TRANSACTION_SUMMARY;
     * <p>
     * <p>
     * COMMIT
     */
    @Override
    protected void applyMigration(@NonNull Database db, int currentVersion) {
        db.execSQL("ALTER TABLE TRANSACTION_SUMMARY RENAME TO TEMP_TRANSACTION_SUMMARY");
        db.execSQL("CREATE TABLE TRANSACTION_SUMMARY (\n" +
                "  _id                             INTEGER PRIMARY KEY,\n" +
                "  TXID                            TEXT UNIQUE,\n" +
                "  TRANSACTIONS_INVITES_SUMMARY_ID INTEGER,\n" +
                "  SOUGHT_NOTIFICATION             INTEGER NOT NULL,\n" +
                "  WALLET_ID                       INTEGER NOT NULL,\n" +
                "  FEE                             INTEGER NOT NULL,\n" +
                "  TX_TIME                         INTEGER NOT NULL,\n" +
                "  NUM_CONFIRMATIONS               INTEGER NOT NULL,\n" +
                "  BLOCKHASH                       TEXT,\n" +
                "  TO_NAME                         TEXT,\n" +
                "  TO_PHONE_NUMBER                 TEXT,\n" +
                "  NUM_INPUTS                      INTEGER NOT NULL,\n" +
                "  NUM_OUTPUTS                     INTEGER NOT NULL,\n" +
                "  BLOCKHEIGHT                     INTEGER NOT NULL,\n" +
                "  HISTORIC_PRICE                  INTEGER NOT NULL,\n" +
                "  MEM_POOL_STATE                  INTEGER,\n" +
                "  TRANSACTION_NOTIFICATION_ID     INTEGER\n" +
                ")");
        db.execSQL("insert into TRANSACTION_SUMMARY\n" +
                "select _id,\n" +
                "       TXID,\n" +
                "       TRANSACTIONS_INVITES_SUMMARY_ID,\n" +
                "       SOUGHT_NOTIFICATION,\n" +
                "       WALLET_ID,\n" +
                "       FEE,\n" +
                "       TX_TIME,\n" +
                "       NUM_CONFIRMATIONS,\n" +
                "       BLOCKHASH,\n" +
                "       TO_NAME,\n" +
                "       TO_PHONE_NUMBER,\n" +
                "       NUM_INPUTS,\n" +
                "       NUM_OUTPUTS,\n" +
                "       BLOCKHEIGHT,\n" +
                "       HISTORIC_PRICE,\n" +
                "       MEM_POOL_STATE,\n" +
                "       TRANSACTION_NOTIFICATION_ID\n" +
                "from TEMP_TRANSACTION_SUMMARY");
        db.execSQL("drop table TEMP_TRANSACTION_SUMMARY");
    }

    @Nullable
    @Override
    public Migration getPreviousMigration() {
        return new Migrate_V25_to_V26();
    }

    @Override
    public int getTargetVersion() {
        return 26;
    }

    @Override
    public int getMigratedVersion() {
        return 27;
    }
}
