package com.coinninja.coinkeeper.cn.wallet.tx

import com.coinninja.bindings.AddressType
import com.coinninja.bindings.DerivationPath
import com.coinninja.bindings.Libbitcoin
import com.coinninja.bindings.UnspentTransactionOutput
import com.coinninja.coinkeeper.cn.account.AccountManager
import com.coinninja.coinkeeper.cn.wallet.HDWallet
import com.coinninja.coinkeeper.cn.wallet.LibBitcoinProvider
import com.coinninja.coinkeeper.model.db.InviteTransactionSummary
import com.coinninja.coinkeeper.model.db.TargetStat
import com.coinninja.coinkeeper.model.helpers.InviteTransactionSummaryHelper
import com.coinninja.coinkeeper.model.helpers.TargetStatHelper
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.mockito.Mockito.mock
import kotlin.math.floor

internal class FundingModelTest {
    data class dropbit(val value: Long, val fee: Long)

    private fun createFundingModel(): FundingModel {
        val fundingModel = FundingModel(
                mock(LibBitcoinProvider::class.java),
                mock(TargetStatHelper::class.java),
                mock(InviteTransactionSummaryHelper::class.java),
                mock(AccountManager::class.java),
                1000
        )
        return fundingModel
    }

    private fun mockChangeIndex(fundingModel: FundingModel, index: Int) {
        whenever(fundingModel.accountManager.nextChangeIndex).thenReturn(index)
    }

    private fun mockWithSpendableTargets(fundingModel: FundingModel, vararg values: Long) {
        val spendableTargets: MutableList<TargetStat> = mutableListOf()

        values.forEach { value ->
            val mockTarget = mock(TargetStat::class.java)
            val mockUTXO = mock(UnspentTransactionOutput::class.java)
            whenever(mockTarget.value).thenReturn(value)
            whenever(mockUTXO.amount).thenReturn(value)
            whenever(mockTarget.toUnspentTransactionOutput()).thenReturn(mockUTXO)
            spendableTargets.add(mockTarget)
        }

        whenever(fundingModel.targetStatHelper.spendableTargets).thenReturn(spendableTargets)
    }

    @Nested
    @DisplayName("Giving pending dropbits")
    inner class HasPendingDropbits {

        private fun mockWithDropbits(fundingModel: FundingModel, dropbits: Array<dropbit>) {
            val unfulfilledSentInvites: MutableList<InviteTransactionSummary> = mutableListOf()

            dropbits.forEach { dropbit ->
                val mockInvite = mock(InviteTransactionSummary::class.java)
                whenever(mockInvite.valueSatoshis).thenReturn(dropbit.value)
                whenever(mockInvite.valueFeesSatoshis).thenReturn(dropbit.fee)
                unfulfilledSentInvites.add(mockInvite)
            }

            whenever(fundingModel.inviteTransactionSummaryHelper.unfulfilledSentInvites).thenReturn(unfulfilledSentInvites)
        }

        @Test
        @DisplayName("available balance equals sum of all spendable targets less the sum of all outstanding dropbits with there fees")
        internal fun `available balance equals sum of all spendable targets less the sum of all outstanding dropbits with there fees`() {
            val fundingModel = createFundingModel()
            mockWithDropbits(fundingModel, arrayOf(dropbit(10000, 500), dropbit(20000, 1500)))
            mockWithSpendableTargets(fundingModel, 10000L, 20000L, 30000L, 40000L)

            assertThat(fundingModel.spendableAmount).isEqualTo(68000L)
        }
    }

    @Nested
    @DisplayName("Giving no pending dropbits")
    inner class DoesNotHavePendingDropbits {
        @Test
        @DisplayName("available balance equals sum of all spendable targets less the sum of all outstanding dropbits with there fees")
        internal fun `available balance equals sum of all spendable targets less the sum of all outstanding dropbits with there fees`() {
            val fundingModel = createFundingModel()
            mockWithSpendableTargets(fundingModel, 10000L, 20000L, 30000L, 40000L)

            assertThat(fundingModel.spendableAmount).isEqualTo(100000L)
        }
    }

