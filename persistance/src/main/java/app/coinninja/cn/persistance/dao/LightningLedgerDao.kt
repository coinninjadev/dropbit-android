package app.coinninja.cn.persistance.dao

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.room.*
import app.coinninja.cn.persistance.model.LightningInvoice
import app.dropbit.annotations.Mockable

@Dao
@Mockable
abstract class LightningInvoiceDao {

    @Query("Select * from LIGHTNING_INVOICE where IS_HIDDEN = 0 order by CREATED_AT DESC")
    abstract fun allVisibleLive(): LiveData<List<LightningInvoice>>

    @Query("Select * from LIGHTNING_INVOICE order by CREATED_AT DESC")
    abstract fun all(): List<LightningInvoice>

    @Query("select * from LIGHTNING_INVOICE where DIRECTION = :direction and TYPE = :type")
    abstract fun allByDirectionAndType(direction:Int, type:Int): Array<LightningInvoice>

    @WorkerThread
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(lightningLedger: LightningInvoice)

    @WorkerThread
    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract fun update(lightningLedger: LightningInvoice)

    @WorkerThread
    @Query("DELETE FROM LIGHTNING_INVOICE")
    abstract fun deleteAll()

    @Query("Select * from LIGHTNING_INVOICE where SERVER_ID = :serverId")
    abstract fun ledgerByServerId(serverId: String): LightningInvoice?

    fun insertOrUpdate(ledger: LightningInvoice) {
        var updated = false
        ledgerByServerId(ledger.serverId)?.let {
            it.createdAt = ledger.createdAt
            it.updatedAt = ledger.updatedAt
            it.expiresAt = ledger.expiresAt
            it.status = ledger.status
            it.direction = ledger.direction
            it.type = ledger.type
            it.isGenerated = ledger.isGenerated
            it.value = ledger.value
            it.networkFee = ledger.networkFee
            it.processingFee = ledger.processingFee
            it.addIndex = ledger.addIndex
            it.memo = ledger.memo
            it.request = ledger.request
            it.error = ledger.error
            it.isHidden = ledger.isHidden
            update(it)
            updated = true
        }

        if (!updated)
            insert(ledger)
    }


}
