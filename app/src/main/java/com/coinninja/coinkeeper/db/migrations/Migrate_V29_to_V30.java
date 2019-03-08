package com.coinninja.coinkeeper.db.migrations;

import android.database.Cursor;

import com.coinninja.coinkeeper.db.AbstractMigration;
import com.coinninja.coinkeeper.db.Migration;

import org.greenrobot.greendao.database.Database;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Migrate_V29_to_V30 extends AbstractMigration {

    @Override
    protected void applyMigration(@NonNull Database db, int currentVersion) {


        // Rename table to temp
        db.execSQL("ALTER TABLE ACCOUNT RENAME TO TEMP_ACCOUNT");

        // Create new table
        db.execSQL("CREATE TABLE \"ACCOUNT\" (" +
                "\"_id\" INTEGER PRIMARY KEY ," +
                "\"WALLET_ID\" INTEGER," +
                "\"CN_WALLET_ID\" TEXT," +
                "\"CN_USER_ID\" TEXT," +
                "\"STATUS\" INTEGER," +
                "\"PHONE_NUMBER_HASH\" TEXT," +
                "\"PHONE_NUMBER\" TEXT," +
                "\"VERIFICATION_TTL\" INTEGER NOT NULL" +
                " )");

        // Convert old records
        db.execSQL("insert into ACCOUNT\n" +
                "select _id,\n" +
                "\"WALLET_ID\", " +
                "\"CN_WALLET_ID\", " +
                "\"CN_USER_ID\", " +
                "\"STATUS\", " +
                "\"PHONE_NUMBER_HASH\", " +
                "\"PHONE_NUMBER\", " +
                "\"VERIFICATION_TTL\" " +
                "from TEMP_ACCOUNT");

        // Drop old table
        db.execSQL("drop table TEMP_ACCOUNT");
    }


    @Nullable
    @Override
    public Migration getPreviousMigration() {
        return new Migrate_V28_to_V29();
    }

    @Override
    public int getTargetVersion() {
        return 29;
    }

    @Override
    public int getMigratedVersion() {
        return 30;
    }
}
