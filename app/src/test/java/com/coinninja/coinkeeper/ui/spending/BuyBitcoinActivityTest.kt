package com.coinninja.coinkeeper.ui.spending

import android.content.pm.PackageManager
import androidx.lifecycle.Lifecycle.State
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.coinkeeper.TestCoinKeeperApplication
import com.coinninja.coinkeeper.cn.transaction.FundingViewModelProvider
import com.coinninja.coinkeeper.cn.transaction.notification.FundingViewModel
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.coinkeeper.util.analytics.Analytics
import com.coinninja.coinkeeper.util.uri.parameter.CoinNinjaParameter
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import dagger.Module
import dagger.Provides
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BuyBitcoinActivityTest {

    private fun createScenario(): ActivityScenario<BuyBitcoinActivity> {
        val application: TestCoinKeeperApplication = ApplicationProvider.getApplicationContext()
        application.locationUtil = mock()
        application.userPreferences = mock()
        application.accountManager = mock()
        whenever(application.locationUtil.canReadLocation()).thenReturn(true)
        whenever(application.locationUtil.lastKnownLocation).thenReturn(mock())
        whenever(application.accountManager.nextReceiveAddress).thenReturn("--next-receive-address--")
        return ActivityScenario.launch(BuyBitcoinActivity::class.java)
    }

    @Test
    fun sets_current_receive_address_on_button_for_blockchain() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            assertThat(activity.copyBlockchainAddressButton.text).isEqualTo("--next-receive-address--")
        }

        scenario.moveToState(State.DESTROYED)
        scenario.close()
    }

    @Test
    fun clicking_g_pay_launches_website_to_buy() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            activity.gPayLink.performClick()

            verify(activity.activityNavigationUtil).buyBitcoin(activity, "--next-receive-address--")
        }

        scenario.moveToState(State.DESTROYED)
        scenario.close()
    }

    @Test
    fun forwards_find_atm_on_click__when_permission_to_access_location() {
        val scenario = createScenario()
        val parameters = HashMap<CoinNinjaParameter, String>()
        parameters[CoinNinjaParameter.TYPE] = "atms"

        scenario.onActivity { activity ->
            whenever(activity.locationUtil.canReadLocation()).thenReturn(true)

            activity.findATM.performClick()

            verify(activity.activityNavigationUtil).navigatesToMapWith(activity, parameters,
                    activity.locationUtil.lastKnownLocation, Analytics.EVENT_BUY_BITCOIN_AT_ATM)

        }
        scenario.moveToState(State.DESTROYED)
        scenario.close()
    }

    @Test
    fun requests_location_permission_on_atm_click__when_no_permission_to_access_location() {
        val scenario = createScenario()
        scenario.onActivity { activity ->
            whenever(activity.locationUtil.canReadLocation()).thenReturn(false)

            activity.findATM.performClick()

            verify(activity.locationUtil).requestPermissionToAccessLocationFor(activity)
        }
        scenario.moveToState(State.DESTROYED)
        scenario.close()
    }

    @Test
    fun navigates_to_map_for_spending_with_no_location_when_permission_denied() {
        val grantResults = IntArray(1)
        val scenario = createScenario()
        scenario.onActivity { activity ->
            grantResults[0] = PackageManager.PERMISSION_DENIED
            whenever(activity.locationUtil.hasGrantedPermission(
                    DropbitIntents.REQUEST_PERMISSIONS_LOCATION,
                    arrayOfNulls(0),
                    grantResults)
            ).thenReturn(false)

            activity.onRequestPermissionsResult(DropbitIntents.REQUEST_PERMISSIONS_LOCATION, emptyArray(), grantResults)

            verify(activity.activityNavigationUtil).navigatesToMapWith(eq(activity), any(), eq(null), eq(Analytics.EVENT_BUY_BITCOIN_AT_ATM))

        }
        scenario.moveToState(State.DESTROYED)
        scenario.close()
    }


    @Module
    class BuyBitcoinActivityTestModule {
        @Provides
        fun fundingViewModelProvider(): FundingViewModelProvider {
            val provider: FundingViewModelProvider = mock()
            val fundingViewModel = mock<FundingViewModel>()
            whenever(provider.provide(any())).thenReturn(fundingViewModel)
            whenever(fundingViewModel.fetchLightningDepositAddress()).thenReturn(mock())
            return provider
        }
    }
}