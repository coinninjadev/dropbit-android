package com.coinninja.coinkeeper.db

import android.annotation.SuppressLint
import androidx.core.util.Preconditions.checkNotNull
import org.greenrobot.greendao.database.Database

abstract class AbstractMigration : Migration {
    protected abstract fun applyMigration(db: Database, currentVersion: Int)
    override fun runMigration(db: Database, currentVersion: Int): Int {
        prepareMigration(db, currentVersion)
        applyMigration(db, currentVersion)
        return migratedVersion
    }

    /**
     * A helper method which helps the migration prepare by passing the call up the chain.
     *
     * @param db
     * @param currentVersion
     */
    @SuppressLint("RestrictedApi")
    private fun prepareMigration(db: Database, currentVersion: Int) {
        checkNotNull<Database?>(db, "Database cannot be null")
        if (currentVersion < 1) {
            throw IllegalArgumentException("Lowest suported schema version is 1, unable to prepare for migration from version: "
                    + currentVersion)
        }
        if (currentVersion < targetVersion) {
            val previousMigration = previousMigration
            if (previousMigration == null) {
                // This is the first migration

                if (currentVersion != targetVersion) {
                    throw IllegalStateException("Unable to apply migration as Version: "
                            + currentVersion
                            .toString() + " is not suitable for this Migration.")
                }
            }
            if (previousMigration!!.runMigration(db, currentVersion) != targetVersion) {
                // For all other migrations ensure that after the earlier
                // migration has been applied the expected version matches
                throw IllegalStateException(
                        "Error, expected migration parent to update database to appropriate version")
            }
        }
    }
}