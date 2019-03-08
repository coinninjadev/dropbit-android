package com.coinninja.coinkeeper.db.migrations;

import com.coinninja.coinkeeper.db.AbstractMigration;
import com.coinninja.coinkeeper.db.Migration;

import org.greenrobot.greendao.database.Database;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Migrate_V19_to_V20 extends AbstractMigration {
    @Override
    protected void applyMigration(@NonNull Database db, int currentVersion) {
        db.execSQL("ALTER TABLE TRANSACTION_SUMMARY ADD COLUMN \"HISTORIC_PRICE\" INTEGER");
        db.execSQL("UPDATE TRANSACTION_SUMMARY SET HISTORIC_PRICE=0");
    }

    @Nullable
    @Override
    public Migration getPreviousMigration() {
        return new Migrate_V18_to_V19();
    }

    @Override
    public int getTargetVersion() {
        return 19;
    }

    @Override
    public int getMigratedVersion() {
        return 20;
    }
}
