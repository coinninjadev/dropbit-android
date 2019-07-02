package com.coinninja.coinkeeper.db.migrations

import com.coinninja.coinkeeper.db.AbstractMigration
import com.coinninja.coinkeeper.db.Migration
import org.greenrobot.greendao.database.Database

class Migrate_V34_to_V35 : AbstractMigration() {

    override fun getMigratedVersion(): Int {
        return 35
    }

    override fun applyMigration(db: Database, currentVersion: Int) {
        dropFeeColumnFromWallet(db)
    }

    private fun dropFeeColumnFromWallet(db: Database) {
        db.execSQL("ALTER TABLE WALLET RENAME TO TEMP_WALLET")

        db.execSQL("CREATE TABLE \"WALLET\" (\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," +
                "\"HD_INDEX\" INTEGER NOT NULL ,\"USER_ID\" INTEGER,\"LAST_SYNC\" INTEGER NOT NULL ," +
                "\"INTERNAL_INDEX\" INTEGER NOT NULL ,\"EXTERNAL_INDEX\" INTEGER NOT NULL ," +
                "\"BALANCE\" INTEGER NOT NULL ,\"SPENDABLE_BALANCE\" INTEGER NOT NULL ," +
                "\"BLOCK_TIP\" INTEGER NOT NULL ,\"LAST_USDPRICE\" INTEGER NOT NULL)")

        db.execSQL("INSERT INTO \"WALLET\" \n" +
                "(" +
                "  \"_id\", " +
                "  \"HD_INDEX\", " +
                "  \"USER_ID\", " +
                "  \"LAST_SYNC\", " +
                "  \"INTERNAL_INDEX\", " +
                "  \"EXTERNAL_INDEX\", " +
                "  \"BALANCE\", " +
                "  \"SPENDABLE_BALANCE\", " +
                "  \"BLOCK_TIP\", " +
                "  \"LAST_USDPRICE\"" +
                ") \n" +
                "SELECT " +
                "  \"_id\", " +
                "  \"HD_INDEX\", " +
                "  \"USER_ID\", " +
                "  \"LAST_SYNC\", " +
                "  \"INTERNAL_INDEX\", " +
                "  \"EXTERNAL_INDEX\", " +
                "  \"BALANCE\", " +
                "  \"SPENDABLE_BALANCE\", " +
                "  \"BLOCK_TIP\", " +
                "  \"LAST_USDPRICE\"" +
                "FROM TEMP_WALLET;")

        db.execSQL("drop table TEMP_WALLET")
    }

    override fun getTargetVersion(): Int {
        return 34
    }

    override fun getPreviousMigration(): Migration {
        return Migrate_V33_to_V34()
    }
}