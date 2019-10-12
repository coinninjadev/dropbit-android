package app.coinninja.cn.persistance.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class MIGRATION_1_2 constructor(from: Int, to: Int) : Migration(from, to) {
    override fun migrate(db: SupportSQLiteDatabase) {
    }

    companion object {
        val MIGRATION_1_2: MIGRATION_1_2 get() = MIGRATION_1_2(1, 2)
    }
}