package com.coinninja.coinkeeper.model.helpers

import com.coinninja.coinkeeper.model.db.DaoSession
import com.coinninja.coinkeeper.model.db.UserDao
import com.coinninja.coinkeeper.model.db.Wallet
import com.coinninja.coinkeeper.model.db.WalletDao
import com.nhaarman.mockitokotlin2.*
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class DaoSessionManagerTest {

    private fun createSessionManager(): DaoSessionManager {
        val daoSessionManager = DaoSessionManager(mock(), 49, 1, 0)
        val session = mock<DaoSession>()
        whenever(daoSessionManager.daoMaster.newSession()).thenReturn(session)
        whenever(session.database).thenReturn(mock())
        return daoSessionManager.connect()
    }

    @Test
    fun creates_wallet_for_user() {
        val sessionManager = createSessionManager()
        val userDao: UserDao = mock()
        val walletDao: WalletDao = mock()
        whenever(sessionManager.daoSession.userDao).thenReturn(userDao)
        whenever(sessionManager.daoSession.walletDao).thenReturn(walletDao)
        whenever(userDao.insert(any())).thenReturn(1L)

        sessionManager.createWallet()
        val walletCaptor = argumentCaptor<Wallet>()
        verify(walletDao).insert(walletCaptor.capture())

        val wallet = walletCaptor.firstValue
        assertThat(wallet.userId, equalTo(1L))
        assertThat(wallet.purpose, equalTo(49))
        assertThat(wallet.coinType, equalTo(1))
        assertThat(wallet.accountIndex, equalTo(0))
    }

}