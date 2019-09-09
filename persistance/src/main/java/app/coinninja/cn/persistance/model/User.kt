package app.coinninja.cn.persistance.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import app.dropbit.annotations.Mockable


@Mockable
@Entity(
        tableName = "USER"
)
data class User(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "_id")
        var id: Long,

        @ColumnInfo(name = "PIN")
        var pin: String = "",

        @ColumnInfo(name = "UID")
        var uid: String = "",

        @ColumnInfo(name = "COMPLETED_TRAINING")
        var completedTraining: Boolean = false,

        @ColumnInfo(name = "LOCKED_UNTIL_TIME")
        var lockedUntilTime: Long = 0
)