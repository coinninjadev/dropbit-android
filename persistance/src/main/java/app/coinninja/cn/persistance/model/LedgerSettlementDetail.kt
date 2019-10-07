package app.coinninja.cn.persistance.model

import androidx.room.TypeConverters
import app.coinninja.cn.persistance.converter.*
import app.dropbit.annotations.Mockable
import app.dropbit.commons.currency.BTCCurrency
import app.dropbit.commons.currency.USDCurrency
import app.dropbit.commons.currency.toBTCCurrency
import app.dropbit.commons.currency.toUSDCurrency
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import java.util.*

@Mockable
@TypeConverters(
        LedgerDirectionConverter::class,
        LedgerTypeConverter::class,
        LedgerStatusConverter::class,
        SendTypeConverter::class,
        BTCStateConverter::class,
        IdentityTypeConverter::class
)
data class LedgerSettlementDetail(
        // Invoice Record Data
        val invoiceValue: Long? = null,
        val invoiceNetworkFee: Long? = null,
        val invoiceProcessingFee: Long? = null,
        val invoiceDirection: LedgerDirection? = null,
        val invoiceType: LedgerType? = null,
        val invoiceStatus: LedgerStatus? = null,
        val invoiceCreatedAt: Date? = null,
        val invoiceMemo: String? = null,

        val inviteValue: Long? = null,
        val inviteUsdValue: Long? = null,
        val inviteType: SendType? = null,
        val inviteState: BTCState? = null,

        val toUserType: IdentityType? = null,
        val toUserIdentity: String? = null,
        val toUserDisplayName: String? = null,
        val toUserHandle: String? = null,
        val toUserAvatar: String? = null,

        val fromUserType: IdentityType? = null,
        val fromUserIdentity: String? = null,
        val fromUserDisplayName: String? = null,
        val fromUserHandle: String? = null,
        val fromUserAvatar: String? = null,

        val createdAt: Date? = null
) {

    val cryptoAmount: BTCCurrency
        get() =
            inviteValue?.toBTCCurrency()
                    ?: invoiceValue?.toBTCCurrency()
                    ?: 0L.toBTCCurrency()

    fun usdValueConsidering(marketValue: USDCurrency): USDCurrency {
        return inviteUsdValue?.toUSDCurrency()
                ?: cryptoAmount.toUSD(marketValue)
    }

    val avatar: String? get() = when (paymentType) {
        PaymentType.ReceivedInvite,
        PaymentType.PendingReceiveInvoice,
        PaymentType.CompletedReceiveInvoice,
        PaymentType.ExpiredInvoice,
        PaymentType.ExpiredReceivedInvite,
        PaymentType.CanceledReceivedInvite -> fromUserAvatar

        PaymentType.FailedInvoice,
        PaymentType.CompletedPayment,
        PaymentType.ExpiredSentInvite,
        PaymentType.SentInvite,
        PaymentType.CanceledSentInvite -> toUserAvatar

        else -> null
    }

    fun identityFormatted(): String? = when (paymentType) {
        PaymentType.ReceivedInvite,
        PaymentType.PendingReceiveInvoice,
        PaymentType.CompletedReceiveInvoice,
        PaymentType.ExpiredInvoice,
        PaymentType.ExpiredReceivedInvite,
        PaymentType.CanceledReceivedInvite -> friendlyFromIdentity()

        PaymentType.FailedInvoice,
        PaymentType.CompletedPayment,
        PaymentType.ExpiredSentInvite,
        PaymentType.SentInvite,
        PaymentType.CanceledSentInvite -> friendlyToIdentity()


        else -> null
    }

    private fun friendlyToIdentity(): String? {
        return toUserType?.let {
            return considerIdentityFrom(it, toUserIdentity, toUserDisplayName, toUserHandle)
        }

    }

    private fun friendlyFromIdentity(): String? {
        return fromUserType?.let {
            return considerIdentityFrom(it, fromUserIdentity, fromUserDisplayName, fromUserHandle)
        }
    }

    private fun considerIdentityFrom(type: IdentityType,
                                     identity: String?,
                                     displayName: String?,
                                     fromUserHandle: String?
    ): String? = displayName ?: when (type) {
        IdentityType.PHONE -> {
            identity.let {
                try {
                    val util = PhoneNumberUtil.getInstance()
                    val phoneNumber = util.parse(it, Locale.US.country)
                    util.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL)
                } catch (e: NumberParseException) {
                    e.printStackTrace()
                    null
                }
            }
        }
        IdentityType.TWITTER -> {
            fromUserHandle?.let { "@${it}" }
        }
        else -> null
    }


    val paymentType: PaymentType
        get() = when {
            (invoiceStatus == LedgerStatus.FAILED) -> PaymentType.FailedInvoice
            (invoiceStatus == LedgerStatus.EXPIRED) -> PaymentType.ExpiredInvoice
            (invoiceDirection == LedgerDirection.IN && invoiceType == LedgerType.BTC) ->
                PaymentType.TransferIn
            (invoiceDirection == LedgerDirection.OUT && invoiceType == LedgerType.BTC) ->
                PaymentType.TransferOut
            (invoiceDirection == LedgerDirection.IN
                    && invoiceType == LedgerType.LIGHTNING
                    && invoiceStatus == LedgerStatus.PENDING) ->
                PaymentType.PendingReceiveInvoice
            (invoiceDirection == LedgerDirection.IN
                    && invoiceType == LedgerType.LIGHTNING
                    && invoiceStatus == LedgerStatus.COMPLETED) ->
                PaymentType.CompletedReceiveInvoice
            (invoiceDirection == LedgerDirection.OUT && invoiceType == LedgerType.LIGHTNING) ->
                PaymentType.CompletedPayment

            inviteState == BTCState.EXPIRED && inviteType == SendType.LIGHTNING_SENT ->
                PaymentType.ExpiredSentInvite
            inviteState == BTCState.CANCELED && inviteType == SendType.LIGHTNING_SENT ->
                PaymentType.CanceledSentInvite
            inviteState == BTCState.CANCELED && inviteType == SendType.LIGHTNING_RECEIVED ->
                PaymentType.CanceledReceivedInvite
            inviteState == BTCState.EXPIRED && inviteType == SendType.LIGHTNING_RECEIVED ->
                PaymentType.ExpiredReceivedInvite
            inviteType == SendType.LIGHTNING_SENT -> PaymentType.SentInvite
            inviteType == SendType.LIGHTNING_RECEIVED -> PaymentType.ReceivedInvite

            else -> PaymentType.Unknown
        }

    enum class PaymentType {
        TransferIn,
        TransferOut,
        PendingReceiveInvoice,
        CompletedReceiveInvoice,
        FailedInvoice,
        ExpiredInvoice,
        CompletedPayment,
        SentInvite,
        ReceivedInvite,
        ExpiredReceivedInvite,
        CanceledReceivedInvite,
        ExpiredSentInvite,
        CanceledSentInvite,
        Unknown
    }
}