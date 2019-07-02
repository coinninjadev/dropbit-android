package com.coinninja.coinkeeper.cn.service

import app.dropbit.commons.util.TestCoroutineContextProvider
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Ignore
import org.junit.Test
import org.mockito.Mockito.*

class YearlyHighViewModelTest {
    val yearlyHighViewModel = YearlyHighViewModel(mock(YearlyHighSubscription::class.java), TestCoroutineContextProvider())

    @Ignore
    @Test
    fun `fetches current subscription when requested`() {
        runBlocking {
            assertTrue(yearlyHighViewModel.isSubscribedToYearlyHigh.value!!)
            verify(yearlyHighViewModel.yearlyHighSubscription, times(2)).isSubscribed()
        }
    }

    @Ignore
    @Test
    fun `toggling performs subscription removal`() {
        runBlocking {
            whenever(yearlyHighViewModel.yearlyHighSubscription.isSubscribed()).thenReturn(false)
            yearlyHighViewModel.isSubscribedToYearlyHigh.value = true

            yearlyHighViewModel.toggleSubscription(false)

            verify(yearlyHighViewModel.yearlyHighSubscription).unsubscribe()
            assertFalse(yearlyHighViewModel.isSubscribedToYearlyHigh.value!!)
        }
    }

    @Ignore
    @Test
    fun `toggling performs subscribe`() {
        runBlocking {
            whenever(yearlyHighViewModel.yearlyHighSubscription.isSubscribed()).thenReturn(true)
            yearlyHighViewModel.isSubscribedToYearlyHigh.value = false

            yearlyHighViewModel.toggleSubscription(false)

            verify(yearlyHighViewModel.yearlyHighSubscription).subscribe()
            assertTrue(yearlyHighViewModel.isSubscribedToYearlyHigh.value!!)
        }
    }

}