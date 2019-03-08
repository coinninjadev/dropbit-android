package com.coinninja.coinkeeper.db.migrations;

import com.coinninja.coinkeeper.db.AbstractMigration;
import com.coinninja.coinkeeper.db.Migration;

import org.greenrobot.greendao.database.Database;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Migrate_V22_to_V23 extends AbstractMigration {
    @Override
    protected void applyMigration(@NonNull Database db, int currentVersion) {
        db.execSQL("ALTER TABLE TRANSACTION_NOTIFICATION ADD COLUMN \"PHONE_NUMBER\" TEXT");
    }

    @Nullable
    @Override
    public Migration getPreviousMigration() {
        return new Migrate_V21_to_V22();

    }

    @Override
    public int getTargetVersion() {
        return 22;
    }

    @Override
    public int getMigratedVersion() {
        return 23;
    }
}
