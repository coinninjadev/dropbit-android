package com.coinninja.coinkeeper.model.db

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.coinkeeper.TestCoinKeeperApplication
import com.coinninja.coinkeeper.cn.wallet.WalletConfiguration
import com.coinninja.coinkeeper.db.TestOpenHelper
import com.coinninja.coinkeeper.model.db.enums.IdentityType
import com.coinninja.coinkeeper.model.helpers.DaoSessionManager
import com.coinninja.coinkeeper.model.helpers.UserIdentityHelper
import org.greenrobot.greendao.database.Database
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TransactionsInvitesSummaryTest {

    private fun getWritableDB(): Database {
        val helper = TestOpenHelper(ApplicationProvider.getApplicationContext(), TestOpenHelper.dbName)
        return helper.writableDb
    }

    @After
    fun tearDown() {
        val db = getWritableDB()
        val daoSessionManager = DaoSessionManager(DaoMaster(db), WalletConfiguration(49, 0, 0, false)).connect()
        daoSessionManager.resetAll()
        db.close()
        ApplicationProvider.getApplicationContext<TestCoinKeeperApplication>().deleteDatabase(TestOpenHelper.dbName)
    }

    @Test
    fun `returns locale friendly identity string`() {
        val db = getWritableDB()
        val daoSessionManager = DaoSessionManager(DaoMaster(db), WalletConfiguration(49, 0, 0, false)).connect()
        val invitesSummary = TransactionsInvitesSummary()
        daoSessionManager.insert(invitesSummary)
        val toUser = UserIdentityHelper(daoSessionManager)
                .getOrCreate(IdentityType.PHONE, "+13305551111")

        invitesSummary.toUser = toUser
        invitesSummary.update()

        assertThat(invitesSummary.localeFriendlyDisplayIdentityForReceiver,
                equalTo("(330) 555-1111"))

        db.close()
    }


}