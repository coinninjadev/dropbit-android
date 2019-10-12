package com.coinninja.coinkeeper.db.migrations

import com.coinninja.coinkeeper.db.AbstractMigration
import com.coinninja.coinkeeper.db.Migration
import org.greenrobot.greendao.database.Database

class Migrate_V35_to_V36 : AbstractMigration() {
    override val previousMigration: Migration = Migrate_V34_to_V35()
    override val migratedVersion: Int = 36
    override val targetVersion: Int = 35

    override fun applyMigration(db: Database, currentVersion: Int) {
        db.execSQL("ALTER TABLE WALLET RENAME TO TEMP_WALLET")

        db.execSQL("CREATE TABLE \"WALLET\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "\"HD_INDEX\" INTEGER NOT NULL ," + // 1: hdIndex
                "\"USER_ID\" INTEGER," + // 2: userId
                "\"LAST_SYNC\" INTEGER NOT NULL ," + // 3: lastSync
                "\"INTERNAL_INDEX\" INTEGER NOT NULL ," + // 4: internalIndex
                "\"EXTERNAL_INDEX\" INTEGER NOT NULL ," + // 5: externalIndex
                "\"BALANCE\" INTEGER NOT NULL ," + // 6: balance
                "\"SPENDABLE_BALANCE\" INTEGER NOT NULL ," + // 7: spendableBalance
                "\"BLOCK_TIP\" INTEGER NOT NULL ," + // 8: blockTip
                "\"LAST_USDPRICE\" INTEGER NOT NULL ," + // 9: lastUSDPrice
                "\"PURPOSE\" INTEGER NOT NULL ," + // 10: purpose
                "\"COIN_TYPE\" INTEGER NOT NULL ," + // 11: coinType
                "\"ACCOUNT_INDEX\" INTEGER NOT NULL );"); // 12: accountIndex

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
                "  \"LAST_USDPRICE\", " +
                "  \"PURPOSE\", " +
                "  \"COIN_TYPE\", " +
                "  \"ACCOUNT_INDEX\"" +
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
                "  \"LAST_USDPRICE\"," +
                "  49, 0, 0 " +
                "FROM TEMP_WALLET;")

        db.execSQL("drop table TEMP_WALLET")
    }


}
