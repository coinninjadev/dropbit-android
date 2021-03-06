package com.coinninja.coinkeeper.db.migrations;

import com.coinninja.coinkeeper.db.AbstractMigration;
import com.coinninja.coinkeeper.db.Migration;

import org.greenrobot.greendao.database.Database;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Migrate_V24_to_V25 extends AbstractMigration {

    @Override
    protected void applyMigration(@NonNull Database db, int currentVersion) {
        db.execSQL("ALTER TABLE INVITE_TRANSACTION_SUMMARY ADD COLUMN \"PUBKEY\" TEXT");
    }

    @Nullable
    @Override
    public Migration getPreviousMigration() {
        return new Migrate_V23_to_V24();
    }

    @Override
    public int getTargetVersion() {
        return 24;
    }

    @Override
    public int getMigratedVersion() {
        return 25;
    }
}
