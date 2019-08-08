package com.coinninja.coinkeeper.ui.market

import android.os.Build
import android.view.ViewGroup
import android.widget.Button
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.viewModel.MarketDataViewModel

class MarketChartButtonBarController {
    private var currentGranularity: Granularity = Granularity.DAY

    fun setupButtonBar(currentGranularity: Granularity, buttonBar: ViewGroup?, marketDataViewModel: MarketDataViewModel) = buttonBar?.let {
        this.currentGranularity = currentGranularity
        it.findViewById<Button>(R.id.granularity_day)?.setOnClickListener { marketDataViewModel.loadGranularity(Granularity.DAY) }
        it.findViewById<Button>(R.id.granularity_week)?.setOnClickListener { marketDataViewModel.loadGranularity(Granularity.WEEK) }
        it.findViewById<Button>(R.id.granularity_month)?.setOnClickListener { marketDataViewModel.loadGranularity(Granularity.MONTH) }
        it.findViewById<Button>(R.id.granularity_year)?.setOnClickListener { marketDataViewModel.loadGranularity(Granularity.YEAR) }
        it.findViewById<Button>(R.id.granularity_all)?.setOnClickListener { marketDataViewModel.loadGranularity(Granularity.ALL) }
    }

    fun onGranularityChanged(newGranularity: Granularity, buttonBar: ViewGroup?) = buttonBar?.let { bar ->
        clearSelection(currentGranularity, bar)
        updateGranularity(newGranularity, bar)
        currentGranularity = newGranularity
    }


    private fun updateGranularity(granularity: Granularity, buttonBar: ViewGroup) {
        when (granularity) {
            Granularity.DAY -> select(buttonBar.findViewById(R.id.granularity_day))
            Granularity.WEEK -> select(buttonBar.findViewById(R.id.granularity_week))
            Granularity.MONTH -> select(buttonBar.findViewById(R.id.granularity_month))
            Granularity.YEAR -> select(buttonBar.findViewById(R.id.granularity_year))
            Granularity.ALL -> select(buttonBar.findViewById(R.id.granularity_all))
        }
    }

    private fun clearSelection(granularity: Granularity, buttonBar: ViewGroup) {
        when (granularity) {
            Granularity.DAY -> reset(buttonBar.findViewById(R.id.granularity_day))
            Granularity.WEEK -> reset(buttonBar.findViewById(R.id.granularity_week))
            Granularity.MONTH -> reset(buttonBar.findViewById(R.id.granularity_month))
            Granularity.YEAR -> reset(buttonBar.findViewById(R.id.granularity_year))
            Granularity.ALL -> reset(buttonBar.findViewById(R.id.granularity_all))
        }
    }

    private fun reset(button: Button?) {
        button?.apply {
            setBackgroundResource(R.drawable.button_market_granularity)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                setTextAppearance(context, R.style.TextAppearance_MarketGranularity)
            } else {
                setTextAppearance(R.style.TextAppearance_MarketGranularity)
            }
        }
    }

    private fun select(button: Button?) {
        button?.apply {
            background = this.context.resources.getDrawable(R.drawable.button_market_granularity_pressed)
            setTextColor(resources.getColor(R.color.font_white))
        }
    }

}
