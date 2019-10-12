package com.coinninja.coinkeeper.db.migrations

import com.coinninja.coinkeeper.db.AbstractMigration
import com.coinninja.coinkeeper.db.Migration
import org.greenrobot.greendao.database.Database

class Migrate_V32_to_V33 : AbstractMigration() {
    override val migratedVersion: Int = 33
    override val targetVersion: Int = 32
    override val previousMigration: Migration? = Migrate_V31_to_V32()

    override fun applyMigration(db: Database, currentVersion: Int) {
        db.execSQL("ALTER TABLE DROPBIT_ME_IDENTITY ADD COLUMN \"STATUS\" INTEGER")
    }


}