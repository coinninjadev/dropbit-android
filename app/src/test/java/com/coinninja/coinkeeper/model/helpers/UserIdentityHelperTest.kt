package com.coinninja.coinkeeper.model.helpers

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.coinkeeper.TestCoinKeeperApplication
import com.coinninja.coinkeeper.cn.wallet.WalletConfiguration
import com.coinninja.coinkeeper.db.TestOpenHelper
import com.coinninja.coinkeeper.model.Identity
import com.coinninja.coinkeeper.model.db.DaoMaster
import com.coinninja.coinkeeper.model.db.DropbitMeIdentity
import com.coinninja.coinkeeper.model.db.UserIdentityDao
import com.coinninja.coinkeeper.model.db.enums.IdentityType
import com.coinninja.coinkeeper.service.client.model.MetadataContact
import org.greenrobot.greendao.database.Database
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserIdentityHelperTest {

    private fun getWritableDB(): Database {
        val helper = TestOpenHelper(ApplicationProvider.getApplicationContext(), TestOpenHelper.dbName)
        return helper.writableDb
    }

    private fun userIdentityHelper(daoSessionManager: DaoSessionManager): UserIdentityHelper {
        val userIdentityHelper = UserIdentityHelper(daoSessionManager)
        return userIdentityHelper
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
    fun `gets or creates user identity`() {
        val db = getWritableDB()
        val daoSessionManager = DaoSessionManager(DaoMaster(db), WalletConfiguration(49, 0, 0, false)).connect()
        val uid = daoSessionManager.newUserIdentity()
        uid.type = IdentityType.TWITTER
        uid.identity = "12345667890"
        daoSessionManager.insert(uid)

        val userIdentity = daoSessionManager.newUserIdentity()
        userIdentity.type = IdentityType.PHONE
        userIdentity.identity = "+13305551111"
        val id2 = daoSessionManager.insert(userIdentity)

        val userIdentityHelper = userIdentityHelper(daoSessionManager)

        val fetchedUserIdentity = userIdentityHelper.getOrCreate(IdentityType.PHONE, userIdentity.identity)

        assertThat(fetchedUserIdentity.id, equalTo(id2))
        assertThat(fetchedUserIdentity.type, equalTo(IdentityType.PHONE))
        assertThat(fetchedUserIdentity.identity, equalTo(userIdentity.identity))
        db.close()
    }

    @Test
    fun `gets or creates user identity from type and identity string`() {
        val db = getWritableDB()
        val daoSessionManager = DaoSessionManager(DaoMaster(db), WalletConfiguration(49, 0, 0, false)).connect()
        val userIdentityHelper = userIdentityHelper(daoSessionManager)

        val fetchedUserIdentity = userIdentityHelper.getOrCreate(IdentityType.PHONE, "+13305551111")

        assertThat(fetchedUserIdentity.id, equalTo(1L))
        assertThat(fetchedUserIdentity.type, equalTo(IdentityType.PHONE))
        assertThat(fetchedUserIdentity.identity, equalTo("+13305551111"))

        assertThat(daoSessionManager.userIdentityDao.queryBuilder().where(UserIdentityDao.Properties.Id.eq(1L)).unique().identity,
                equalTo("+13305551111"))
        db.close()
    }

    @Test
    fun `updates identity with values`() {
        val db = getWritableDB()
        val daoSessionManager = DaoSessionManager(DaoMaster(db), WalletConfiguration(49, 0, 0, false)).connect()
        val userIdentityHelper = userIdentityHelper(daoSessionManager)
        val identity = Identity(IdentityType.PHONE, "+13305551111", "--hash--", "Some Name")
        userIdentityHelper.getOrCreate(identity.identityType, identity.value)

        userIdentityHelper.updateFrom(identity)

        val userIdentity = daoSessionManager.userIdentityDao.queryBuilder().where(UserIdentityDao.Properties.Id.eq(1L)).unique()
        assertThat(userIdentity.type, equalTo(identity.identityType))
        assertThat(userIdentity.identity, equalTo(identity.value))
        assertThat(userIdentity.hash, equalTo(identity.hash))
        assertThat(userIdentity.displayName, equalTo(identity.displayName))
        db.close()
    }

    @Test
    fun `creates TWITER identity when does not exist during update`() {
        val db = getWritableDB()
        val daoSessionManager = DaoSessionManager(DaoMaster(db), WalletConfiguration(49, 0, 0, false)).connect()
        val userIdentityHelper = userIdentityHelper(daoSessionManager)
        val identity = Identity(IdentityType.TWITTER, "1233321", displayName = "Some Name", handle = "@Handle")

        userIdentityHelper.updateFrom(identity)

        val userIdentity = daoSessionManager.userIdentityDao.queryBuilder().where(UserIdentityDao.Properties.Id.eq(1L)).unique()
        assertThat(userIdentity.type, equalTo(identity.identityType))
        assertThat(userIdentity.identity, equalTo(identity.value))
        assertThat(userIdentity.hash, equalTo("1233321"))
        assertThat(userIdentity.displayName, equalTo(identity.displayName))
        assertThat(userIdentity.handle, equalTo("Handle"))
        db.close()
    }

    @Test
    fun `creates PHONE identity when does not exist during update`() {
        val db = getWritableDB()
        val daoSessionManager = DaoSessionManager(DaoMaster(db), WalletConfiguration(49, 0, 0, false)).connect()
        val userIdentityHelper = userIdentityHelper(daoSessionManager)
        val identity = Identity(IdentityType.PHONE, "+13305551111", null, "Some Name")

        userIdentityHelper.updateFrom(identity)

        val userIdentity = daoSessionManager.userIdentityDao.queryBuilder().where(UserIdentityDao.Properties.Id.eq(1L)).unique()
        assertThat(userIdentity.type, equalTo(identity.identityType))
        assertThat(userIdentity.identity, equalTo(identity.value))
        assertThat(userIdentity.hash, equalTo("710c3ec37d3bbab4d9b140656ea8ab28d14bad091e12b912dc73d0fbcd78664d"))
        assertThat(userIdentity.displayName, equalTo(identity.displayName))
        db.close()
    }

    @Test
    fun `creates identity from dropbit me identity during update PHONE`() {
        val db = getWritableDB()
        val daoSessionManager = DaoSessionManager(DaoMaster(db), WalletConfiguration(49, 0, 0, false)).connect()
        val userIdentityHelper = userIdentityHelper(daoSessionManager)
        val dropbitMeIdentity = DropbitMeIdentity()
        dropbitMeIdentity.type = IdentityType.PHONE
        dropbitMeIdentity.identity = "+13305551111"
        dropbitMeIdentity.hash = "--hash--"
        dropbitMeIdentity.handle = ""

        userIdentityHelper.updateFrom(dropbitMeIdentity)

        val userIdentity = daoSessionManager.userIdentityDao.queryBuilder().where(UserIdentityDao.Properties.Id.eq(1L)).unique()
        assertThat(userIdentity.type, equalTo(dropbitMeIdentity.type))
        assertThat(userIdentity.identity, equalTo(dropbitMeIdentity.identity))
        assertThat(userIdentity.hash, equalTo(dropbitMeIdentity.hash))
        db.close()
    }

    @Test
    fun `creates identity from dropbit me identity during update TWITTER`() {
        val db = getWritableDB()
        val daoSessionManager = DaoSessionManager(DaoMaster(db), WalletConfiguration(49, 0, 0, false)).connect()
        val userIdentityHelper = userIdentityHelper(daoSessionManager)
        val dropbitMeIdentity = DropbitMeIdentity()
        dropbitMeIdentity.type = IdentityType.TWITTER
        dropbitMeIdentity.identity = "12345678909"
        dropbitMeIdentity.hash = "--hash--"
        dropbitMeIdentity.handle = "Handle"

        userIdentityHelper.updateFrom(dropbitMeIdentity)

        val userIdentity = daoSessionManager.userIdentityDao.queryBuilder().where(UserIdentityDao.Properties.Id.eq(1L)).unique()
        assertThat(userIdentity.type, equalTo(dropbitMeIdentity.type))
        assertThat(userIdentity.identity, equalTo(dropbitMeIdentity.identity))
        assertThat(userIdentity.hash, equalTo(dropbitMeIdentity.hash))
        assertThat(userIdentity.handle, equalTo(dropbitMeIdentity.handle))
        db.close()
    }

    @Test
    fun `creates identity from InviteMetadata MetaDataContact`() {
        val db = getWritableDB()
        val daoSessionManager = DaoSessionManager(DaoMaster(db), WalletConfiguration(49, 0, 0, false)).connect()
        val userIdentityHelper = userIdentityHelper(daoSessionManager)
        val contact = MetadataContact("twitter", "12345678990", "@myHandle")
        val contact2 = MetadataContact("phone", "13305551111", "")

        val identity = userIdentityHelper.updateFrom(contact)
        assertThat(identity.type, equalTo(IdentityType.TWITTER))
        assertThat(identity.identity, equalTo(contact.identity))
        assertThat(identity.handle, equalTo("myHandle"))
        assertThat(identity.hash, equalTo(contact.identity))

        val identity2 = userIdentityHelper.updateFrom(contact2)
        assertThat(identity2.type, equalTo(IdentityType.PHONE))
        assertThat(identity2.identity, equalTo("+13305551111"))
        assertThat(identity2.hash, equalTo("710c3ec37d3bbab4d9b140656ea8ab28d14bad091e12b912dc73d0fbcd78664d"))
        assertThat(identity2.handle, equalTo(""))
        db.close()
    }

    @Test
    fun `returns phone identities that do not have names`() {
        val db = getWritableDB()
        val daoSessionManager = DaoSessionManager(DaoMaster(db), WalletConfiguration(49, 0, 0, false)).connect()
        val userIdentityHelper = userIdentityHelper(daoSessionManager)
        val user1 = daoSessionManager.newUserIdentity()
        user1.identity = "+3305551111"
        user1.type = IdentityType.PHONE
        daoSessionManager.insert(user1)

        val user2 = daoSessionManager.newUserIdentity()
        user2.identity = "+3305550000"
        user2.type = IdentityType.PHONE
        user2.displayName = ""
        daoSessionManager.insert(user2)

        val user3 = daoSessionManager.newUserIdentity()
        user3.identity = "+3305551122"
        user3.type = IdentityType.PHONE
        user3.displayName = "Some Body"
        daoSessionManager.insert(user3)

        val user4 = daoSessionManager.newUserIdentity()
        user4.identity = "1234567890"
        user4.type = IdentityType.TWITTER
        user4.displayName = "Some Body"
        user4.handle = "Some Handle"
        daoSessionManager.insert(user4)

        val namelessPhoneIdentities = userIdentityHelper.namelessPhoneIdentities

        assertThat(userIdentityHelper.all.size, equalTo(4))
        assertThat(namelessPhoneIdentities.size, equalTo(2))
        assertThat(namelessPhoneIdentities.get(0).identity, equalTo("+3305551111"))
        assertThat(namelessPhoneIdentities.get(1).identity, equalTo("+3305550000"))
        db.close()
    }
}