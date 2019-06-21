package com.coinninja.coinkeeper.model.helpers


import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.coinkeeper.TestCoinKeeperApplication
import com.coinninja.coinkeeper.db.TestOpenHelper
import com.coinninja.coinkeeper.model.PhoneNumber
import com.coinninja.coinkeeper.model.db.*
import com.coinninja.coinkeeper.model.db.enums.AccountStatus
import com.coinninja.coinkeeper.model.db.enums.IdentityType
import com.coinninja.coinkeeper.service.client.CNUserAccount
import com.coinninja.coinkeeper.service.client.CNUserIdentity
import com.coinninja.coinkeeper.service.client.model.CNUserPatch
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.Assert.assertNotNull
import org.greenrobot.greendao.database.Database
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@Suppress("UNCHECKED_CAST")
@RunWith(AndroidJUnit4::class)
class DropbitAccountHelperTest {
    private fun getWritableDB(): Database {
        val helper = TestOpenHelper(ApplicationProvider.getApplicationContext(), TestOpenHelper.dbName)
        return helper.writableDb
    }

    @After
    fun tearDown() {
        ApplicationProvider.getApplicationContext<TestCoinKeeperApplication>().deleteDatabase(TestOpenHelper.dbName)
    }

    @Test
    fun `returns number of verified identities`() {
        val dropbitAccountHelper = createDropbitAccountHelper()
        val dao = mock(DropbitMeIdentityDao::class.java)
        whenever(dropbitAccountHelper.daoSessionManager.dropbitMeIdentityDao).thenReturn(dao)
        whenever(dao.loadAll()).thenReturn(arrayListOf(mock(DropbitMeIdentity::class.java), mock(DropbitMeIdentity::class.java)))

        assertThat(dropbitAccountHelper.numVerifiedIdentities, equalTo(2))
    }

    @Test
    fun updates_user_account_from_patch() {
        val account = mock(Account::class.java)
        val dropbitAccountHelper = createDropbitAccountHelper()
        whenever(dropbitAccountHelper.walletHelper.userAccount).thenReturn(account)

        dropbitAccountHelper.updateUserAccount(CNUserPatch(true))

        verify(account).isPrivate = true
        verify(account).update()
    }

    @Test(expected = Test.None::class)
    fun updates_user_account_from_patch__with_null_account() {
        val dropbitAccountHelper = createDropbitAccountHelper()
        whenever(dropbitAccountHelper.walletHelper.userAccount).thenReturn(null)

        dropbitAccountHelper.updateUserAccount(CNUserPatch(true))
    }

    @Test
    fun updates_account_when_verified() {
        val dropbitAccountHelper = createDropbitAccountHelper()
        val hash = "0123456789abcdefghijklmnopqrstuvwxyz"
        val phoneNumber = PhoneNumber("+13305551111")
        val cnUserAccount = CNUserAccount()
        cnUserAccount.isPrivate = false
        val account = mock(Account::class.java)
        whenever(account.phoneNumberHash).thenReturn(hash)
        whenever(account.phoneNumber).thenReturn(phoneNumber)
        whenever(dropbitAccountHelper.walletHelper.userAccount).thenReturn(account)
        val dropbitMeIdentity = mock(DropbitMeIdentity::class.java)
        whenever(dropbitAccountHelper.daoSessionManager.newDropbitMeIdentity()).thenReturn(dropbitMeIdentity)

        dropbitAccountHelper.updateVerifiedAccount(cnUserAccount)

        verify(account).isPrivate = false
        verify(account).status = AccountStatus.VERIFIED
        verify(account).update()

        verify(dropbitMeIdentity).hash = hash
        verify(dropbitMeIdentity).identity = phoneNumber.toString()
        verify(dropbitMeIdentity).type = IdentityType.PHONE
        verify(dropbitMeIdentity).account = account
        verify(dropbitMeIdentity).handle = "0123456789ab"
        verify(dropbitAccountHelper.daoSessionManager).insert(dropbitMeIdentity)

    }

