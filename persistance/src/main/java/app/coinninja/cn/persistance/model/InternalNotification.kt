package app.coinninja.cn.persistance.model

import android.net.Uri
import androidx.room.*
import app.coinninja.cn.persistance.converter.MessageLevelConverter
import app.dropbit.annotations.Mockable

@Mockable
@Entity(
        tableName = "INTERNAL_NOTIFICATION",
        indices = [
            Index(
                    value = ["WALLET_ID"],
                    name = "INTERNAL_NOTIFICATION__WALLET_ID",
                    unique = false)
        ],
        foreignKeys = [
            ForeignKey(
                    entity = Wallet::class,
                    parentColumns = arrayOf("_id"),
                    childColumns = arrayOf("WALLET_ID"),
                    onDelete = ForeignKey.NO_ACTION
            )
        ]
)
@TypeConverters(
        MessageLevelConverter::class
)
data class InternalNotification(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "_id")
        var id: Long = 0,

        @ColumnInfo(name = "WALLET_ID")
        var walletId: Long? = null,

        @ColumnInfo(name = "MESSAGE")
        var message: String = "",

        @ColumnInfo(name = "HAS_BEEN_SEEN")
        var hasBeenSeen: Boolean = false,

        @ColumnInfo(name = "MESSAGE_LEVEL")
        var messageLevel: MessageLevel = MessageLevel.INFO,

        @ColumnInfo(name = "SERVER_UUID")
        var serverUUID: String = "",

        @ColumnInfo(name = "CLICK_ACTION")
        var clickAction: Uri? = null
)