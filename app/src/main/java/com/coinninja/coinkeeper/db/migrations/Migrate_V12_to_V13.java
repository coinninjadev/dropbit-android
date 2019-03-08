package com.coinninja.coinkeeper.db.migrations;

import android.database.Cursor;

import com.coinninja.coinkeeper.db.AbstractMigration;
import com.coinninja.coinkeeper.db.Migration;

import org.greenrobot.greendao.database.Database;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Migrate_V12_to_V13 extends AbstractMigration {
    @Override
    protected void applyMigration(@NonNull Database db, int currentVersion) {
        onUpdate(db);
        onSetInitalValue(db);
    }


    @Nullable
    @Override
    public Migration getPreviousMigration() {
        return new Migrate_V11_to_V12();
    }

    @Override
    public int getTargetVersion() {
        return 12;
    }

    @Override
    public int getMigratedVersion() {
        return 13;
    }

    private void onUpdate(Database db) {
        db.execSQL("ALTER TABLE WALLET ADD COLUMN LAST_USDPRICE INTEGER");
        db.execSQL("ALTER TABLE WALLET ADD COLUMN LAST_FEE TEXT");
    }

    private void onSetInitalValue(Database db) {
        Cursor cursor = db.rawQuery("select _id from WALLET", null);

        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndex("_id"));
            String sql = String.format("update INVITE_TRANSACTION_SUMMARY set LAST_FEE ='0', LAST_USDPRICE = 0 where _id=%s", id);
            db.execSQL(sql);
        }
    }
}
