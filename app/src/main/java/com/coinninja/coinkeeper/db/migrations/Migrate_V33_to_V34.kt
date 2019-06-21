package com.coinninja.coinkeeper.db.migrations

import com.coinninja.coinkeeper.db.AbstractMigration
import com.coinninja.coinkeeper.db.Migration
import com.coinninja.coinkeeper.model.PhoneNumber
import com.coinninja.coinkeeper.model.db.enums.Type
import com.coinninja.coinkeeper.util.Hasher
import org.greenrobot.greendao.database.Database

class Migrate_V33_to_V34 : AbstractMigration() {
    private val hasher: Hasher = Hasher()

    override fun getMigratedVersion(): Int {
        return 34
    }

    override fun applyMigration(db: Database, currentVersion: Int) {
        createUserIdentityTable(db)
        migrateTransactionNotifications(db)
        migrateInviteTransactionSummaries(db)
        migrateTransactionInviteSummaries(db)
    }


    internal fun createUserIdentityTable(db: Database) {
        db.execSQL("CREATE TABLE \"USER_IDENTITY\" (\"_id\" INTEGER PRIMARY KEY , \"IDENTITY\" TEXT UNIQUE , \"TYPE\" INTEGER, \"DISPLAY_NAME\" TEXT, \"HANDLE\" TEXT, \"HASH\" TEXT, \"AVATAR\" TEXT);")
    }

    internal fun migrateInviteTransactionSummaries(db: Database) {
        db.execSQL("ALTER TABLE INVITE_TRANSACTION_SUMMARY RENAME TO TEMP_INVITE_TRANSACTION_SUMMARY;")
        db.execSQL("CREATE TABLE \"INVITE_TRANSACTION_SUMMARY\" " +
                "(\"TO_USER_IDENTITY_ID\" INTEGER, " +
                "\"FROM_USER_IDENTITY_ID\" INTEGER, " +
                "\"WALLET_ID\" INTEGER, " +
                "\"TRANSACTIONS_INVITES_SUMMARY_ID\" INTEGER, " +
                "\"TRANSACTION_NOTIFICATION_ID\" INTEGER, " +
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT , " +
                "\"TYPE\" INTEGER, " +
                "\"BTC_STATE\" INTEGER, " +
                "\"SERVER_ID\" TEXT UNIQUE , " +
                "\"BTC_TRANSACTION_ID\" TEXT, " +
                "\"SENT_DATE\" INTEGER, " +
                "\"ADDRESS\" TEXT, " +
                "\"PUBKEY\" TEXT, " +
                "\"VALUE_SATOSHIS\" INTEGER, " +
                "\"VALUE_FEES_SATOSHIS\" INTEGER, " +
                "\"HISTORIC_VALUE\" INTEGER NOT NULL" +
                ")")

        db.execSQL("INSERT INTO \"INVITE_TRANSACTION_SUMMARY\" \n" +
                "(" +
                "  \"_id\", " +
                "  \"WALLET_ID\", " +
                "  \"TRANSACTIONS_INVITES_SUMMARY_ID\", " +
                "  \"TRANSACTION_NOTIFICATION_ID\", " +
                "  \"BTC_TRANSACTION_ID\", " +
                "  \"TYPE\", " +
                "  \"BTC_STATE\", " +
                "  \"SERVER_ID\", " +
                "  \"SENT_DATE\", " +
                "  \"ADDRESS\", " +
                "  \"PUBKEY\", " +
                "  \"VALUE_SATOSHIS\", " +
                "  \"VALUE_FEES_SATOSHIS\", " +
                "  \"HISTORIC_VALUE\"" +
                ") \n" +
                "SELECT " +
                "  \"_id\", " +
                "  \"WALLET_ID\", " +
                "  \"TRANSACTIONS_INVITES_SUMMARY_ID\", " +
                "  \"TRANSACTION_NOTIFICATION_ID\", " +
                "  \"BTC_TRANSACTION_ID\", " +
                "  \"TYPE\", " +
                "  \"BTC_STATE\", " +
                "  \"SERVER_ID\", " +
                "  \"SENT_DATE\", " +
                "  \"ADDRESS\", " +
                "  \"PUBKEY\", " +
                "  \"VALUE_SATOSHIS\", " +
                "  \"VALUE_FEES_SATOSHIS\", " +
                "  \"HISTORIC_VALUE\"" +
                "FROM TEMP_INVITE_TRANSACTION_SUMMARY;")


        val cursor = db.rawQuery("select _id, TYPE, INVITE_NAME, SENDER_PHONE_NUMBER, RECEIVER_PHONE_NUMBER from TEMP_INVITE_TRANSACTION_SUMMARY", null)
        if (cursor.moveToFirst()) {
            do {
                val inviteId = cursor.getLong(cursor.getColumnIndex("_id"))
                val type = cursor.getInt(cursor.getColumnIndex("TYPE"))
                val inviteName: String? = cursor.getString(cursor.getColumnIndex("INVITE_NAME"))
                val sendersPhone: String = cursor.getString(cursor.getColumnIndex("SENDER_PHONE_NUMBER"))
                val receiverPhone: String = cursor.getString(cursor.getColumnIndex("RECEIVER_PHONE_NUMBER"))

                var toUser: Long
                var fromUser: Long

                if (type == Type.SENT.id) {
                    toUser = getOrCreateUserIdentityRecord(db = db, identity = receiverPhone, name = inviteName)
                    fromUser = getOrCreateUserIdentityRecord(db = db, identity = sendersPhone)
                } else {
                    toUser = getOrCreateUserIdentityRecord(db = db, identity = receiverPhone)
                    fromUser = getOrCreateUserIdentityRecord(db = db, identity = sendersPhone, name = inviteName)
                }

                db.execSQL("UPDATE INVITE_TRANSACTION_SUMMARY " +
                        "SET TO_USER_IDENTITY_ID=$toUser, FROM_USER_IDENTITY_ID=$fromUser " +
                        "WHERE _id=$inviteId")

            } while (cursor.moveToNext())
        }

        cursor.close()
        db.execSQL("DROP TABLE TEMP_INVITE_TRANSACTION_SUMMARY;")
    }

