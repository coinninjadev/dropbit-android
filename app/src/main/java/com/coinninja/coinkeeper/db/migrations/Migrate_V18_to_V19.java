package com.coinninja.coinkeeper.db.migrations;

import com.coinninja.coinkeeper.db.AbstractMigration;
import com.coinninja.coinkeeper.db.Migration;

import org.greenrobot.greendao.database.Database;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Migrate_V18_to_V19 extends AbstractMigration {
    @Override
    protected void applyMigration(@NonNull Database db, int currentVersion) {
        // Bye -- removed in v21 in impl of shared memos
    }

    @Nullable
    @Override
    public Migration getPreviousMigration() {
        return new Migrate_V17_to_V18();
    }

    @Override
    public int getTargetVersion() {
        return 18;
    }

    @Override
    public int getMigratedVersion() {
        return 19;
    }
}
