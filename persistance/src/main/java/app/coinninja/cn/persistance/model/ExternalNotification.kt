package app.coinninja.cn.persistance.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import app.dropbit.annotations.Mockable

@Mockable
@Entity(
        tableName = "EXTERNAL_NOTIFICATION",
        indices = [
            Index(value = ["MESSAGE"], name = "EXTERNAL_NOTIFICATION__MESSAGE", unique = false)
        ]
)
data class ExternalNotification(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "_id")
        var id: Long = 0,
        @ColumnInfo(name = "MESSAGE")
        var message: String = "",
        @ColumnInfo(name = "EXTRA_DATA")
        var extraData: String = ""
)