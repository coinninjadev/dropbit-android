package com.coinninja.coinkeeper.db.migrations;

import com.coinninja.coinkeeper.db.AbstractMigration;
import com.coinninja.coinkeeper.db.Migration;
import com.coinninja.coinkeeper.model.db.DaoMaster;
import com.coinninja.coinkeeper.model.db.DaoSession;

import org.greenrobot.greendao.database.Database;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Migrate_V11_to_V12 extends AbstractMigration {
    @Override
    protected void applyMigration(@NonNull Database db, int currentVersion) {
        onUpdate(db);
    }

    @Nullable
    @Override
    public Migration getPreviousMigration() {
        return new Migrate_V10_to_V11();
    }

    @Override
    public int getTargetVersion() {
        return 11;
    }

    @Override
    public int getMigratedVersion() {
        return 12;
    }

    private void onUpdate(Database db) {
        DaoSession daoSession = new DaoMaster(db).newSession();
        daoSession.getAddressDao().deleteAll();
    }
}
