package com.coinninja.coinkeeper.ui.payment.request

import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.TestCoinKeeperApplication
import com.coinninja.coinkeeper.ui.home.HomeActivity
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.Assert.assertNotNull
import junit.framework.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class RequestDialogFragmentTest {
    val scenario = ActivityScenario.launch(HomeActivity::class.java)

    private fun createRequestDialing(): RequestDialogFragment {
        ApplicationProvider.getApplicationContext<TestCoinKeeperApplication>().apply {
            accountManager = mock { whenever(it.nextReceiveAddress).thenReturn("--address--") }
            bitcoinUriBuilder = mock()
            whenever(bitcoinUriBuilder.setAddress(any())).thenReturn(bitcoinUriBuilder)
            whenever(bitcoinUriBuilder.build()).thenReturn(mock())
        }
        val dialog = RequestDialogFragment()
        scenario.onActivity { activity ->
            dialog.show(activity.supportFragmentManager, "RequestDialog")
        }
        return dialog
    }

    @Test
    fun allows_user_to_close_dialog() {
        val dialog = createRequestDialing()

        scenario.onActivity { activity ->
            assertNotNull(activity.supportFragmentManager.findFragmentByTag("RequestDialog"))
            assertNotNull(dialog.childFragmentManager.findFragmentByTag(RequestDialogFragment.fragmentTag))

            dialog.view!!.findViewById<View>(R.id.close).performClick()

            assertNull(activity.supportFragmentManager.findFragmentByTag("RequestDialog"))
        }

        scenario.close()
    }
}