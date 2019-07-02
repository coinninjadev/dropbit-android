package com.coinninja.coinkeeper.db.migrations

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.coinkeeper.TestCoinKeeperApplication
import com.coinninja.coinkeeper.db.TestOpenHelper
import com.coinninja.coinkeeper.model.db.DaoMaster
import com.coinninja.coinkeeper.model.helpers.DaoSessionManager
import org.greenrobot.greendao.database.Database
import org.hamcrest.Matchers.equalTo
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Migrate_V34_to_V35Test {

    @Before
    fun setUp() {
    }

    @After
    fun tearDown() {
        ApplicationProvider.getApplicationContext<TestCoinKeeperApplication>().deleteDatabase(TestOpenHelper.dbName)
    }

    private fun prepareWalletForMigration(db: Database) {
        db.execSQL("INSERT INTO WALLET (\"_id\", " +
                "\"HD_INDEX\", \"USER_ID\", \"LAST_SYNC\", \"INTERNAL_INDEX\", \"EXTERNAL_INDEX\", " +
                "\"BALANCE\", \"SPENDABLE_BALANCE\", \"BLOCK_TIP\", \"LAST_USDPRICE\", \"LAST_FEE\") " +
                "VALUES (1, 5, 4, 3, 8, 2, 26, 17, 14, 23, \"10.0\")"
        )
    }

    private fun prepare(db: Database) {
        prepareWalletForMigration(db)
    }

    @Test
    fun `migrates data`() {
        val helper = TestOpenHelper(ApplicationProvider.getApplicationContext(), TestOpenHelper.dbName)
        val db = helper.writableV31Db
        prepare(db)

        Migrate_V34_to_V35().runMigration(db, 34)

        val daoSessionManager = DaoSessionManager(DaoMaster(db)).connect()
        verifyWallet(daoSessionManager)

        var cursor = db.rawQuery("select * from sqlite_master where tbl_name = \"TEMP_WALLET\"", null)
        assertThat(cursor.count, equalTo(0))
        cursor.close()

        db.close()
    }

    private fun verifyWallet(daoSessionManager: DaoSessionManager) {
        val all = daoSessionManager.walletDao.loadAll()

        var wallet = all.get(0)
        assertEquals(wallet.id, 1)
        assertEquals(wallet.hdIndex, 5)
        assertEquals(wallet.userId, 4)
        assertEquals(wallet.lastSync, 3)
        assertEquals(wallet.internalIndex, 8)
        assertEquals(wallet.externalIndex, 2)
        assertEquals(wallet.balance, 26)
        assertEquals(wallet.spendableBalance, 17)
        assertEquals(wallet.blockTip, 14)
        assertEquals(wallet.lastUSDPrice, 23)
    }

}