package com.coinninja.coinkeeper.service.runner

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test
import org.mockito.Mockito.verify

class NegativeBalanceRunnerTest {

    @Test
    fun on_negative_lightning_cancel() {
        val runner = createRunner()
        whenever(runner.thunderDomeRepository.availableBalance).thenReturn(-1L)

        runner.run()

        verify(runner.cancellationService).markUnfulfilledLightningAsCanceled()
    }

    @Test
    fun on_negative_cancel() {
        val runner = createRunner()
        whenever(runner.walletHelper.buildBalances(false)).thenReturn(-1L)

        runner.run()

        verify(runner.cancellationService).markUnfulfilledAsCanceled()
    }

    @Test
    fun not_negative_NOOP() {
        val runner = createRunner()
        whenever(runner.walletHelper.buildBalances(false)).thenReturn(1L)

        runner.run()

        verifyZeroInteractions(runner.cancellationService)
    }

    private fun createRunner() = NegativeBalanceRunner(mock(), mock(), mock())
}