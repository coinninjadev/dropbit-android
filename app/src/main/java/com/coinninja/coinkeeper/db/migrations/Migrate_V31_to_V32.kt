package com.coinninja.coinkeeper.db.migrations

import com.coinninja.coinkeeper.db.AbstractMigration
import com.coinninja.coinkeeper.db.Migration
import org.greenrobot.greendao.database.Database

class Migrate_V31_to_V32 : AbstractMigration() {
    override val migratedVersion: Int = 32
    override val targetVersion: Int = 31
    override val previousMigration: Migration? = Migrate_V30_to_V31()

    override fun applyMigration(db: Database, currentVersion: Int) {
        db.execSQL("ALTER TABLE DROPBIT_ME_IDENTITY ADD COLUMN \"SERVER_ID\" TEXT")
    }


}