package com.coinninja.coinkeeper.db.migrations;

import com.coinninja.coinkeeper.db.AbstractMigration;
import com.coinninja.coinkeeper.db.Migration;

import org.greenrobot.greendao.database.Database;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Migrate_V13_to_V14 extends AbstractMigration {
    @Override
    protected void applyMigration(@NonNull Database db, int currentVersion) {
        alterDatabase(db);
    }

    @Nullable
    @Override
    public Migration getPreviousMigration() {
        return new Migrate_V12_to_V13();
    }

    @Override
    public int getTargetVersion() {
        return 13;
    }

    @Override
    public int getMigratedVersion() {
        return 14;
    }

    private void alterDatabase(Database db) {
        db.execSQL("ALTER TABLE ACCOUNT ADD COLUMN CN_USER_ID TEXT");
        db.execSQL("ALTER TABLE ACCOUNT ADD COLUMN CN_USER_ID_CACHED TEXT");
        db.execSQL("ALTER TABLE ACCOUNT ADD COLUMN CN_PHONE_NUMBER_HASH_CACHED TEXT");
    }
}
