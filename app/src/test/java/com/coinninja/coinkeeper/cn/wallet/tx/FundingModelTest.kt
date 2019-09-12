package com.coinninja.coinkeeper.cn.wallet.tx

import app.coinninja.cn.libbitcoin.HDWallet
import app.coinninja.cn.libbitcoin.enum.AddressType
import app.coinninja.cn.libbitcoin.model.DerivationPath
import app.coinninja.cn.libbitcoin.model.UnspentTransactionOutput
import com.coinninja.coinkeeper.model.db.InviteTransactionSummary
import com.coinninja.coinkeeper.model.db.TargetStat
import com.coinninja.coinkeeper.model.db.Wallet
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import kotlin.math.floor

internal class FundingModelTest {
    data class dropbit(val value: Long, val fee: Long)

    private fun createFundingModel(): FundingModel {
        val wallet: Wallet = mock()
        val fundingModel = FundingModel(mock(), mock(), mock(), mock(), mock(), 1000)
        whenever(fundingModel.walletHelper.wallet).thenReturn(wallet)
        whenever(wallet.purpose).thenReturn(49)
        whenever(wallet.coinType).thenReturn(0)
        whenever(wallet.accountIndex).thenReturn(0)
        return fundingModel
    }

    private fun mockChangeIndex(fundingModel: FundingModel, index: Int) {
        whenever(fundingModel.accountManager.nextChangeIndex).thenReturn(index)
    }

    private fun mockWithSpendableTargets(fundingModel: FundingModel, vararg values: Long) {
        val spendableTargets: MutableList<TargetStat> = mutableListOf()

        values.forEach { value ->
            val targetStat: TargetStat = mock()
            val utxo = UnspentTransactionOutput(amount = value)
            whenever(targetStat.value).thenReturn(value)
            whenever(targetStat.toUnspentTransactionOutput()).thenReturn(utxo)
            spendableTargets.add(targetStat)
        }

        whenever(fundingModel.targetStatHelper.spendableTargets).thenReturn(spendableTargets)
    }

    @Nested
    @DisplayName("Giving pending dropbits")
    inner class HasPendingDropbits {

        private fun mockWithDropbits(fundingModel: FundingModel, dropbits: Array<dropbit>) {
            val unfulfilledSentInvites: MutableList<InviteTransactionSummary> = mutableListOf()

            dropbits.forEach { dropbit ->
                val mockInvite: InviteTransactionSummary = mock()
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
        internal fun available_balance_equals_sum_of_all_spendable_targets_less_the_sum_of_all_outstanding_dropbits_with_there_fees() {
            val fundingModel = createFundingModel()
            mockWithSpendableTargets(fundingModel, 10000L, 20000L, 30000L, 40000L)

            assertThat(fundingModel.spendableAmount).isEqualTo(100000L)
        }
    }

    @Nested
    @DisplayName("Given Any FundingModel State")
    inner class GenericTest {

        @Test
        @DisplayName("provides access to next receive address")
        internal fun provides_access_to_next_receive_address() {
            val model = createFundingModel()
            whenever(model.accountManager.nextReceiveAddress).thenReturn("--address--")

            assertThat(model.nextReceiveAddress).isEqualTo("--address--")
        }

        @Test
        @DisplayName("provides access to dust size")
        internal fun provides_access_to_dust_size() {
            assertThat(createFundingModel().transactionDustValue).isEqualTo(1000L)
        }

        @Test
        @DisplayName("provides access to dust size P2SH")
        internal fun provides_input_sizing_in_bytes_for_P2SH() {
            assertThat(createFundingModel().inputSizeInBytes).isEqualTo(91)
        }

        @Test
        @DisplayName("calculates change output size based on saved purpose")
        internal fun calculates_change_output_size_based_on_wallet_purpose() {
            val fundingModel = createFundingModel()
            val wallet:Wallet = mock()
            whenever(wallet.purpose).thenReturn(49).thenReturn(84)
            whenever(fundingModel.walletHelper.wallet).thenReturn(wallet)

            assertThat(fundingModel.inputSizeInBytes).isEqualTo(91)
            assertThat(fundingModel.inputSizeInBytes).isEqualTo(68)
        }

        @Test
        @DisplayName("calculates input size based on saved purpose")
        internal fun calculates_input_size_based_on_wallet_purpose() {
            val fundingModel = createFundingModel()
            val wallet:Wallet = mock()
            whenever(wallet.purpose).thenReturn(49).thenReturn(84)
            whenever(fundingModel.walletHelper.wallet).thenReturn(wallet)

            assertThat(fundingModel.changeSizeInBytes).isEqualTo(32)
            assertThat(fundingModel.changeSizeInBytes).isEqualTo(31)
        }

        @Test
        @DisplayName("determines bytes for payment addresses")
        internal fun looks_up_byte_size_for_address() {
            val addressP2SH = "-- P2SH Address --"
            val addressP2PkH = "-- P2PKH Address --"
            val addressP2WSH = "-- P2WSH Address --"
            val addressP2WPKH = "-- P2WPKH Address --"
            val fundingModel = createFundingModel()
            whenever(fundingModel.addressUtil.typeOfPaymentAddress(addressP2SH)).thenReturn(AddressType.P2SH)
            whenever(fundingModel.addressUtil.typeOfPaymentAddress(addressP2PkH)).thenReturn(AddressType.P2PKH)
            whenever(fundingModel.addressUtil.typeOfPaymentAddress(addressP2WSH)).thenReturn(AddressType.P2WSH)
            whenever(fundingModel.addressUtil.typeOfPaymentAddress(addressP2WPKH)).thenReturn(AddressType.P2WPKH)

            assertThat(fundingModel.outPutSizeInBytesForAddress(addressP2SH)).isEqualTo(32)
            assertThat(fundingModel.outPutSizeInBytesForAddress(addressP2PkH)).isEqualTo(34)
            assertThat(fundingModel.outPutSizeInBytesForAddress(addressP2WSH)).isEqualTo(32)
            assertThat(fundingModel.outPutSizeInBytesForAddress(addressP2WPKH)).isEqualTo(31)
        }

        @Test
        @DisplayName("output size for P2SH")
        internal fun output_size_for_P2SH() {
            assertThat(createFundingModel().changeSizeInBytes).isEqualTo(32)
        }

        @Test
        @DisplayName("calculates cost of fee based on sizing")
        internal fun calculates_cost_of_fee_based_on_sizing() {
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
        internal fun provides_access_to_next_change_derivation_path() {
            val fundingModel = createFundingModel()
            mockChangeIndex(fundingModel, 2)

            val derivationPath = DerivationPath(49, 0, 0, HDWallet.INTERNAL, 2)
            assertAll("Derivation Path",
                    { assertThat(fundingModel.nextChangePath.purpose).isEqualTo(derivationPath.purpose) },
                    { assertThat(fundingModel.nextChangePath.coin).isEqualTo(derivationPath.coin) },
                    { assertThat(fundingModel.nextChangePath.account).isEqualTo(derivationPath.account) },
                    { assertThat(fundingModel.nextChangePath.chain).isEqualTo(derivationPath.chain) },
                    { assertThat(fundingModel.nextChangePath.index).isEqualTo(derivationPath.index) }
            )

        }

        @Test
        @DisplayName("provides access to UTXOs from targets")
        internal fun provides_access_to_UTXOs_from_targets() {
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
