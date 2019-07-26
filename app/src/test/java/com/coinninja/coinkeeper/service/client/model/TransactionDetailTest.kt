package com.coinninja.coinkeeper.service.client.model

import com.coinninja.coinkeeper.model.db.enums.MemPoolState
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TransactionDetailTest {

    @Test
    fun prioritizes_block_time() {
        val detail = TransactionDetail(blocktime = 3, time = 2, receivedTime = 1)

        assertThat(detail.timeMillis).isEqualTo(3000)
    }

    @Test
    fun then_prioritizes_time() {
        val detail = TransactionDetail(time = 2, receivedTime = 1)

        assertThat(detail.timeMillis).isEqualTo(2000)
    }

    @Test
    fun finally_prioritizes_received_time() {
        val detail = TransactionDetail(receivedTime = 1)

        assertThat(detail.timeMillis).isEqualTo(1000)
    }

    @Test
    fun calculates_number_of_confirmations() {
        assertThat(TransactionDetail(blockheight = 100).numConfirmations(103)).isEqualTo(4)
        assertThat(TransactionDetail().numConfirmations(103)).isEqualTo(0)
    }

    @Test
    fun provides_mempool_state() {
        assertThat(TransactionDetail(blockhash = "--hash--").mempoolState).isEqualTo(MemPoolState.MINED)
        assertThat(TransactionDetail().mempoolState).isEqualTo(MemPoolState.ACKNOWLEDGE)
    }

    @Test
    fun provides_number_of_inputs() {
        assertThat(TransactionDetail(vInList = listOf(VIn(), VIn(), VIn())).numberOfInputs).isEqualTo(3)
        assertThat(TransactionDetail().numberOfInputs).isEqualTo(0)
    }

    @Test
    fun provides_number_of_outputs() {
        assertThat(TransactionDetail(vOutList = listOf(VOut(), VOut(), VOut())).numberOfOutputs).isEqualTo(3)
        assertThat(TransactionDetail().numberOfOutputs).isEqualTo(0)
    }
}