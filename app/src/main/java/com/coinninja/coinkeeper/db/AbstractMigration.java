package com.coinninja.coinkeeper.db;

import android.annotation.SuppressLint;

import org.greenrobot.greendao.database.Database;

import androidx.annotation.NonNull;

import static androidx.core.util.Preconditions.checkNotNull;

public abstract class AbstractMigration implements Migration {

    protected abstract void applyMigration(@NonNull Database db, int currentVersion);

    @Override
    public final int runMigration(@NonNull Database db, int currentVersion) {
        prepareMigration(db, currentVersion);
        applyMigration(db, currentVersion);
        return getMigratedVersion();
    }

    /**
     * A helper method which helps the migration prepare by passing the call up the chain.
     *
     * @param db
     * @param currentVersion
     */
    @SuppressLint("RestrictedApi")
    private void prepareMigration(@NonNull Database db, int currentVersion) {
        checkNotNull(db, "Database cannot be null");
        if (currentVersion < 1) {
            throw new IllegalArgumentException(
                    "Lowest suported schema version is 1, unable to prepare for migration from version: "
                            + currentVersion);
        }

        if (currentVersion < getTargetVersion()) {
            Migration previousMigration = getPreviousMigration();

            if (previousMigration == null) {
                // This is the first migration
                if (currentVersion != getTargetVersion()) {
                    throw new IllegalStateException(
                            "Unable to apply migration as Version: "
                                    + currentVersion
                                    + " is not suitable for this Migration.");
                }
            }

            if (previousMigration.runMigration(db, currentVersion) != getTargetVersion()) {
                // For all other migrations ensure that after the earlier
                // migration has been applied the expected version matches
                throw new IllegalStateException(
                        "Error, expected migration parent to update database to appropriate version");
            }
        }
    }
}
