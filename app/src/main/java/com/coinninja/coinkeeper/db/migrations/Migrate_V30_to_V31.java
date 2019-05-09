package com.coinninja.coinkeeper.db.migrations;

import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.coinninja.coinkeeper.db.AbstractMigration;
import com.coinninja.coinkeeper.db.Migration;
import com.coinninja.coinkeeper.model.db.enums.IdentityType;

import org.greenrobot.greendao.database.Database;

public class Migrate_V30_to_V31 extends AbstractMigration {

    @Override
    protected void applyMigration(@NonNull Database db, int currentVersion) {
        db.execSQL("CREATE TABLE IF NOT EXISTS \"DROPBIT_ME_IDENTITY\" (" + //
                "\"_id\" INTEGER PRIMARY KEY ," + // 0: id
                "\"TYPE\" INTEGER," + // 1: type
                "\"IDENTITY\" TEXT," + // 2: identity
                "\"HANDLE\" TEXT," + // 3: handle
                "\"HASH\" TEXT," + // 4: hash
                "\"ACCOUNT_ID\" INTEGER);"); // 5: accountId

        db.execSQL("ALTER TABLE ACCOUNT ADD COLUMN \"IS_PRIVATE\" INTEGER");
        db.execSQL("update ACCOUNT set IS_PRIVATE = 0");
        Cursor cursor = db.rawQuery("select * from account", null);

        if (cursor.moveToFirst()) {
            String hash = cursor.getString(cursor.getColumnIndex("PHONE_NUMBER_HASH"));
            String identity =
                    cursor.getString(cursor.getColumnIndex("PHONE_NUMBER"));
            if (identity != null) {
                String handle = hash.substring(0, 12);
                String accountId = cursor.getString(cursor.getColumnIndex("_id"));
                int type = IdentityType.PHONE.getId();
                db.execSQL(String.format("insert into DROPBIT_ME_IDENTITY (" +
                                "\"TYPE\"," +
                                "\"IDENTITY\", " +
                                "\"HANDLE\", " +
                                "\"HASH\", " +
                                "\"ACCOUNT_ID\"" +
                                ") values (%s, \"%s\", \"%s\", \"%s\", %s)",
                        type, identity, handle, hash, accountId));
            }
        }
        cursor.close();
    }

    @Nullable
    @Override
    public Migration getPreviousMigration() {
        return new Migrate_V29_to_V30();
    }

    @Override
    public int getTargetVersion() {
        return 30;
    }

    @Override
    public int getMigratedVersion() {
        return 31;
    }
}
