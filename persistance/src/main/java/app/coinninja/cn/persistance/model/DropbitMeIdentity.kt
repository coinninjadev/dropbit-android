package app.coinninja.cn.persistance.model

import androidx.room.*
import app.coinninja.cn.persistance.converter.AccountStatusConverter
import app.coinninja.cn.persistance.converter.IdentityTypeConverter
import app.dropbit.annotations.Mockable

@Mockable
@Entity(
        tableName = "DROPBIT_ME_IDENTITY",
        indices = [
            Index(
                    value = ["ACCOUNT_ID"],
                    name = "DROPBIT_ME_IDENTITY__ACCOUNT_ID",
                    unique = false),
            Index(
                    value = ["SERVER_ID"],
                    name = "DROPBIT_ME_IDENTITY__SERVER_ID",
                    unique = true)
        ],
        foreignKeys = [
            ForeignKey(
                    entity = Account::class,
                    parentColumns = arrayOf("_id"),
                    childColumns = arrayOf("ACCOUNT_ID"),
                    onDelete = ForeignKey.CASCADE
            )
        ]
)

@TypeConverters(
        IdentityTypeConverter::class,
        AccountStatusConverter::class
)
data class DropbitMeIdentity(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "_id")
        var id: Long = 0,
        @ColumnInfo(name = "TYPE")
        var type: IdentityType = IdentityType.UNKNOWN,
        @ColumnInfo(name = "IDENTITY")
        var identity: String = "",
        @ColumnInfo(name = "SERVER_ID")
        var serverId: String = "",
        @ColumnInfo(name = "HANDLE")
        var handle: String = "",
        @ColumnInfo(name = "HASH")
        var hash: String = "",
        @ColumnInfo(name = "STATUS")
        var status: AccountStatus = AccountStatus.UNVERIFIED,
        @ColumnInfo(name = "ACCOUNT_ID")
        var accountId: Long? = null
)