    internal fun migrateTransactionNotifications(db: Database) {
        db.execSQL("ALTER TABLE TRANSACTION_NOTIFICATION RENAME TO TEMP_TRANSACTION_NOTIFICATION;")
        db.execSQL("CREATE TABLE \"TRANSACTION_NOTIFICATION\" ( " +
                "\"_id\" INTEGER PRIMARY KEY , " +
                "\"MEMO\" TEXT, " +
                "\"IS_SHARED\" INTEGER NOT NULL , " +
                "\"AMOUNT\" INTEGER NOT NULL , " +
                "\"AMOUNT_CURRENCY\" TEXT, " +
                "\"TXID\" TEXT, " +
                "\"TO_USER_IDENTITY_ID\" INTEGER, " +
                "\"FROM_USER_IDENTITY_ID\" INTEGER);");

        db.execSQL("INSERT INTO \"TRANSACTION_NOTIFICATION\" \n" +
                "(" +
                "  \"_id\", " +
                "  \"MEMO\", " +
                "  \"IS_SHARED\", " +
                "  \"AMOUNT\", " +
                "  \"AMOUNT_CURRENCY\", " +
                "  \"TXID\"" +
                ") \n" +
                "SELECT " +
                "   \"_id\", " +
                "   \"MEMO\", " +
                "   \"IS_SHARED\", " +
                "   \"AMOUNT\", " +
                "   \"AMOUNT_CURRENCY\", " +
                "   \"TXID\" " +
                "FROM TEMP_TRANSACTION_NOTIFICATION;")

        val cursor = db.rawQuery("select _id, DISPLAY_NAME, PHONE_NUMBER from TEMP_TRANSACTION_NOTIFICATION", null)
        if (cursor.moveToFirst()) {
            do {
                val notificationId = cursor.getLong(cursor.getColumnIndex("_id"))
                val name: String? = cursor.getString(cursor.getColumnIndex("DISPLAY_NAME"))
                val phoneNumber: String? = cursor.getString(cursor.getColumnIndex("PHONE_NUMBER"))
                phoneNumber?.let {
                    val identityId = getOrCreateUserIdentityRecord(db, phoneNumber, name)
                    db.execSQL("UPDATE TRANSACTION_NOTIFICATION SET TO_USER_IDENTITY_ID=$identityId WHERE _id=$notificationId")
                }

            } while (cursor.moveToNext())
        }

        cursor.close()


        db.execSQL("DROP TABLE TEMP_TRANSACTION_NOTIFICATION;")
    }

