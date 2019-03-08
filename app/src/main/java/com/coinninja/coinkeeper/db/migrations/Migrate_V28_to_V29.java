package com.coinninja.coinkeeper.db.migrations;

import android.database.Cursor;

import com.coinninja.coinkeeper.db.AbstractMigration;
import com.coinninja.coinkeeper.db.Migration;

import org.greenrobot.greendao.database.Database;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Migrate_V28_to_V29 extends AbstractMigration {

    @Override
    protected void applyMigration(@NonNull Database db, int currentVersion) {
        db.execSQL("UPDATE ACCOUNT SET PHONE_NUMBER = '+1' || PHONE_NUMBER WHERE PHONE_NUMBER IS NOT NULL AND PHONE_NUMBER != \"\" AND PHONE_NUMBER NOT LIKE '+1%';");
        db.execSQL("UPDATE INVITE_TRANSACTION_SUMMARY SET SENDER_PHONE_NUMBER = '+1' || SENDER_PHONE_NUMBER WHERE SENDER_PHONE_NUMBER IS NOT NULL AND SENDER_PHONE_NUMBER != \"\" AND SENDER_PHONE_NUMBER NOT LIKE '+1%';");
        db.execSQL("UPDATE INVITE_TRANSACTION_SUMMARY SET RECEIVER_PHONE_NUMBER = '+1' || RECEIVER_PHONE_NUMBER WHERE RECEIVER_PHONE_NUMBER IS NOT NULL AND RECEIVER_PHONE_NUMBER != \"\" AND RECEIVER_PHONE_NUMBER NOT LIKE '+1%';");
        db.execSQL("UPDATE TRANSACTION_NOTIFICATION SET PHONE_NUMBER = '+1' || PHONE_NUMBER WHERE PHONE_NUMBER IS NOT NULL AND PHONE_NUMBER != \"\" AND PHONE_NUMBER NOT LIKE '+1%';");
        db.execSQL("UPDATE TRANSACTIONS_INVITES_SUMMARY SET TO_PHONE_NUMBER = '+1' || TO_PHONE_NUMBER WHERE TO_PHONE_NUMBER IS NOT NULL AND TO_PHONE_NUMBER != \"\" AND TO_PHONE_NUMBER NOT LIKE '+1%';");
    }

    @Nullable
    @Override
    public Migration getPreviousMigration() {
        return new Migrate_V27_to_V28();
    }

    @Override
    public int getTargetVersion() {
        return 28;
    }

    @Override
    public int getMigratedVersion() {
        return 29;
    }
}
