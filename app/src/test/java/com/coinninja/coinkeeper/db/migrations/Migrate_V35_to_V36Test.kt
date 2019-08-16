package com.coinninja.coinkeeper.db.migrations

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.coinkeeper.TestCoinKeeperApplication
import com.coinninja.coinkeeper.db.TestOpenHelper
import com.coinninja.coinkeeper.model.db.DaoMaster
import com.coinninja.coinkeeper.model.db.WalletDao
import com.coinninja.coinkeeper.model.helpers.DaoSessionManager
import com.google.common.truth.Truth.assertThat
import org.greenrobot.greendao.database.Database
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Migrate_V35_to_V36Test {

    @Before
    fun setUp() {
    }

    @After
    fun tearDown() {
        ApplicationProvider.getApplicationContext<TestCoinKeeperApplication>().deleteDatabase(TestOpenHelper.dbName)
    }

    private fun prepareWalletForMigration(db: Database) {
        db.execSQL("INSERT INTO WALLET (\"_id\", \"HD_INDEX\", \"USER_ID\", " +
                "\"LAST_SYNC\", \"INTERNAL_INDEX\", \"EXTERNAL_INDEX\", \"BALANCE\", " +
                "\"SPENDABLE_BALANCE\", \"BLOCK_TIP\", \"LAST_USDPRICE\") " +
                "VALUES (1, 5, 4, 3, 8, 2, 26, 17, 14, 23)"
        )
    }

    @Test
    fun configuration() {
        val migration = Migrate_V35_to_V36()
        assertThat(migration.targetVersion).isEqualTo(35)
        assertThat(migration.migratedVersion).isEqualTo(36)
        assertThat(migration.previousMigration).isInstanceOf(Migrate_V34_to_V35::class.java)
    }

    @Test
    fun migrates_data() {
        val helper = TestOpenHelper(ApplicationProvider.getApplicationContext(), TestOpenHelper.dbName)
        val db = helper.writableV31Db
        prepareWalletForMigration(db)

        Migrate_V35_to_V36().runMigration(db, 34)

        val daoSessionManager = DaoSessionManager(DaoMaster(db)).connect()
        verifyWallet(daoSessionManager)

        val cursor = db.rawQuery("select * from sqlite_master where tbl_name = \"TEMP_WALLET\"", null)
        assertThat(cursor.count).isEqualTo(0)
        cursor.close()

        db.close()
    }

    private fun verifyWallet(daoSessionManager: DaoSessionManager) {
        val cursor = daoSessionManager.daoSession.database.rawQuery("select * from wallet", null)
        cursor.moveToFirst()

        assertThat(cursor.getLong(cursor.getColumnIndex(WalletDao.Properties.Id.columnName))).isEqualTo(1L)
        assertThat(cursor.getInt(cursor.getColumnIndex(WalletDao.Properties.HdIndex.columnName))).isEqualTo(5)
        assertThat(cursor.getLong(cursor.getColumnIndex(WalletDao.Properties.UserId.columnName))).isEqualTo(4)
        assertThat(cursor.getLong(cursor.getColumnIndex(WalletDao.Properties.LastSync.columnName))).isEqualTo(3)
        assertThat(cursor.getInt(cursor.getColumnIndex(WalletDao.Properties.InternalIndex.columnName))).isEqualTo(8)
        assertThat(cursor.getInt(cursor.getColumnIndex(WalletDao.Properties.ExternalIndex.columnName))).isEqualTo(2)
        assertThat(cursor.getLong(cursor.getColumnIndex(WalletDao.Properties.Balance.columnName))).isEqualTo(26)
        assertThat(cursor.getLong(cursor.getColumnIndex(WalletDao.Properties.SpendableBalance.columnName))).isEqualTo(17)
        assertThat(cursor.getInt(cursor.getColumnIndex(WalletDao.Properties.BlockTip.columnName))).isEqualTo(14)
        assertThat(cursor.getLong(cursor.getColumnIndex(WalletDao.Properties.LastUSDPrice.columnName))).isEqualTo(23)
        assertThat(cursor.getInt(cursor.getColumnIndex(WalletDao.Properties.Purpose.columnName))).isEqualTo(49)
        assertThat(cursor.getInt(cursor.getColumnIndex(WalletDao.Properties.CoinType.columnName))).isEqualTo(0)
        assertThat(cursor.getInt(cursor.getColumnIndex(WalletDao.Properties.AccountIndex.columnName))).isEqualTo(0)

        cursor.close()
    }

}