    @Test
    fun `creates phone identity from server identity`() {
        val helper = createDropbitAccountHelper()
        val dropbitMeIdentity = mock(DropbitMeIdentity::class.java)
        whenever(helper.daoSessionManager.newDropbitMeIdentity()).thenReturn(dropbitMeIdentity)
        val account = mock(Account::class.java)
        whenever(helper.walletHelper.userAccount).thenReturn(account)
        val identity = CNUserIdentity()
        identity.type = "phone"
        identity.id = "--server-id--"
        identity.handle = "0123456789AB"
        identity.hash = "0123456789ABCDEFGHIJKLMNOPQRSTUV"
        identity.identity = "13305551111"

        helper.newFrom(identity)

        verify(dropbitMeIdentity).handle = identity.handle
        verify(dropbitMeIdentity).serverId = identity.id
        verify(dropbitMeIdentity).identity = identity.identity
        verify(dropbitMeIdentity).hash = identity.hash
        verify(dropbitMeIdentity).type = IdentityType.PHONE
        verify(dropbitMeIdentity).account = account
        verify(helper.daoSessionManager).insert(dropbitMeIdentity)
    }

    @Test
    fun `creates twitter identity from server identity`() {
        val helper = createDropbitAccountHelper()
        val dropbitMeIdentity = mock(DropbitMeIdentity::class.java)
        whenever(helper.daoSessionManager.newDropbitMeIdentity()).thenReturn(dropbitMeIdentity)
        val account = mock(Account::class.java)
        whenever(helper.walletHelper.userAccount).thenReturn(account)
        val identity = CNUserIdentity()
        identity.type = "twitter"
        identity.id = "--server-id--"
        identity.handle = "@JohnnyNumber5"
        identity.hash = "--snow-flake--"
        identity.identity = "--snow-flake--"

        helper.newFrom(identity)

        verify(dropbitMeIdentity).handle = identity.handle
        verify(dropbitMeIdentity).serverId = identity.id
        verify(dropbitMeIdentity).identity = identity.identity
        verify(dropbitMeIdentity).hash = identity.hash
        verify(dropbitMeIdentity).type = IdentityType.TWITTER
        verify(dropbitMeIdentity).account = account
        verify(helper.daoSessionManager).insert(dropbitMeIdentity)
    }

    @Test
    fun `fetches a dropbit account for the desired toUser identity type -- resulting in identity of same type`() {
        val userIdentity = mock(UserIdentity::class.java)
        val db = getWritableDB()
        val daoSessionManager = DaoSessionManager(DaoMaster(db)).connect()
        whenever(userIdentity.type).thenReturn(IdentityType.PHONE)
        val dropbitAccountHelper = DropbitAccountHelper(daoSessionManager, mock(WalletHelper::class.java))
        val phoneIdentity = DropbitMeIdentity()
        phoneIdentity.type = IdentityType.PHONE
        phoneIdentity.identity = "+13305551111"
        phoneIdentity.status = AccountStatus.VERIFIED
        daoSessionManager.insert(phoneIdentity)
        val twitterIdentity = DropbitMeIdentity()
        twitterIdentity.type = IdentityType.TWITTER
        twitterIdentity.identity = "12345654321"
        daoSessionManager.insert(twitterIdentity)


        val profile = dropbitAccountHelper.profileForIdentity(userIdentity)

        assertNotNull(profile)
        assertThat(profile!!.type, equalTo(IdentityType.PHONE))
    }

    @Test
    fun `fetches a dropbit account for the desired toUser identity type -- resulting in whatever is verified`() {
        val userIdentity = mock(UserIdentity::class.java)
        val db = getWritableDB()
        val daoSessionManager = DaoSessionManager(DaoMaster(db)).connect()
        whenever(userIdentity.type).thenReturn(IdentityType.PHONE)
        val dropbitAccountHelper = DropbitAccountHelper(daoSessionManager, mock(WalletHelper::class.java))
        val twitterIdentity = DropbitMeIdentity()
        twitterIdentity.type = IdentityType.TWITTER
        twitterIdentity.identity = "12345654321"
        daoSessionManager.insert(twitterIdentity)

        val profile = dropbitAccountHelper.profileForIdentity(userIdentity)

        assertNotNull(profile)
        assertThat(profile!!.type, equalTo(IdentityType.TWITTER))
    }

    private fun createDropbitAccountHelper(): DropbitAccountHelper {
        val dropbitAccountHelper = DropbitAccountHelper(mock(DaoSessionManager::class.java),
                mock(WalletHelper::class.java))
        return dropbitAccountHelper
    }
}

