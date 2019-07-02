package com.coinninja.coinkeeper.util

import com.coinninja.coinkeeper.service.client.model.TransactionFee
import com.coinninja.coinkeeper.util.FeesManager.Companion.CHEAP_FEE_STRING
import com.coinninja.coinkeeper.util.FeesManager.Companion.FAST_FEE_STRING
import com.coinninja.coinkeeper.util.FeesManager.Companion.SLOW_FEE_STRING
import com.coinninja.coinkeeper.util.android.PreferencesUtil
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*

class FeesManagerTest {

    fun setup(preferencesUtil: PreferencesUtil): FeesManager {
        return FeesManager(preferencesUtil)
    }

    @Test
    fun `test fee preference`() {
        val preferenceUtil = mock(PreferencesUtil::class.java)
        whenever(preferenceUtil.getString(ArgumentMatchers.anyString(), ArgumentMatchers.anyString())).thenReturn(FAST_FEE_STRING)
        val manager = setup(preferenceUtil)

        assertThat(manager.feePreference, equalTo(FeesManager.FeeType.FAST))
    }

    @Test
    fun `test set fees`() {
        val preferenceUtil = mock(PreferencesUtil::class.java)
        val manager = setup(preferenceUtil)
        val fees = TransactionFee(2.0, 4.0, 6.0)

        manager.setFees(fees)

        verify(preferenceUtil).savePreference(FAST_FEE_STRING, fees.fast.toString())
        verify(preferenceUtil).savePreference(SLOW_FEE_STRING, fees.med.toString())
        verify(preferenceUtil).savePreference(CHEAP_FEE_STRING, fees.slow.toString())
    }

    @Test
    fun `test get fees`() {
        val preferenceUtil = mock(PreferencesUtil::class.java)
        val manager = setup(preferenceUtil)
        val fees = TransactionFee(2.0, 4.0, 6.0)

        manager.fee(FeesManager.FeeType.FAST)

        verify(preferenceUtil).getString(FAST_FEE_STRING, "0.0")
    }
}