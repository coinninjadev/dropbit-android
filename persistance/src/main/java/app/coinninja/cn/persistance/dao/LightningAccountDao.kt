package app.coinninja.cn.persistance.dao

import androidx.annotation.WorkerThread
import androidx.room.*
import app.coinninja.cn.persistance.model.LightningAccount
import app.dropbit.annotations.Mockable

@Dao
@Mockable
abstract class LightningAccountDao {
    @Query("Select * FROM LIGHTNING_ACCOUNT order By _id ASC")
    abstract fun all(): List<LightningAccount>

    @WorkerThread
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insert(lightningAccount: LightningAccount)

    @WorkerThread
    @Update(onConflict = OnConflictStrategy.IGNORE)
    abstract fun update(lightningAccount: LightningAccount)

    @WorkerThread
    @Query("DELETE FROM LIGHTNING_ACCOUNT")
    abstract fun deleteAll()

    @Query("SELECT * from LIGHTNING_ACCOUNT limit 1")
    abstract fun getAccount(): LightningAccount?

    @Query("""
        SELECT  SUM(balance) available
        FROM
        (
            select balance from LIGHTNING_ACCOUNT 
            UNION ALL
            select (sum(VALUE_SATOSHIS + VALUE_FEES_SATOSHIS) * -1) as balance 
            from INVITE_TRANSACTION_SUMMARY as ITS where TYPE = 20 and BTC_STATE = 0
        ) s
        
    """)
    abstract fun availableBalance(): Long

    fun insertOrUpdate(lightningAccount: LightningAccount) {
        var updated = false
        getAccount()?.let {
            it.serverId = lightningAccount.serverId
            it.address = lightningAccount.address
            it.balance = lightningAccount.balance
            it.pendingIn = lightningAccount.pendingIn
            it.pendingOut = lightningAccount.pendingOut
            it.createdAt = lightningAccount.createdAt
            it.updatedAt = lightningAccount.updatedAt
            it.isLocked = lightningAccount.isLocked
            update(it)
            updated = true
        }

        if (!updated)
            insert(lightningAccount)

    }

}