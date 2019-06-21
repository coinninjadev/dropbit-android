package com.coinninja.coinkeeper.model.db

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.coinkeeper.TestCoinKeeperApplication
import com.coinninja.coinkeeper.db.TestOpenHelper
import com.coinninja.coinkeeper.model.db.enums.IdentityType
import com.coinninja.coinkeeper.model.helpers.DaoSessionManager
import org.greenrobot.greendao.database.Database
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserIdentityTest {

    private fun getWritableDB(): Database {
        val helper = TestOpenHelper(ApplicationProvider.getApplicationContext(), TestOpenHelper.dbName)
        return helper.writableDb
    }

    @After
    fun tearDown() {
        val db = getWritableDB()
        val daoSessionManager = DaoSessionManager(DaoMaster(db)).connect()
        daoSessionManager.resetAll()
        db.close()
        ApplicationProvider.getApplicationContext<TestCoinKeeperApplication>().deleteDatabase(TestOpenHelper.dbName)
    }

    @Test
    fun `provides display name when type is phone and display name exists`() {
        val userIdentity = UserIdentity()
        userIdentity.type = IdentityType.PHONE
        userIdentity.identity = "+13305551111"

        assertThat(userIdentity.localeFriendlyDisplayIdentityText, equalTo("(330) 555-1111"))
    }

    @Test
    fun `provides formatted phone number when type is phone and display name null`() {
        val userIdentity = UserIdentity()
        userIdentity.type = IdentityType.PHONE
        userIdentity.identity = "+13305551111"
        userIdentity.displayName = "Some Body"

        assertThat(userIdentity.localeFriendlyDisplayIdentityText, equalTo("Some Body"))
    }

    @Test
    fun `provides handle when type is twitter`() {
        val userIdentity = UserIdentity()
        userIdentity.type = IdentityType.TWITTER
        userIdentity.identity = "1234567890"
        userIdentity.displayName = "Some Body"
        userIdentity.handle = "SomeBody14"

        assertThat(userIdentity.localeFriendlyDisplayIdentityText, equalTo("@SomeBody14"))
    }
}