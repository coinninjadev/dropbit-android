package app.coinninja.cn.persistance.model

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.dropbit.commons.currency.USDCurrency
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LedgerSettlementDetailTest {

    @Test
    fun returns_avatar_for_settlement() {
        var settlement = LedgerSettlementDetail(
                inviteState = BTCState.UNFULFILLED,
                inviteType = SendType.LIGHTNING_SENT,
                inviteUsdValue = 10_00,
                inviteValue = 100_000,
                toUserIdentity = "0000000000000",
                toUserAvatar = "toAvatar",
                toUserType = IdentityType.TWITTER,
                fromUserIdentity = "111111111111",
                fromUserType = IdentityType.TWITTER,
                fromUserAvatar = "fromAvatar"
        )

        assertThat(settlement.avatar).isEqualTo("toAvatar")

        settlement = LedgerSettlementDetail(
                inviteState = BTCState.UNFULFILLED,
                inviteType = SendType.LIGHTNING_RECEIVED,
                inviteUsdValue = 10_00,
                inviteValue = 100_000,
                toUserIdentity = "0000000000000",
                toUserAvatar = "toAvatar",
                toUserType = IdentityType.TWITTER,
                fromUserIdentity = "111111111111",
                fromUserType = IdentityType.TWITTER,
                fromUserAvatar = "fromAvatar"
        )

        assertThat(settlement.avatar).isEqualTo("fromAvatar")
    }

    @Test
    fun identity_formatted() {

        var settlement = LedgerSettlementDetail(
                inviteState = BTCState.UNFULFILLED,
                inviteType = SendType.LIGHTNING_SENT,
                inviteUsdValue = 10_00,
                inviteValue = 100_000,
                toUserIdentity = "+13305550000",
                toUserType = IdentityType.PHONE,
                fromUserIdentity = "+13305551111",
                fromUserType = IdentityType.PHONE
        )

        assertThat(settlement.identityFormatted()).isEqualTo("+1 330-555-0000")

        settlement = LedgerSettlementDetail(
                inviteState = BTCState.UNFULFILLED,
                inviteType = SendType.LIGHTNING_SENT,
                inviteUsdValue = 10_00,
                inviteValue = 100_000,
                toUserIdentity = "+13305550000",
                toUserDisplayName = "Joey",
                toUserType = IdentityType.PHONE,
                fromUserIdentity = "+13305551111",
                fromUserType = IdentityType.PHONE
        )

        assertThat(settlement.identityFormatted()).isEqualTo("Joey")

        settlement = LedgerSettlementDetail(
                inviteState = BTCState.UNFULFILLED,
                inviteType = SendType.LIGHTNING_RECEIVED,
                inviteUsdValue = 10_00,
                inviteValue = 100_000,
                toUserIdentity = "+13305550000",
                toUserType = IdentityType.PHONE,
                fromUserIdentity = "+13305551111",
                fromUserType = IdentityType.PHONE
        )

        assertThat(settlement.identityFormatted()).isEqualTo("+1 330-555-1111")

        settlement = LedgerSettlementDetail(
                inviteState = BTCState.UNFULFILLED,
                inviteType = SendType.LIGHTNING_SENT,
                inviteUsdValue = 10_00,
                inviteValue = 100_000,
                toUserIdentity = "12345678902",
                toUserType = IdentityType.TWITTER,
                toUserHandle = "JOE",
                fromUserIdentity = "12345678901",
                fromUserType = IdentityType.TWITTER,
                fromUserHandle = "Blue"
        )

        assertThat(settlement.identityFormatted()).isEqualTo("@JOE")

        settlement = LedgerSettlementDetail(
                inviteState = BTCState.UNFULFILLED,
                inviteType = SendType.LIGHTNING_RECEIVED,
                inviteUsdValue = 10_00,
                inviteValue = 100_000,
                toUserIdentity = "12345678902",
                toUserType = IdentityType.TWITTER,
                toUserHandle = "JOE",
                fromUserIdentity = "12345678901",
                fromUserType = IdentityType.TWITTER,
                fromUserHandle = "Blue"
        )

        assertThat(settlement.identityFormatted()).isEqualTo("@Blue")

        settlement = LedgerSettlementDetail(
                inviteState = BTCState.UNFULFILLED,
                inviteType = SendType.LIGHTNING_RECEIVED,
                inviteUsdValue = 10_00,
                inviteValue = 100_000,
                toUserIdentity = "12345678902",
                toUserType = IdentityType.TWITTER,
                toUserHandle = "JOE",
                fromUserIdentity = "12345678901",
                fromUserType = IdentityType.TWITTER,
                fromUserDisplayName = "[-_-]",
                fromUserHandle = "Blue"
        )

        assertThat(settlement.identityFormatted()).isEqualTo("[-_-]")
    }

    @Test
    fun returns_fiat_amount() {
        var ledgerSettlementDetail = LedgerSettlementDetail(
                inviteType = SendType.LIGHTNING_SENT,
                inviteUsdValue = 10_00
        )
        assertThat(ledgerSettlementDetail.usdValueConsidering(USDCurrency(10_000_00)).toLong())
                .isEqualTo(10_00)

        ledgerSettlementDetail = LedgerSettlementDetail(
                inviteType = SendType.LIGHTNING_SENT,
                invoiceDirection = LedgerDirection.OUT,
                invoiceType = LedgerType.LIGHTNING,
                invoiceValue = 150_000
        )
        assertThat(ledgerSettlementDetail.usdValueConsidering(USDCurrency(10_000_00)).toLong())
                .isEqualTo(15_00)
    }

    @Test
    fun returns_crypto_amount() {
        var ledgerSettlementDetail = LedgerSettlementDetail(
                inviteType = SendType.LIGHTNING_SENT,
                inviteValue = 10_000
        )
        assertThat(ledgerSettlementDetail.cryptoAmount.toLong()).isEqualTo(10_000)

        ledgerSettlementDetail = LedgerSettlementDetail(
                inviteType = SendType.LIGHTNING_SENT,
                invoiceDirection = LedgerDirection.OUT,
                invoiceType = LedgerType.LIGHTNING,
                invoiceValue = 15_000
        )
        assertThat(ledgerSettlementDetail.cryptoAmount.toLong()).isEqualTo(15_000)
    }

    @Test
    fun returns_type() {
        //INVITES
        assertThat(LedgerSettlementDetail(
                inviteType = SendType.LIGHTNING_SENT
        ).paymentType).isEqualTo(LedgerSettlementDetail.PaymentType.SentInvite)

        assertThat(LedgerSettlementDetail(
                inviteType = SendType.LIGHTNING_SENT,
                inviteState = BTCState.EXPIRED

        ).paymentType).isEqualTo(LedgerSettlementDetail.PaymentType.ExpiredSentInvite)

        assertThat(LedgerSettlementDetail(
                inviteType = SendType.LIGHTNING_SENT,
                inviteState = BTCState.CANCELED

        ).paymentType).isEqualTo(LedgerSettlementDetail.PaymentType.CanceledSentInvite)

        assertThat(LedgerSettlementDetail(
                inviteType = SendType.LIGHTNING_RECEIVED
        ).paymentType).isEqualTo(LedgerSettlementDetail.PaymentType.ReceivedInvite)

        assertThat(LedgerSettlementDetail(
                inviteType = SendType.LIGHTNING_RECEIVED,
                inviteState = BTCState.EXPIRED

        ).paymentType).isEqualTo(LedgerSettlementDetail.PaymentType.ExpiredReceivedInvite)

        assertThat(LedgerSettlementDetail(
                inviteType = SendType.LIGHTNING_RECEIVED,
                inviteState = BTCState.CANCELED

        ).paymentType).isEqualTo(LedgerSettlementDetail.PaymentType.CanceledReceivedInvite)

        //INVOICES
        assertThat(LedgerSettlementDetail(
                inviteType = SendType.LIGHTNING_SENT,
                invoiceDirection = LedgerDirection.OUT,
                invoiceType = LedgerType.LIGHTNING
        ).paymentType).isEqualTo(LedgerSettlementDetail.PaymentType.CompletedPayment)

        assertThat(LedgerSettlementDetail(
                invoiceDirection = LedgerDirection.IN,
                invoiceType = LedgerType.LIGHTNING,
                invoiceStatus = LedgerStatus.PENDING
        ).paymentType).isEqualTo(LedgerSettlementDetail.PaymentType.PendingReceiveInvoice)

        assertThat(LedgerSettlementDetail(
                invoiceDirection = LedgerDirection.IN,
                invoiceType = LedgerType.LIGHTNING,
                invoiceStatus = LedgerStatus.COMPLETED
        ).paymentType).isEqualTo(LedgerSettlementDetail.PaymentType.CompletedReceiveInvoice)

        assertThat(LedgerSettlementDetail(
                invoiceDirection = LedgerDirection.IN,
                invoiceType = LedgerType.BTC,
                invoiceStatus = LedgerStatus.COMPLETED
        ).paymentType).isEqualTo(LedgerSettlementDetail.PaymentType.TransferIn)

        assertThat(LedgerSettlementDetail(
                invoiceDirection = LedgerDirection.OUT,
                invoiceType = LedgerType.BTC,
                invoiceStatus = LedgerStatus.COMPLETED
        ).paymentType).isEqualTo(LedgerSettlementDetail.PaymentType.TransferOut)

        assertThat(LedgerSettlementDetail(
                invoiceDirection = LedgerDirection.IN,
                invoiceType = LedgerType.LIGHTNING,
                invoiceStatus = LedgerStatus.EXPIRED
        ).paymentType).isEqualTo(LedgerSettlementDetail.PaymentType.ExpiredInvoice)

        assertThat(LedgerSettlementDetail(
                invoiceDirection = LedgerDirection.IN,
                invoiceType = LedgerType.LIGHTNING,
                invoiceStatus = LedgerStatus.FAILED
        ).paymentType).isEqualTo(LedgerSettlementDetail.PaymentType.FailedInvoice)
    }


}