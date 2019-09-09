package app.coinninja.cn.persistance.dao

import androidx.annotation.WorkerThread
import androidx.room.*
import app.coinninja.cn.persistance.model.LightningAccount
import app.coinninja.cn.persistance.model.User
import app.dropbit.annotations.Mockable

@Dao
@Mockable
abstract class UserDao {
    @Query("Select * from USER order by _id limit 1")
    abstract fun user(): User?

    @WorkerThread
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insert(lightningAccount: LightningAccount)

    @WorkerThread
    @Update(onConflict = OnConflictStrategy.IGNORE)
    abstract fun update(lightningAccount: LightningAccount)

    @WorkerThread
    @Query("DELETE FROM lightning_account")
    abstract fun deleteAll()
}