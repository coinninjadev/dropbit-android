package app.coinninja.cn.persistance.model

import androidx.room.*
import app.coinninja.cn.persistance.converter.AccountStatusConverter
import app.dropbit.annotations.Mockable

@Mockable
@Entity(
        tableName = "ACCOUNT",
        indices = [
            Index(
                    value = ["WALLET_ID"],
                    name = "ACCOUNT_WALLET_ID",
                    unique = false
            )
        ],
        foreignKeys = [
            ForeignKey(
                    entity = Wallet::class,
                    parentColumns = arrayOf("_id"),
                    childColumns = arrayOf("WALLET_ID"),
                    onDelete = ForeignKey.CASCADE
            )
        ]
)
@TypeConverters(AccountStatusConverter::class)
data class Account(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "_id")
        var id: Long = 0,

        @ColumnInfo(name = "WALLET_ID")
        var walletId: Long? = null,

        @ColumnInfo(name = "CN_USER_ID")
        var cnUserId: String = "",

        @ColumnInfo(name = "STATUS")
        var index: AccountStatus = AccountStatus.UNVERIFIED,

        @ColumnInfo(name = "PHONE_NUMBER_HASH")
        var phoneNumberHash: String = "",

        @ColumnInfo(name = "PHONE_NUMBER")
        var phoneNumber: String = "",

        @ColumnInfo(name = "VERIFICATION_TTL")
        var verification_ttl: Long = 0,

        @ColumnInfo(name = "IS_PRIVATE")
        var isPrivate: Boolean = false
)