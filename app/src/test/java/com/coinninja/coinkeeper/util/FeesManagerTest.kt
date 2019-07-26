package com.coinninja.coinkeeper.util

import com.coinninja.coinkeeper.service.client.model.TransactionFee
import com.coinninja.coinkeeper.util.FeesManager.Companion.CHEAP_FEE_STRING
import com.coinninja.coinkeeper.util.FeesManager.Companion.FAST_FEE_STRING
import com.coinninja.coinkeeper.util.FeesManager.Companion.SLOW_FEE_STRING
import com.nhaarman.mockitokotlin2.*
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class FeesManagerTest {

    private fun createManager(): FeesManager = FeesManager(mock()).also {
        whenever(it.preferencesUtil.getString(eq(FAST_FEE_STRING), any())).thenReturn("2.0")
    }

    @Test
    fun `test fee preference`() {
        val manager = createManager()
        whenever(manager.preferencesUtil.getString(any(), any())).thenReturn(FAST_FEE_STRING)

        assertThat(manager.feePreference, equalTo(FeesManager.FeeType.FAST))
    }

    @Test
    fun `test set fees`() {
        val manager = createManager()
        val fees = TransactionFee(2.0, 4.0, 6.0)

        manager.setFees(fees)

        verify(manager.preferencesUtil).savePreference(FAST_FEE_STRING, fees.fast.toString())
        verify(manager.preferencesUtil).savePreference(SLOW_FEE_STRING, fees.med.toString())
        verify(manager.preferencesUtil).savePreference(CHEAP_FEE_STRING, fees.slow.toString())
    }

    @Test
    fun `test get fees`() {
        val manager = createManager()

        manager.fee(FeesManager.FeeType.FAST)

        verify(manager.preferencesUtil).getString(FAST_FEE_STRING, "0.0")
    }
}