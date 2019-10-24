package app.coinninja.cn.persistance

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteOpenHelper
import app.coinninja.cn.persistance.converter.BTCCurrencyConverter
import app.coinninja.cn.persistance.converter.USDCurrencyConverter
import app.coinninja.cn.persistance.converter.UriConverter
import app.coinninja.cn.persistance.dao.LedgerSettlementDao
import app.coinninja.cn.persistance.dao.LightningAccountDao
import app.coinninja.cn.persistance.dao.LightningInvoiceDao
import app.coinninja.cn.persistance.migration.MIGRATION_1_2.Companion.MIGRATION_1_2
import app.coinninja.cn.persistance.model.*

@Database(
        entities = [
            Account::class,
            Address::class,
            BroadcastBtcInvite::class,
            Contact::class,
            DropbitMeIdentity::class,
            ExternalNotification::class,
            FundingStat::class,
            InternalNotification::class,
            InviteTransactionSummary::class,
            LedgerSettlement::class,
            LightningAccount::class,
            LightningInvoice::class,
            TransactionSummary::class,
            TransactionsInvitesSummary::class,
            TransactionNotification::class,
            TargetStat::class,
            User::class,
            UserIdentity::class,
            Wallet::class,
            Word::class
        ],
        version = 41,
        exportSchema = true
)

@TypeConverters(
        BTCCurrencyConverter::class,
        USDCurrencyConverter::class,
        UriConverter::class
)
abstract class DropbitDatabase : RoomDatabase() {

    abstract fun lightningAccountDao(): LightningAccountDao
    abstract fun lightningInvoiceDao(): LightningInvoiceDao
    abstract val ledgerSettlementDao: LedgerSettlementDao

    companion object {
        @Volatile
        internal var INSTANCE: DropbitDatabase? = null

        fun getDatabase(context: Context, databaseName: String,
                        dropbitDatabaseConnectionCallback: Callback,
                        openHelperFactory: SupportSQLiteOpenHelper.Factory? = null): DropbitDatabase {
            INSTANCE?.let { return it }

            synchronized(this) {
                val databaseBuilder = Room.databaseBuilder(context.applicationContext, DropbitDatabase::class.java, databaseName)
                        .allowMainThreadQueries()
                        .addMigrations(MIGRATION_1_2)
                        .addCallback(dropbitDatabaseConnectionCallback)

                openHelperFactory?.let {
                    databaseBuilder.openHelperFactory(openHelperFactory)
                }

                val instance = databaseBuilder.build()
                INSTANCE = instance
                return instance
            }
        }

    }
}
