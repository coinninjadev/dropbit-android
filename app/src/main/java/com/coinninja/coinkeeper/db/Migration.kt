package com.coinninja.coinkeeper.db

import org.greenrobot.greendao.database.Database

interface Migration {
    /**
     * Applies migration to provided database
     *
     * @param db
     * @param currentVersion version prior to migration
     * @return version after migration has been applied
     */
    fun runMigration(db: Database, currentVersion: Int): Int

    /**
     * @return null or an instance of current migration
     */
    val previousMigration: Migration?

    val targetVersion: Int
    /**
     * @return the new version that will result
     */
    val migratedVersion: Int
}