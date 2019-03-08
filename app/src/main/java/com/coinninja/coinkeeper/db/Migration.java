package com.coinninja.coinkeeper.db;

import org.greenrobot.greendao.database.Database;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface Migration {
    /**
     * Applies migration to provided database
     *
     * @param db
     * @param currentVersion version prior to migration
     * @return version after migration has been applied
     */
    int runMigration(@NonNull Database db, int currentVersion);

    /**
     * @return null or an instance of current migration
     */
    @Nullable
    Migration getPreviousMigration();

    int getTargetVersion();

    /**
     * @return the new version that will result
     */
    int getMigratedVersion();
}