    internal fun migrateTransactionInviteSummaries(db: Database) {
        db.execSQL("ALTER TABLE TRANSACTIONS_INVITES_SUMMARY RENAME TO TEMP_TRANSACTIONS_INVITES_SUMMARY")

        db.execSQL("CREATE TABLE TRANSACTIONS_INVITES_SUMMARY (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT," +
                "\"TRANSACTION_SUMMARY_ID\" INTEGER," +
                "\"TO_USER_IDENTITY_ID\" INTEGER," +
                "\"FROM_USER_IDENTITY_ID\" INTEGER," +
                "\"INVITE_SUMMARY_ID\" INTEGER," +
                "\"INVITE_TIME\" INTEGER NOT NULL ," +
                "\"BTC_TX_TIME\" INTEGER NOT NULL ," +
                "\"TRANSACTION_TX_ID\" TEXT," +
                "\"INVITE_TX_ID\" TEXT)")

        db.execSQL("INSERT INTO TRANSACTIONS_INVITES_SUMMARY " +
                "(" +
                "  \"_id\", " +
                "  \"TRANSACTION_SUMMARY_ID\", " +
                "  \"INVITE_SUMMARY_ID\", " +
                "  \"INVITE_TIME\", " +
                "  \"BTC_TX_TIME\", " +
                "  \"TRANSACTION_TX_ID\", " +
                "  \"INVITE_TX_ID\"" +
                ") " +
                "SELECT " +
                "  \"_id\", " +
                "  \"TRANSACTION_SUMMARY_ID\", " +
                "  \"INVITE_SUMMARY_ID\", " +
                "  \"INVITE_TIME\", " +
                "  \"BTC_TX_TIME\", " +
                "  \"TRANSACTION_TX_ID\", " +
                "  \"INVITE_TX_ID\"" +
                "FROM TEMP_TRANSACTIONS_INVITES_SUMMARY")

        // 1 - Save identities
        // 2 - Assign TX -- with phone number to the toUSER field")
        // 3 - lookup invites to take appropriate to/from users
        val cursor = db.rawQuery("select _id, TO_NAME, INVITE_SUMMARY_ID, TO_PHONE_NUMBER from TEMP_TRANSACTIONS_INVITES_SUMMARY", null)
        if (cursor.moveToFirst()) {
            do {
                val summaryId = cursor.getLong(cursor.getColumnIndex("_id"))
                val inviteId: Long = cursor.getLong(cursor.getColumnIndex("INVITE_SUMMARY_ID"))
                val name: String? = cursor.getString(cursor.getColumnIndex("TO_NAME"))
                val phoneNumber: String? = cursor.getString(cursor.getColumnIndex("TO_PHONE_NUMBER"))

                phoneNumber?.let {
                    val identityId = getOrCreateUserIdentityRecord(db, phoneNumber, name)
                    if (inviteId == 0L) {
                        db.execSQL("UPDATE TRANSACTIONS_INVITES_SUMMARY SET TO_USER_IDENTITY_ID=$identityId WHERE _id=$summaryId")
                    }
                }

                updateTransactionIdentitiesFromInvite(db, inviteId, summaryId)

            } while (cursor.moveToNext())
        }

        cursor.close()

        db.execSQL("DROP TABLE TEMP_TRANSACTIONS_INVITES_SUMMARY")
    }

    private fun updateTransactionIdentitiesFromInvite(db: Database, inviteId: Long, summaryId: Long) {
        if (inviteId == 0L) return
        val cursor = db.rawQuery("select TO_USER_IDENTITY_ID, FROM_USER_IDENTITY_ID " +
                "from INVITE_TRANSACTION_SUMMARY where _id=$inviteId", null)
        if (cursor.moveToFirst()) {
            val toUserId = cursor.getLong(cursor.getColumnIndex("TO_USER_IDENTITY_ID"))
            val fromUserId = cursor.getLong(cursor.getColumnIndex("FROM_USER_IDENTITY_ID"))
            if (fromUserId != 0L && toUserId != 0L) {
                db.execSQL("UPDATE TRANSACTIONS_INVITES_SUMMARY " +
                        "SET TO_USER_IDENTITY_ID=$toUserId, " +
                        "FROM_USER_IDENTITY_ID=$fromUserId " +
                        "WHERE _id=$summaryId")
            }
        }
        cursor.close()
    }

    private fun getOrCreateUserIdentityRecord(db: Database, identity: String, name: String? = null): Long {
        var cursor = db.rawQuery("select _id, DISPLAY_NAME from USER_IDENTITY where IDENTITY = \"$identity\"", null)
        var recordId = -1L
        var displayName: String? = null

        if (cursor.moveToFirst()) {
            recordId = cursor.getLong(cursor.getColumnIndex("_id"))
            displayName = cursor.getString(cursor.getColumnIndex("DISPLAY_NAME"))
        }
        cursor.close()

        if (recordId == -1L) {
            val phoneNumber = PhoneNumber(identity)
            val hash = hasher.hash("${phoneNumber.countryCode}${phoneNumber.nationalNumber}")
            db.execSQL("INSERT INTO USER_IDENTITY (IDENTITY, TYPE, HASH) VALUES ('$identity', 0, '$hash')")
            cursor = db.rawQuery("select _id from USER_IDENTITY where IDENTITY = \"$identity\"", null)
            cursor.moveToFirst()
            recordId = cursor.getLong(cursor.getColumnIndex("_id"))
            cursor.close()
        }

        if (!name.isNullOrEmpty() && name != displayName) {
            db.execSQL("UPDATE \"USER_IDENTITY\" SET \"DISPLAY_NAME\" = '$name' WHERE \"_id\" = $recordId")
        }

        return recordId
    }

    override fun getTargetVersion(): Int {
        return 33
    }

    override fun getPreviousMigration(): Migration {
        return Migrate_V32_to_V33()
    }

}