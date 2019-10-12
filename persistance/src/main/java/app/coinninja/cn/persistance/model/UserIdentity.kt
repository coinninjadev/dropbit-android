package app.coinninja.cn.persistance.model

import androidx.room.*
import app.coinninja.cn.persistance.converter.IdentityTypeConverter
import app.coinninja.cn.persistance.model.IdentityType.UNKNOWN
import app.dropbit.annotations.Mockable

@Mockable
@Entity(
        tableName = "USER_IDENTITY",
        indices = [
            Index(
                    value = ["IDENTITY"],
                    name = "USER_IDENTITY__IDENTITY",
                    unique = true)
        ]
)
@TypeConverters(IdentityTypeConverter::class)
data class UserIdentity(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "_id")
        var id: Long = 0,

        @ColumnInfo(name = "IDENTITY")
        var identity: String = "",

        @ColumnInfo(name = "TYPE")
        var IdentityType: IdentityType = UNKNOWN,

        @ColumnInfo(name = "DISPLAY_NAME")
        var displayName: String = "",

        @ColumnInfo(name = "HANDLE")
        var handle: String = "",

        @ColumnInfo(name = "HASH")
        var hash: String = "",

        @ColumnInfo(name = "AVATAR")
        var avatar: String = ""
)