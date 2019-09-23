package com.coinninja.coinkeeper.model.helpers


import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.coinkeeper.TestCoinKeeperApplication
import com.coinninja.coinkeeper.cn.wallet.WalletConfiguration
import com.coinninja.coinkeeper.db.TestOpenHelper
import com.coinninja.coinkeeper.model.PhoneNumber
import com.coinninja.coinkeeper.model.db.Account
import com.coinninja.coinkeeper.model.db.DaoMaster
import com.coinninja.coinkeeper.model.db.DropbitMeIdentity
import com.coinninja.coinkeeper.model.db.UserIdentity
import com.coinninja.coinkeeper.model.db.enums.AccountStatus
import com.coinninja.coinkeeper.model.db.enums.IdentityType
import com.coinninja.coinkeeper.model.query.WalletQueryManager
import com.coinninja.coinkeeper.service.client.CNUserAccount
import com.coinninja.coinkeeper.service.client.CNUserIdentity
import com.coinninja.coinkeeper.service.client.model.CNUserPatch
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.greenrobot.greendao.database.Database
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
    fun returns_number_of_verified_identities() {
        val db = getWritableDB()
        val daoSessionManager = DaoSessionManager(DaoMaster(db), WalletConfiguration(49, 0, 0, false)).connect()
        val helper = createDropbitAccountHelper(daoSessionManager, accountStatus = AccountStatus.VERIFIED)
        val dropbitMeIdentity = DropbitMeIdentity()
        dropbitMeIdentity.account = helper.walletHelper.userAccount
        dropbitMeIdentity.type = IdentityType.PHONE
        dropbitMeIdentity.status = AccountStatus.VERIFIED
        daoSessionManager.insert(dropbitMeIdentity)

        assertThat(helper.numVerifiedIdentities).isEqualTo(1)
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
    fun creates_phone_identity_from_server_identity() {
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
    fun creates_twitter_identity_from_server_identity() {
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
    fun has_verified_account__false_when_pending_verification_status() {
        val db = getWritableDB()
        val daoSessionManager = DaoSessionManager(DaoMaster(db), WalletConfiguration(49, 0, 0, false)).connect()
        val helper = createDropbitAccountHelper(daoSessionManager)
        val dropbitMeIdentity = DropbitMeIdentity()
        dropbitMeIdentity.account = helper.walletHelper.userAccount
        dropbitMeIdentity.type = IdentityType.PHONE
        daoSessionManager.insert(dropbitMeIdentity)

        assertThat(helper.hasVerifiedAccount).isFalse()
    }

    @Test
    fun has_verified_account__true_when_verified_status() {
        val db = getWritableDB()
        val daoSessionManager = DaoSessionManager(DaoMaster(db), WalletConfiguration(49, 0, 0, false)).connect()
        val helper = createDropbitAccountHelper(daoSessionManager, accountStatus = AccountStatus.VERIFIED)
        val dropbitMeIdentity = DropbitMeIdentity()
        dropbitMeIdentity.account = helper.walletHelper.userAccount
        dropbitMeIdentity.type = IdentityType.PHONE
        dropbitMeIdentity.status = AccountStatus.VERIFIED
        daoSessionManager.insert(dropbitMeIdentity)

        assertThat(helper.hasVerifiedAccount).isTrue()
    }

    @Test
    fun fetches_a_dropbit_account_for_the_desired_toUser_identity_type____resulting_in_identity_of_same_type() {
        val userIdentity = mock(UserIdentity::class.java)
        val db = getWritableDB()
        val daoSessionManager = DaoSessionManager(DaoMaster(db), WalletConfiguration(49, 0, 0, false)).connect()
        whenever(userIdentity.type).thenReturn(IdentityType.PHONE)
        val account = Account()
        account.status = AccountStatus.VERIFIED
        account.wallet = daoSessionManager.createWallet()
        daoSessionManager.insert(account)
        val dropbitAccountHelper = createDropbitAccountHelper(daoSessionManager, accountStatus = AccountStatus.VERIFIED)
        val phoneIdentity = DropbitMeIdentity()
        phoneIdentity.type = IdentityType.PHONE
        phoneIdentity.identity = "+13305551111"
        phoneIdentity.status = AccountStatus.VERIFIED
        phoneIdentity.account = account
        daoSessionManager.insert(phoneIdentity)
        val twitterIdentity = DropbitMeIdentity()
        twitterIdentity.type = IdentityType.TWITTER
        twitterIdentity.identity = "12345654321"
        phoneIdentity.account = account
        daoSessionManager.insert(twitterIdentity)

        val profile = dropbitAccountHelper.profileForIdentity(userIdentity)

        assertThat(profile).isNotNull()
        assertThat(profile!!.type).isEqualTo(IdentityType.PHONE)
    }

    @Test
    fun fetches_a_dropbit_account_for_the_desired_toUser_identity_type__resulting_in_whatever_is_verified() {
        val userIdentity = mock(UserIdentity::class.java)
        val db = getWritableDB()
        val daoSessionManager = DaoSessionManager(DaoMaster(db), WalletConfiguration(49, 0, 0, false)).connect()
        whenever(userIdentity.type).thenReturn(IdentityType.PHONE)
        val dropbitAccountHelper = createDropbitAccountHelper(daoSessionManager, accountStatus = AccountStatus.VERIFIED)
        val twitterIdentity = DropbitMeIdentity()
        twitterIdentity.type = IdentityType.TWITTER
        twitterIdentity.identity = "12345654321"
        daoSessionManager.insert(twitterIdentity)

        val profile = dropbitAccountHelper.profileForIdentity(userIdentity)

        assertThat(profile).isNotNull()
        assertThat(profile!!.type).isEqualTo(IdentityType.TWITTER)
    }

    @Test
    fun updates_account_with_new_ids() {
        val accountHelper = createDropbitAccountHelper()
        val account: Account = mock()
        whenever(accountHelper.walletHelper.userAccount).thenReturn(account)

        accountHelper.updateAccountIds("--wallet-id--")

        verify(account).cnWalletId = "--wallet-id--"
        verify(account).update()
    }

    private fun createDropbitAccountHelper(daoSessionManager: DaoSessionManager, accountStatus: AccountStatus = AccountStatus.PENDING_VERIFICATION): DropbitAccountHelper {
        val helper = DropbitAccountHelper(daoSessionManager, WalletHelper(daoSessionManager, WalletQueryManager(daoSessionManager), WordHelper(daoSessionManager), mock()))
        val wallet = daoSessionManager.createWallet()
        val account = Account()
        account.status = accountStatus
        account.wallet = wallet
        account.cnWalletId = "--cn-wallet-id--"
        daoSessionManager.insert(account)
        return helper
    }

    private fun createDropbitAccountHelper(): DropbitAccountHelper {
        val dropbitAccountHelper = DropbitAccountHelper(mock(DaoSessionManager::class.java),
                mock(WalletHelper::class.java))
        return dropbitAccountHelper
    }
}

