package com.coinninja.coinkeeper.db.migrations

import com.coinninja.coinkeeper.db.AbstractMigration
import com.coinninja.coinkeeper.db.Migration
import org.greenrobot.greendao.database.Database

class Migrate_V32_to_V33 : AbstractMigration() {
    override fun getMigratedVersion(): Int {
        return 33
    }

    override fun applyMigration(db: Database, currentVersion: Int) {
        db.execSQL("ALTER TABLE DROPBIT_ME_IDENTITY ADD COLUMN \"STATUS\" INTEGER")
    }

    override fun getTargetVersion(): Int {
        return 32
    }

    override fun getPreviousMigration(): Migration {
        return Migrate_V31_to_V32()
    }

}