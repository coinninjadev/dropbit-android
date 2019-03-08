package com.coinninja.coinkeeper.db.migrations;

import com.coinninja.coinkeeper.db.AbstractMigration;
import com.coinninja.coinkeeper.db.Migration;

import org.greenrobot.greendao.database.Database;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Migrate_V21_to_V22 extends AbstractMigration {
    @Override
    protected void applyMigration(@NonNull Database db, int currentVersion) {
        db.execSQL("ALTER TABLE TRANSACTION_SUMMARY ADD COLUMN \"SOUGHT_NOTIFICATION\" INTEGER");
        db.execSQL("UPDATE TRANSACTION_SUMMARY set \"SOUGHT_NOTIFICATION\" = 0");
    }

    @Nullable
    @Override
    public Migration getPreviousMigration() {
        return new Migrate_V20_to_V21();

    }

    @Override
    public int getTargetVersion() {
        return 21;
    }

    @Override
    public int getMigratedVersion() {
        return 22;
    }
}
