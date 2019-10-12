package app.coinninja.cn.persistance

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

class WipDevDropbitConnectionCallback : RoomDatabase.Callback() {

    override fun onOpen(db: SupportSQLiteDatabase) {
        super.onOpen(db)
        dropAllTables(db)
        createAllTables(db)
    }

    fun createAllTables(_db: SupportSQLiteDatabase) {
        _db.execSQL("CREATE TABLE IF NOT EXISTS `lightning_account` (`uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `address` TEXT NOT NULL, `balance` INTEGER NOT NULL, `pending_in` INTEGER NOT NULL, `pending_out` INTEGER NOT NULL, `created_at` TEXT NOT NULL, `updated_at` TEXT NOT NULL)");
        _db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        _db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '54dad0d25b26832c15302887c35bbe53')");
    }

    fun dropAllTables(_db: SupportSQLiteDatabase) {
        _db.execSQL("DROP TABLE IF EXISTS `lightning_account`");
    }

}
