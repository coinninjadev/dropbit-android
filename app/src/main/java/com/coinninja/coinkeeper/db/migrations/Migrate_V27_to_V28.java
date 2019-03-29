package com.coinninja.coinkeeper.db.migrations;

import android.database.Cursor;

import com.coinninja.coinkeeper.db.AbstractMigration;
import com.coinninja.coinkeeper.db.Migration;

import org.greenrobot.greendao.database.Database;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Migrate_V27_to_V28 extends AbstractMigration {

    @Override
    protected void applyMigration(@NonNull Database db, int currentVersion) {
        //1. Add two new fields to InviteTransactionSummary
        db.execSQL("ALTER TABLE TRANSACTIONS_INVITES_SUMMARY ADD COLUMN \"TO_NAME\" TEXT");
        db.execSQL("ALTER TABLE TRANSACTIONS_INVITES_SUMMARY ADD COLUMN \"TO_PHONE_NUMBER\" TEXT");

        //2. Populate the new fields above
        populateJoinTableFromInvite(db);
        populateJoinTableFromTransactionIfNecessary(db);

        //3. Rename transaction summary table to temp
        db.execSQL("ALTER TABLE TRANSACTION_SUMMARY RENAME TO TEMP_TRANSACTION_SUMMARY");

        //4. Create new transaction summary table
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
                "  NUM_INPUTS                      INTEGER NOT NULL,\n" +
                "  NUM_OUTPUTS                     INTEGER NOT NULL,\n" +
                "  BLOCKHEIGHT                     INTEGER NOT NULL,\n" +
                "  HISTORIC_PRICE                  INTEGER NOT NULL,\n" +
                "  MEM_POOL_STATE                  INTEGER,\n" +
                "  TRANSACTION_NOTIFICATION_ID     INTEGER\n" +
                ")");

        //5. Convert old transaction summary tables
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
                "       NUM_INPUTS,\n" +
                "       NUM_OUTPUTS,\n" +
                "       BLOCKHEIGHT,\n" +
                "       HISTORIC_PRICE,\n" +
                "       MEM_POOL_STATE,\n" +
                "       TRANSACTION_NOTIFICATION_ID\n" +
                "from TEMP_TRANSACTION_SUMMARY");

        //6. Drop old transaction summary table
        db.execSQL("drop table TEMP_TRANSACTION_SUMMARY");
    }

    private void populateJoinTableFromInvite(@NonNull Database db) {
        Cursor inviteCursor = db.rawQuery("SELECT * FROM INVITE_TRANSACTION_SUMMARY WHERE TYPE = 0", null);

        inviteCursor.moveToFirst();
        while (inviteCursor.moveToNext()) {
            String inviteName = inviteCursor.getString(inviteCursor.getColumnIndex("INVITE_NAME"));
            String toPhoneNumber = inviteCursor.getString(inviteCursor.getColumnIndex("RECEIVER_PHONE_NUMBER"));

            Long id = inviteCursor.getLong(inviteCursor.getColumnIndex("TRANSACTIONS_INVITES_SUMMARY_ID"));
            db.execSQL(String.format("UPDATE TRANSACTIONS_INVITES_SUMMARY SET TO_NAME = %s AND TO_PHONE_NUMBER = %s WHERE _id = %s", inviteName, toPhoneNumber, id));
        }
        inviteCursor.close();
    }

    private void populateJoinTableFromTransactionIfNecessary(@NonNull Database db) {
        Cursor cursor = db.rawQuery("SELECT * FROM TRANSACTION_SUMMARY", null);

        cursor.moveToFirst();
        while (cursor.moveToNext()) {
            String phoneNumber = cursor.getString(cursor.getColumnIndex("TO_PHONE_NUMBER"));
            String toName = cursor.getString(cursor.getColumnIndex("TO_NAME"));

            Long id = cursor.getLong(cursor.getColumnIndex("TRANSACTIONS_INVITES_SUMMARY_ID"));

            if (phoneNumber != null && !phoneNumber.equals("")) {
                db.execSQL(String.format("UPDATE TRANSACTIONS_INVITES_SUMMARY SET TO_PHONE_NUMBER = %s WHERE _id = %s", phoneNumber, id));
            }

            if (toName != null && !toName.equals("")) {
                db.execSQL(String.format("UPDATE TRANSACTIONS_INVITES_SUMMARY SET TO_NAME = %s WHERE _id = %s", toName, id));
            }
        }
        cursor.close();
    }

    @Nullable
    @Override
    public Migration getPreviousMigration() {
        return new Migrate_V26_to_V27();
    }

    @Override
    public int getTargetVersion() {
        return 27;
    }

    @Override
    public int getMigratedVersion() {
        return 28;
    }
}
