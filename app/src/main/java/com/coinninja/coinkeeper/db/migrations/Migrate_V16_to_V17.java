package com.coinninja.coinkeeper.db.migrations;

import android.database.Cursor;

import com.coinninja.coinkeeper.db.AbstractMigration;
import com.coinninja.coinkeeper.db.Migration;

import org.greenrobot.greendao.database.Database;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Migrate_V16_to_V17 extends AbstractMigration {

    @Override
    protected void applyMigration(@NonNull Database db, int currentVersion) {
        rebuildNotificationTable(db);
    }

    @Nullable
    @Override
    public Migration getPreviousMigration() {

        return new Migrate_V15_to_V16();
    }

    @Override
    public int getTargetVersion() {
        return 16;
    }

    @Override
    public int getMigratedVersion() {
        return 17;
    }

    private void rebuildNotificationTable(Database db) {
        ArrayList<TempNotificationHolder> allPreviousMessage;
        allPreviousMessage = gatherPreviousMessagesInDatabase(db);

        dropTable(db);
        createTable(db);
        insertAll(db, allPreviousMessage);
    }

    private ArrayList<TempNotificationHolder> gatherPreviousMessagesInDatabase(Database db) {
        ArrayList<TempNotificationHolder> allPreviousMessage = new ArrayList<>();
        String sql = "SELECT MESSAGE, PRIORITY, HAS_BEEN_SEEN, WALLET_ID FROM INTERNAL_NOTIFICATION";
        Cursor cursor = db.rawQuery(sql, null);

        while (cursor.moveToNext()) {
            TempNotificationHolder tempNotifyHaler;

            String message = cursor.getString(cursor.getColumnIndex("MESSAGE"));
            int hasBeenSeen = cursor.getInt(cursor.getColumnIndex("HAS_BEEN_SEEN"));
            int walletId = cursor.getInt(cursor.getColumnIndex("WALLET_ID"));
            int priority = cursor.getInt(cursor.getColumnIndex("PRIORITY"));
            int messageLevel = 0;//0 = message level info

            tempNotifyHaler = new TempNotificationHolder(message, hasBeenSeen, walletId, messageLevel, priority);
            allPreviousMessage.add(tempNotifyHaler);
        }

        cursor.close();

        return allPreviousMessage;
    }

    private void createTable(Database db) {
        db.execSQL("CREATE TABLE \"INTERNAL_NOTIFICATION\" (" + //
                "\"PRIORITY\" INTEGER," + // 0: priority
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE ," + // 1: id
                "\"MESSAGE\" TEXT," + // 2: message
                "\"HAS_BEEN_SEEN\" INTEGER NOT NULL ," + // 3: hasBeenSeen
                "\"MESSAGE_LEVEL\" INTEGER," + // 4: messageLevel
                "\"SERVER_UUID\" TEXT," + // 5: serverUUID
                "\"CLICK_ACTION\" TEXT," + // 6: clickAction
                "\"WALLET_ID\" INTEGER);"); // 7: walletId
    }

    private void dropTable(Database db) {
        String sql = "DROP TABLE \"INTERNAL_NOTIFICATION\"";
        db.execSQL(sql);
    }

    private void insertAll(Database db, ArrayList<TempNotificationHolder> allPreviousMessage) {
        if (!allPreviousMessage.isEmpty()) {
            for (TempNotificationHolder tempHolder : allPreviousMessage) {
                insertIntoTablet(db, tempHolder);
            }
        }
    }

    private void insertIntoTablet(Database db, TempNotificationHolder tempHolder) {
        String sqlInsert = "INSERT INTO INTERNAL_NOTIFICATION(MESSAGE,HAS_BEEN_SEEN,MESSAGE_LEVEL,PRIORITY,WALLET_ID) VALUES(?,?,?,?,?)";
        Object[] values = new Object[]{tempHolder.message, tempHolder.hasBeenSeen, tempHolder.messageLevel, tempHolder.priority, tempHolder.walletID};
        db.execSQL(sqlInsert, values);
    }


    class TempNotificationHolder {
        final String message;
        final int hasBeenSeen;
        final int walletID;
        final int messageLevel;
        final int priority;

        public TempNotificationHolder(String message, int hasBeenSeen, int walletID, int messageLevel, int priority) {
            this.message = message;
            this.hasBeenSeen = hasBeenSeen;
            this.walletID = walletID;
            this.messageLevel = messageLevel;
            this.priority = priority;
        }
    }
}
