package com.coinninja.coinkeeper.db.migrations;

import com.coinninja.coinkeeper.db.AbstractMigration;
import com.coinninja.coinkeeper.db.Migration;

import org.greenrobot.greendao.database.Database;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Migrate_V25_to_V26 extends AbstractMigration {
    @Override
    protected void applyMigration(@NonNull Database db, int currentVersion) {
        db.execSQL("ALTER TABLE TRANSACTION_NOTIFICATION ADD COLUMN \"TXID\" TEXT");
    }

    @Nullable
    @Override
    public Migration getPreviousMigration() {
        return new Migrate_V24_to_V25();
    }

    @Override
    public int getTargetVersion() {
        return 25;
    }

    @Override
    public int getMigratedVersion() {
        return 26;
    }
}