    @Nested
    @DisplayName("Given Any FundingModel State")
    inner class GenericTest {

        @Test
        @DisplayName("provides access to dust size")
        internal fun `provides access to dust size`() {
            assertThat(createFundingModel().transactionDustValue).isEqualTo(1000L)
        }

        @Test
        @DisplayName("provides access to dust size P2SH")
        internal fun `provides input sizing in bytes for P2SH`() {
            assertThat(createFundingModel().inputSizeInBytes).isEqualTo(91)
        }

        @Test
        @DisplayName("determines bytes for P2SH payment address")
        internal fun `looks up byte size for address`() {
            val addressP2SH = "-- P2SH Address --"
            val addressP2PkH = "-- P2PKH Address --"
            val createFundingModel = createFundingModel()
            val libbitcoin = mock(Libbitcoin::class.java)
            whenever(createFundingModel.libBitcoinProvider.provide()).thenReturn(libbitcoin)
            whenever(libbitcoin.getTypeOfPaymentAddress(addressP2SH)).thenReturn(AddressType.P2SH)
            whenever(libbitcoin.getTypeOfPaymentAddress(addressP2PkH)).thenReturn(AddressType.P2PKH)

            assertThat(createFundingModel.outPutSizeInBytesForAddress(addressP2SH)).isEqualTo(32)
            assertThat(createFundingModel.outPutSizeInBytesForAddress(addressP2PkH)).isEqualTo(34)
        }

        @Test
        @DisplayName("output size for P2SH")
        internal fun `output size for P2SH`() {
            assertThat(createFundingModel().changeSizeInBytes).isEqualTo(32)
        }

        @Test
        @DisplayName("calculates cost of fee based on sizing")
        internal fun `calculates cost of fee based on sizing`() {
            val fee = 25.01
            var bytes = 11 // base tx
            bytes += 91 // input 1
            bytes += 91 // input 2
            bytes += 34 // output -- P2PKH
            bytes += 32 // output -- change

            assertThat(createFundingModel().calculateFeeForBytes(bytes, fee)).isEqualTo(floor(259 * 25.01).toLong())
        }

        @Test
        @DisplayName("provides access to next change derivation path")
        internal fun `provides access to next change derivation path`() {
            val fundingModel = createFundingModel()
            mockChangeIndex(fundingModel, 2)

            val derivationPath = DerivationPath(49, 0, 0, HDWallet.INTERNAL, 2)
            assertAll("Derivation Path",
                    { assertThat(fundingModel.nextChangePath.purpose).isEqualTo(derivationPath.purpose) },
                    { assertThat(fundingModel.nextChangePath.coinType).isEqualTo(derivationPath.coinType) },
                    { assertThat(fundingModel.nextChangePath.account).isEqualTo(derivationPath.account) },
                    { assertThat(fundingModel.nextChangePath.change).isEqualTo(derivationPath.change) },
                    { assertThat(fundingModel.nextChangePath.index).isEqualTo(derivationPath.index) }
            )

        }

        @Test
        @DisplayName("provides access to UTXOs from targets")
        internal fun `provides access to UTXOs from targets`() {
            val fundingModel = createFundingModel()
            mockWithSpendableTargets(fundingModel, 10000L, 20000L, 30000L, 40000L)

            val unspentTransactionOutputs = fundingModel.unspentTransactionOutputs

            assertAll("Unspent UTXO list is created",
                    { assertThat(unspentTransactionOutputs.size).isEqualTo(4) },
                    { assertThat(unspentTransactionOutputs[0].amount).isEqualTo(10000L) },
                    { assertThat(unspentTransactionOutputs[1].amount).isEqualTo(20000L) },
                    { assertThat(unspentTransactionOutputs[2].amount).isEqualTo(30000L) },
                    { assertThat(unspentTransactionOutputs[3].amount).isEqualTo(40000L) }
            )
        }
    }


}
