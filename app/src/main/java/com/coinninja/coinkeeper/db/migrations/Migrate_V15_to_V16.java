package com.coinninja.coinkeeper.db.migrations;

import android.database.Cursor;

import com.coinninja.coinkeeper.db.AbstractMigration;
import com.coinninja.coinkeeper.db.Migration;

import org.greenrobot.greendao.database.Database;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Migrate_V15_to_V16 extends AbstractMigration {

    @Override
    protected void applyMigration(@NonNull Database db, int currentVersion) {
        alterDb(db);
        populateHistoricValue(db);
    }

    @Nullable
    @Override
    public Migration getPreviousMigration() {
        return new Migrate_V14_to_V15();
    }

    @Override
    public int getTargetVersion() {
        return 15;
    }

    @Override
    public int getMigratedVersion() {
        return 16;
    }

    private void alterDb(Database db) {
        db.execSQL("ALTER TABLE INVITE_TRANSACTION_SUMMARY" +
                " ADD COLUMN HISTORIC_VALUE INTEGER");
    }

    private void populateHistoricValue(Database db) {
        Cursor cursor = db.rawQuery("select _id, HISTORIC_USDVALUE from INVITE_TRANSACTION_SUMMARY", null);

        while (cursor.moveToNext()) {
            String value = cursor.getString(cursor.getColumnIndex("HISTORIC_USDVALUE"));
            long id = cursor.getLong(cursor.getColumnIndex("_id"));
            long converted;

            if (null != value && !value.isEmpty()) {
                try {
                    converted = ((long) (Double.parseDouble(value) * 100));
                } catch (Exception ex) {
                    continue;
                }

                String sql = String.format("update INVITE_TRANSACTION_SUMMARY set HISTORIC_VALUE = %1s where _id = %2s", converted, id);
                db.execSQL(sql);
            }

        }

        cursor.close();
    }

}
