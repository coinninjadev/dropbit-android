package com.coinninja.coinkeeper.db.migrations;

import com.coinninja.coinkeeper.db.AbstractMigration;
import com.coinninja.coinkeeper.db.Migration;

import org.greenrobot.greendao.database.Database;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Migrate_V14_to_V15 extends AbstractMigration {
    @Override
    protected void applyMigration(@NonNull Database db, int currentVersion) {
        alterDatabase(db);
    }

    @Nullable
    @Override
    public Migration getPreviousMigration() {
        return new Migrate_V13_to_V14();
    }

    @Override
    public int getTargetVersion() {
        return 14;
    }

    @Override
    public int getMigratedVersion() {
        return 15;
    }

    private void alterDatabase(Database db) {
        db.execSQL("ALTER TABLE USER ADD COLUMN COMPLETED_TRAINING INTEGER");
    }
}
