package com.coinninja.coinkeeper.db.migrations

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.coinkeeper.TestCoinKeeperApplication
import com.coinninja.coinkeeper.db.TestOpenHelper
import com.coinninja.coinkeeper.model.db.DaoMaster
import com.coinninja.coinkeeper.model.db.enums.BTCState
import com.coinninja.coinkeeper.model.db.enums.IdentityType
import com.coinninja.coinkeeper.model.db.enums.Type
import com.coinninja.coinkeeper.model.helpers.DaoSessionManager
import junit.framework.Assert.*
import org.greenrobot.greendao.database.Database
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Migrate_V33_to_V34Test {


    @Before
    fun setUp() {
    }

    @After
    fun tearDown() {
        ApplicationProvider.getApplicationContext<TestCoinKeeperApplication>().deleteDatabase(TestOpenHelper.dbName)
    }


    private fun prepareInviteTransactionSummariesForTest(db: Database) {

        //Insert Invite Record
        db.execSQL("INSERT INTO INVITE_TRANSACTION_SUMMARY (\"WALLET_ID\", " +
                "\"TRANSACTIONS_INVITES_SUMMARY_ID\", \"TYPE\", \"BTC_STATE\", \"SERVER_ID\", " +
                "\"INVITE_NAME\", \"BTC_TRANSACTION_ID\", \"SENT_DATE\", \"SENDER_PHONE_NUMBER\", " +
                "\"RECEIVER_PHONE_NUMBER\", \"ADDRESS\", \"PUBKEY\", \"VALUE_SATOSHIS\", " +
                "\"VALUE_FEES_SATOSHIS\", \"HISTORIC_VALUE\", " +
                "\"HISTORIC_USDVALUE\", \"TRANSACTION_NOTIFICATION_ID\") " +
                "VALUES (1, 1, 0, 1, '--server-id--', 'Joe Blow', '--txid--', 1559329263801, " +
                "'+13305551111', '+13305551122', '--address--', '--pub-key--', " +
                "5000, 400, 500, '500', 1)"
        )

        //Insert Invite Record
        db.execSQL("INSERT INTO INVITE_TRANSACTION_SUMMARY (\"WALLET_ID\", " +
                "\"TRANSACTIONS_INVITES_SUMMARY_ID\", \"TYPE\", \"BTC_STATE\", \"SERVER_ID\", " +
                "\"INVITE_NAME\", \"BTC_TRANSACTION_ID\", \"SENT_DATE\", \"SENDER_PHONE_NUMBER\", " +
                "\"RECEIVER_PHONE_NUMBER\", \"ADDRESS\", \"PUBKEY\", \"VALUE_SATOSHIS\", " +
                "\"VALUE_FEES_SATOSHIS\", \"HISTORIC_VALUE\", " +
                "\"HISTORIC_USDVALUE\", \"TRANSACTION_NOTIFICATION_ID\") " +
                "VALUES (1, 2, 10, 0, '--server-id-1--', NULL, NULL, 1559329333331, " +
                "'+13305551122', '+13305550099', '--address-1--', '--pub-key-1--', " +
                "15000, 500, 100, '100', 2)"
        )
    }

    private fun prepareTransactionInviteSummariesForTest(db: Database) {
        // existing number
        db.execSQL("INSERT INTO TRANSACTIONS_INVITES_SUMMARY (TRANSACTION_SUMMARY_ID, TO_NAME, TO_PHONE_NUMBER, INVITE_SUMMARY_ID, INVITE_TIME, " +
                "BTC_TX_TIME, TRANSACTION_TX_ID, INVITE_TX_ID) " +
                "VALUES (1, 'Joe Blow', '+13305551122', 1, 0, 1559851000000, '--txid--', '--txid--')")
        db.execSQL("INSERT INTO TRANSACTIONS_INVITES_SUMMARY (TRANSACTION_SUMMARY_ID, TO_NAME, TO_PHONE_NUMBER, INVITE_SUMMARY_ID, INVITE_TIME, " +
                "BTC_TX_TIME, TRANSACTION_TX_ID, INVITE_TX_ID) " +
                "VALUES (2, NULL, '+13305550000', NULL, 0, 1559852000000, '--txid-1--', NULL)")
        db.execSQL("INSERT INTO TRANSACTIONS_INVITES_SUMMARY (TRANSACTION_SUMMARY_ID, TO_NAME, TO_PHONE_NUMBER, INVITE_SUMMARY_ID, INVITE_TIME, " +
                "BTC_TX_TIME, TRANSACTION_TX_ID, INVITE_TX_ID) " +
                "VALUES (NULL, 'Joe Blow', '+13305551122', 2, 1559853000000, 0, NULL, NULL)")
    }

    private fun prepareTransactionNotificationForTest(db: Database) {
        // Joe name and number
        db.execSQL("INSERT INTO \"TRANSACTION_NOTIFICATION\" (\"_id\", " +
                "\"DROPBIT_ME_HANDLE\", \"AVATAR\", \"DISPLAY_NAME\", \"MEMO\", \"IS_SHARED\", " +
                "\"AMOUNT\", \"AMOUNT_CURRENCY\", \"TXID\", \"PHONE_NUMBER\") " +
                "VALUES (NULL, NULL, 'NULL', 'Joe', 'for the tacos', 1, 500, " +
                "'USD', '--txid-1--', '+13305551122');")
        // Joe Blow number only
        db.execSQL("INSERT INTO \"TRANSACTION_NOTIFICATION\" (\"_id\", " +
                "\"DROPBIT_ME_HANDLE\", \"AVATAR\", \"DISPLAY_NAME\", \"MEMO\", \"IS_SHARED\", " +
                "\"AMOUNT\", \"AMOUNT_CURRENCY\", \"TXID\", \"PHONE_NUMBER\") " +
                "VALUES (NULL, NULL, 'NULL', '', 'for the game', 1, 600, " +
                "'USD', '--txid-2--', '+13305551122');")
        // other number
        db.execSQL("INSERT INTO \"TRANSACTION_NOTIFICATION\" (\"_id\", " +
                "\"DROPBIT_ME_HANDLE\", \"AVATAR\", \"DISPLAY_NAME\", \"MEMO\", \"IS_SHARED\", " +
                "\"AMOUNT\", \"AMOUNT_CURRENCY\", \"TXID\", \"PHONE_NUMBER\") " +
                "VALUES (NULL, NULL, 'NULL', '', 'for things and stuff', 0, 700, " +
                "'USD', '--txid-3--', '+14405551111');")

        // Joe Blow number only
        db.execSQL("INSERT INTO \"TRANSACTION_NOTIFICATION\" (\"_id\", " +
                "\"DROPBIT_ME_HANDLE\", \"AVATAR\", \"DISPLAY_NAME\", \"MEMO\", \"IS_SHARED\", " +
                "\"AMOUNT\", \"AMOUNT_CURRENCY\", \"TXID\", \"PHONE_NUMBER\") " +
                "VALUES (NULL, NULL, NULL, \"Joe Blow\", NULL, 0, 1500, " +
                "'USD', '--txid-4--', '+13305551122');")

        db.execSQL("INSERT INTO TRANSACTION_NOTIFICATION (DISPLAY_NAME, PHONE_NUMBER, IS_SHARED, AMOUNT) " +
                "VALUES (\"Sunny\", '+13305551111', 0, 0)")

        db.execSQL("INSERT INTO TRANSACTION_NOTIFICATION (DISPLAY_NAME, PHONE_NUMBER, IS_SHARED, AMOUNT) " +
                "VALUES (\"Calvin\", '+13305550000', 0, 0)")
    }

    private fun prepare(db: Database) {
        prepareTransactionNotificationForTest(db)
        prepareInviteTransactionSummariesForTest(db)
        prepareTransactionInviteSummariesForTest(db)
    }

    @Test
    fun `migrates data`() {
        val helper = TestOpenHelper(ApplicationProvider.getApplicationContext(), TestOpenHelper.dbName)
        val db = helper.writableV31Db
        prepare(db)

        Migrate_V33_to_V34().runMigration(db, 33)

        val daoSessionManager = DaoSessionManager(DaoMaster(db)).connect()
        verifyTransactionInviteSummaries(daoSessionManager)
        verifyInviteSummaries(daoSessionManager)
        verifyTransactionNotifications(daoSessionManager)

        // Deletes temp tables
        var cursor = db.rawQuery("select * from sqlite_master where tbl_name = \"TEMP_TRANSACTION_NOTIFICATION\"", null)
        assertThat(cursor.count, equalTo(0))
        cursor.close()
        cursor = db.rawQuery("select * from sqlite_master where tbl_name = \"TEMP_INVITE_TRANSACTION_SUMMARY\"", null)
        assertThat(cursor.count, equalTo(0))
        cursor.close()
        cursor = db.rawQuery("select * from sqlite_master where tbl_name = \"TEMP_TRANSACTIONS_INVITES_SUMMARY\"", null)
        assertThat(cursor.count, equalTo(0))
        cursor.close()

        db.close()
    }


    private fun verifyTransactionNotifications(daoSessionManager: DaoSessionManager) {
        val all = daoSessionManager.transactionNotificationDao.loadAll()
        assertThat(all.size, equalTo(6))

        var notification = all.get(0)
        assertThat(notification.memo, equalTo("for the tacos"))
        assertTrue(notification.isShared)
        assertThat(notification.amount, equalTo(500L))
        assertThat(notification.amountCurrency, equalTo("USD"))
        assertThat(notification.txid, equalTo("--txid-1--"))
        assertThat(notification.toUser.id, equalTo(1L))
        assertThat(notification.toUser.identity, equalTo("+13305551122"))
        assertThat(notification.toUser.type, equalTo(IdentityType.PHONE))
        assertThat(notification.toUser.hash, equalTo("8c0d6dd8ecd2daa218493a07e99fe03da184d1aeee6e55e373e6171eaab81641"))
        assertThat(notification.toUser.displayName, equalTo("Joe Blow"))

        // does not replace existing identity data
        notification = all.get(1)
        assertThat(notification.memo, equalTo("for the game"))
        assertTrue(notification.isShared)
        assertThat(notification.amount, equalTo(600L))
        assertThat(notification.amountCurrency, equalTo("USD"))
        assertThat(notification.txid, equalTo("--txid-2--"))
        assertThat(notification.toUser.id, equalTo(1L))
        assertThat(notification.toUser.identity, equalTo("+13305551122"))
        assertThat(notification.toUser.type, equalTo(IdentityType.PHONE))
        assertThat(notification.toUser.hash, equalTo("8c0d6dd8ecd2daa218493a07e99fe03da184d1aeee6e55e373e6171eaab81641"))
        assertThat(notification.toUser.displayName, equalTo("Joe Blow"))

        notification = all.get(2)
        assertThat(notification.memo, equalTo("for things and stuff"))
        assertFalse(notification.isShared)
        assertThat(notification.amount, equalTo(700L))
        assertThat(notification.amountCurrency, equalTo("USD"))
        assertThat(notification.txid, equalTo("--txid-3--"))
        assertThat(notification.toUser.identity, equalTo("+14405551111"))
        assertThat(notification.toUser.id, equalTo(2L))
        assertThat(notification.toUser.type, equalTo(IdentityType.PHONE))
        assertThat(notification.toUser.hash, equalTo("6be07ceea64153ce304bc2951fa187b8608d43a6ba829a1172dd93dcdd8c7251"))
        assertNull(notification.toUser.displayName)

        notification = all.get(3)
        assertNull(notification.memo)
        assertFalse(notification.isShared)
        assertThat(notification.amount, equalTo(1500L))
        assertThat(notification.amountCurrency, equalTo("USD"))
        assertThat(notification.txid, equalTo("--txid-4--"))
        assertThat(notification.toUser.id, equalTo(1L))
        assertThat(notification.toUser.identity, equalTo("+13305551122"))
        assertThat(notification.toUser.type, equalTo(IdentityType.PHONE))
        assertThat(notification.toUser.hash, equalTo("8c0d6dd8ecd2daa218493a07e99fe03da184d1aeee6e55e373e6171eaab81641"))
        assertThat(notification.toUser.displayName, equalTo("Joe Blow"))

    }

    private fun verifyInviteSummaries(daoSessionManager: DaoSessionManager) {
        val all = daoSessionManager.inviteTransactionSummaryDao.loadAll()
        assertThat(all.size, equalTo(2))

        var invite = all.get(0)
        assertThat(invite.id, equalTo(1L))
        assertThat(invite.walletId, equalTo(1L))
        assertThat(invite.transactionsInvitesSummaryID, equalTo(1L))
        assertThat(invite.transactionNotificationId, equalTo(1L))
        assertThat(invite.type, equalTo(Type.SENT))
        assertThat(invite.btcState, equalTo(BTCState.FULFILLED))
        assertThat(invite.serverId, equalTo("--server-id--"))
        assertThat(invite.btcTransactionId, equalTo("--txid--"))
        assertThat(invite.sentDate, equalTo(1559329263801L))
        assertThat(invite.address, equalTo("--address--"))
        assertThat(invite.pubkey, equalTo("--pub-key--"))
        assertThat(invite.valueSatoshis, equalTo(5000L))
        assertThat(invite.valueFeesSatoshis, equalTo(400L))
        assertThat(invite.historicValue, equalTo(500L))

        assertThat(invite.fromUser.id, equalTo(3L))
        assertThat(invite.fromUser.identity, equalTo("+13305551111"))
        assertThat(invite.fromUser.type, equalTo(IdentityType.PHONE))
        assertThat(invite.fromUser.displayName, equalTo("Sunny"))

        assertThat(invite.toUser.id, equalTo(1L))
        assertThat(invite.toUser.identity, equalTo("+13305551122"))
        assertThat(invite.toUser.type, equalTo(IdentityType.PHONE))
        assertThat(invite.toUser.displayName, equalTo("Joe Blow"))

        invite = all.get(1)
        assertThat(invite.walletId, equalTo(1L))
        assertThat(invite.id, equalTo(2L))
        assertThat(invite.walletId, equalTo(1L))
        assertThat(invite.transactionsInvitesSummaryID, equalTo(2L))
        assertThat(invite.transactionNotificationId, equalTo(2L))
        assertThat(invite.type, equalTo(Type.RECEIVED))
        assertThat(invite.btcState, equalTo(BTCState.UNFULFILLED))
        assertThat(invite.serverId, equalTo("--server-id-1--"))
        assertNull(invite.btcTransactionId)
        assertThat(invite.sentDate, equalTo(1559329333331L))
        assertThat(invite.address, equalTo("--address-1--"))
        assertThat(invite.pubkey, equalTo("--pub-key-1--"))
        assertThat(invite.valueSatoshis, equalTo(15000L))
        assertThat(invite.valueFeesSatoshis, equalTo(500L))
        assertThat(invite.historicValue, equalTo(100L))

        assertThat(invite.fromUser.id, equalTo(1L))
        assertThat(invite.fromUser.identity, equalTo("+13305551122"))
        assertThat(invite.fromUser.type, equalTo(IdentityType.PHONE))
        assertThat(invite.fromUser.displayName, equalTo("Joe Blow"))

        assertThat(invite.toUser.id, equalTo(5L))
        assertThat(invite.toUser.identity, equalTo("+13305550099"))
        assertThat(invite.toUser.type, equalTo(IdentityType.PHONE))
        assertNull(invite.toUser.displayName)
    }

    fun verifyTransactionInviteSummaries(daoSessionManager: DaoSessionManager) {
        val all = daoSessionManager.transactionsInvitesSummaryDao.loadAll()
        assertThat(all.size, equalTo(3))

        var summary = all[0]
        assertThat(summary.id, equalTo(1L))
        assertThat(summary.transactionSummaryID, equalTo(1L))
        assertThat(summary.inviteSummaryID, equalTo(1L))
        assertThat(summary.inviteTime, equalTo(0L))
        assertThat(summary.btcTxTime, equalTo(1559851000000L))
        assertThat(summary.transactionTxID, equalTo("--txid--"))
        assertThat(summary.inviteTxID, equalTo("--txid--"))

        assertThat(summary.fromUser.id, equalTo(3L))
        assertThat(summary.fromUser.displayName, equalTo("Sunny"))
        assertThat(summary.fromUser.identity, equalTo("+13305551111"))

        assertThat(summary.toUser.id, equalTo(1L))
        assertThat(summary.toUser.displayName, equalTo("Joe Blow"))
        assertThat(summary.toUser.identity, equalTo("+13305551122"))

        summary = all[1]
        assertThat(summary.id, equalTo(2L))
        assertThat(summary.transactionSummaryID, equalTo(2L))
        assertNull(summary.inviteSummaryID)
        assertThat(summary.inviteTime, equalTo(0L))
        assertThat(summary.btcTxTime, equalTo(1559852000000L))
        assertThat(summary.transactionTxID, equalTo("--txid-1--"))
        assertNull(summary.inviteTxID)

        assertNull(summary.fromUser)

        assertThat(summary.toUser.id, equalTo(4L))
        assertThat(summary.toUser.displayName, equalTo("Calvin"))
        assertThat(summary.toUser.identity, equalTo("+13305550000"))

        summary = all[2]
        assertThat(summary.id, equalTo(3L))
        assertNull(summary.transactionSummaryID)
        assertThat(summary.inviteSummaryID, equalTo(2L))
        assertThat(summary.inviteTime, equalTo(1559853000000L))
        assertThat(summary.btcTxTime, equalTo(0L))
        assertNull(summary.transactionTxID)
        assertNull(summary.inviteTxID)

        assertThat(summary.fromUser.id, equalTo(1L))
        assertThat(summary.fromUser.identity, equalTo("+13305551122"))
        assertThat(summary.fromUser.type, equalTo(IdentityType.PHONE))
        assertThat(summary.fromUser.displayName, equalTo("Joe Blow"))

        assertThat(summary.toUser.id, equalTo(5L))
        assertThat(summary.toUser.identity, equalTo("+13305550099"))
        assertThat(summary.toUser.type, equalTo(IdentityType.PHONE))
        assertNull(summary.toUser.displayName)

    }

}