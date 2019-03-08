package com.coinninja.coinkeeper.db.migrations;

import com.coinninja.coinkeeper.db.AbstractMigration;
import com.coinninja.coinkeeper.db.Migration;

import org.greenrobot.greendao.database.Database;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Migrate_V17_to_V18 extends AbstractMigration {

    @Override
    protected void applyMigration(@NonNull Database db, int currentVersion) {
        String sql = "DROP TABLE \"CONTACT_ADDRESS\"";
        db.execSQL(sql);
    }

    @Nullable
    @Override
    public Migration getPreviousMigration() {
        return new Migrate_V16_to_V17();
    }

    @Override
    public int getTargetVersion() {
        return 17;
    }

    @Override
    public int getMigratedVersion() {
        return 18;
    }
}
