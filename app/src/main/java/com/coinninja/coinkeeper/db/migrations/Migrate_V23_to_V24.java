package com.coinninja.coinkeeper.db.migrations;

import com.coinninja.coinkeeper.db.AbstractMigration;
import com.coinninja.coinkeeper.db.Migration;

import org.greenrobot.greendao.database.Database;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Migrate_V23_to_V24 extends AbstractMigration {
    @Override
    protected void applyMigration(@NonNull Database db, int currentVersion) {
        db.execSQL("update ACCOUNT set PHONE_NUMBER= \"+1\" ||  PHONE_NUMBER;");
    }

    @Nullable
    @Override
    public Migration getPreviousMigration() {
        return new Migrate_V22_to_V23();
    }

    @Override
    public int getTargetVersion() {
        return 23;
    }

    @Override
    public int getMigratedVersion() {
        return 24;
    }
}
