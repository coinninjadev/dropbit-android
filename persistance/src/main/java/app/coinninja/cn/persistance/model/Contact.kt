package app.coinninja.cn.persistance.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import app.dropbit.annotations.Mockable

@Mockable
@Entity(
        tableName = "CONTACT"
)
data class Contact(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "_id")
        var id: Long = 0,

        @ColumnInfo(name = "PHONE_NUMBER_HASH")
        var phoneNumberHash: String = ""
)
