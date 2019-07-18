package com.coinninja.coinkeeper.ui.settings

import android.net.Uri
import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.util.FeesManager
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import dagger.Module
import dagger.Provides
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class AdjustableFeesActivityTest {

    private fun createScenario(): ActivityScenario<AdjustableFeesActivity> {
        return ActivityScenario.launch(AdjustableFeesActivity::class.java)
    }

    @Test
    fun shows_tooltip_for_adjustable_fees() {
        createScenario().onActivity {
            it.findViewById<View>(R.id.adjustable_fees_tooltip).performClick()

            verify(it.activityNavigationUtil).openUrl(it, Uri.parse("https://dropbit.app/tooltips/fees"))
        }
    }

    @Module
    class TestAdjustableFeesActivityModule {
        @Provides
        fun feesManager(): FeesManager = mock()
    }
}