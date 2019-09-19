package com.coinninja.coinkeeper.model.db

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.coinkeeper.TestCoinKeeperApplication
import com.coinninja.coinkeeper.cn.wallet.WalletConfiguration
import com.coinninja.coinkeeper.db.TestOpenHelper
import com.coinninja.coinkeeper.model.helpers.DaoSessionManager
import junit.framework.Assert.assertFalse
import org.greenrobot.greendao.database.Database
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InviteTransactionSummaryTest {

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
    fun prove_the_custom_mock_is_touching_the_real_InviteTransactionSummary_class_test() {
        val transactionSummary = CustomMockInviteTransactionSummary()
        val sampleTxId = "some tx id"

        transactionSummary.btcTransactionId = sampleTxId//this setter dose NOT exist in the mock
        val currentTxId = transactionSummary.btcTransactionId

        assertThat(currentTxId, equalTo(sampleTxId))
    }

    @Test
    fun set_tx_id_to_real_value_then_try_to_set_it_empty_test() {
        //setting txID to empty should never work, InviteTransactionSummary should protect it's self
        val transactionSummary = CustomMockInviteTransactionSummary()
        val sampleTxId = "some tx id"

        transactionSummary.btcTransactionId = sampleTxId
        val currentTxId = transactionSummary.btcTransactionId

        assertThat(currentTxId, equalTo(sampleTxId))//prove its set to a non empty value
        transactionSummary.btcTransactionId = ""//then try to set it empty
        val newTxId = transactionSummary.btcTransactionId


        //txid should still be sampleId and not empty
        //new txid should be the same as the old
        assertFalse(newTxId!!.isEmpty())
        assertThat(newTxId, equalTo(sampleTxId))
        assertThat(newTxId, equalTo(currentTxId))
    }

    @Test
    fun set_tx_id_to_real_value_then_try_to_set_it_null_test() {
        //setting txID to null should never work, InviteTransactionSummary should protect it's self
        val sampleTxId = "some tx id"
        val transactionSummary = CustomMockInviteTransactionSummary()

        transactionSummary.btcTransactionId = sampleTxId
        val currentTxId = transactionSummary.btcTransactionId

        assertThat(currentTxId, equalTo(sampleTxId))//prove its set to a not null value
        transactionSummary.btcTransactionId = null//then try to set it null
        val newTxId = transactionSummary.btcTransactionId


        //txid should still be sampleId and not null
        //new txid should be the same as the old
        assertFalse(newTxId == null)
        assertThat(newTxId, equalTo(sampleTxId))
        assertThat(newTxId, equalTo(currentTxId))
    }


    //this custom mock will use @Override's to touch the real underline classes code
    internal inner class CustomMockInviteTransactionSummary : InviteTransactionSummary() {

        private var txId: String? = null


        //the real InviteTransactionSummary will call this method and this mock will catch it and set its local var
        override fun setTxId(btcTransactionId: String) {
            //the unit test should Never call setTxId directly.
            //if this method is ever called that means the real InviteTransactionSummary called it
            txId = btcTransactionId
        }

        override fun getBtcTransactionId(): String? {
            return txId
        }
    }
}