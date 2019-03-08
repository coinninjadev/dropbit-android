package com.coinninja.coinkeeper.db.migrations;

import com.coinninja.coinkeeper.db.AbstractMigration;
import com.coinninja.coinkeeper.db.Migration;

import org.greenrobot.greendao.database.Database;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Migrate_V20_to_V21 extends AbstractMigration {
    @Override
    protected void applyMigration(@NonNull Database db, int currentVersion) {
        db.execSQL("ALTER TABLE TRANSACTION_SUMMARY ADD COLUMN \"TRANSACTION_NOTIFICATION_ID\" INTEGER");
        db.execSQL("ALTER TABLE INVITE_TRANSACTION_SUMMARY ADD COLUMN \"TRANSACTION_NOTIFICATION_ID\" INTEGER");
        db.execSQL("CREATE TABLE IF NOT EXISTS \"TRANSACTION_NOTIFICATION\" (" +
                "\"_id\" INTEGER PRIMARY KEY ," +
                "\"DROPBIT_ME_HANDLE\" TEXT," +
                "\"AVATAR\" TEXT," +
                "\"DISPLAY_NAME\" TEXT," +
                "\"MEMO\" TEXT," +
                "\"IS_SHARED\" INTEGER NOT NULL ," +
                "\"AMOUNT\" INTEGER NOT NULL ," +
                "\"AMOUNT_CURRENCY\" TEXT);");
    }

    @Nullable
    @Override
    public Migration getPreviousMigration() {
        return new Migrate_V19_to_V20();
    }

    @Override
    public int getTargetVersion() {
        return 20;
    }

    @Override
    public int getMigratedVersion() {
        return 21;
    }
}
