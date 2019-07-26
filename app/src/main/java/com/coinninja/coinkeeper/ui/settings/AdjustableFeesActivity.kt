package com.coinninja.coinkeeper.ui.settings

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.util.FeesManager
import com.coinninja.coinkeeper.util.FeesManager.FeeType.*
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil
import com.coinninja.coinkeeper.util.uri.DropbitUriBuilder
import com.coinninja.coinkeeper.util.uri.routes.DropbitRoute
import com.coinninja.coinkeeper.view.activity.base.SecuredActivity
import javax.inject.Inject

class AdjustableFeesActivity : SecuredActivity() {
    @Inject
    lateinit var activityNavigationUtil: ActivityNavigationUtil

    @Inject
    lateinit var dropbitUriBuilder: DropbitUriBuilder

    @Inject
    lateinit var adjustableFeesManager: FeesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_adjustable_fees)
        findViewById<Switch>(R.id.adjustable_fees_switch).setOnClickListener { adjustableFeeSwitchClicked() }
        findViewById<TextView>(R.id.adjustable_fees_tooltip).setOnClickListener { adjustableFeesTooltipClicked() }
        findViewById<Button>(R.id.fast_fees).setOnClickListener { feeSelectionButtonClicked(it) }
        findViewById<Button>(R.id.slow_fees).setOnClickListener { feeSelectionButtonClicked(it) }
        findViewById<Button>(R.id.cheap_fees).setOnClickListener { feeSelectionButtonClicked(it) }
    }

    private fun adjustableFeesTooltipClicked() {
        activityNavigationUtil.openUrl(this, dropbitUriBuilder.build(DropbitRoute.ADJUSTABLE_FEES))
    }

    private fun feeSelectionButtonClicked(view: View) {
        when (view) {
            findViewById<View>(R.id.fast_fees) -> {
                adjustableFeesManager.feePreference = FAST
            }
            findViewById<View>(R.id.slow_fees) -> {
                adjustableFeesManager.feePreference = SLOW
            }
            findViewById<View>(R.id.cheap_fees) -> {
                adjustableFeesManager.feePreference = CHEAP
            }
        }

        setupUIForFeePreference()
    }

    override fun onResume() {
        super.onResume()
        setupUIForFeePreference()
    }

    private fun setupUIForFeePreference() {
        if (adjustableFeesManager.isAdjustableFeesEnabled) {
            findViewById<Group>(R.id.adjustable_fee_configuration_group).visibility = View.VISIBLE
            setupUIForSelectedFee()
        } else {
            findViewById<Group>(R.id.adjustable_fee_configuration_group).visibility = View.GONE
        }

        findViewById<Switch>(R.id.adjustable_fees_switch).isChecked = adjustableFeesManager.isAdjustableFeesEnabled
    }

    private fun setupUIForSelectedFee() {
        when (adjustableFeesManager.feePreference) {
            FAST -> {
                setupSelectedButtonUI(findViewById(R.id.fast_fees))
                setupUnselectedButton(findViewById(R.id.slow_fees))
                setupUnselectedButton(findViewById(R.id.cheap_fees))
            }
            SLOW -> {
                setupUnselectedButton(findViewById(R.id.fast_fees))
                setupSelectedButtonUI(findViewById(R.id.slow_fees))
                setupUnselectedButton(findViewById(R.id.cheap_fees))
            }
            CHEAP -> {
                setupUnselectedButton(findViewById(R.id.fast_fees))
                setupUnselectedButton(findViewById(R.id.slow_fees))
                setupSelectedButtonUI(findViewById(R.id.cheap_fees))
            }
        }
    }

    private fun setupSelectedButtonUI(button: Button) {
        val right = resources.getDrawable(R.drawable.checkmark, null)
        button.setCompoundDrawablesWithIntrinsicBounds(null, null, right, null)
    }

    private fun setupUnselectedButton(button: Button) {
        button.setCompoundDrawables(null, null, null, null)
    }

    private fun adjustableFeeSwitchClicked() {
        adjustableFeesManager.isAdjustableFeesEnabled = !adjustableFeesManager.isAdjustableFeesEnabled
        setupUIForFeePreference()
    }
}
