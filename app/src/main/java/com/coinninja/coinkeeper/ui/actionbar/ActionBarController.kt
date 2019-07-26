package com.coinninja.coinkeeper.ui.actionbar

import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.ui.base.MenuItemClickListener
import javax.inject.Inject

@Mockable
class ActionBarController @Inject constructor() {

    var menuItemClickListener: MenuItemClickListener? = null
    internal var isActionBarGone: Boolean = false
    internal var isUpEnabled: Boolean = false
    internal var optionMenuLayout: Int? = null
    private var _title:String = ""

    fun setTheme(activity: AppCompatActivity, actionBarType: TypedValue) {
        when (actionBarType.resourceId) {
            R.id.actionbar_gone -> isActionBarGone = true

            R.id.actionbar_up_on -> isUpEnabled = true

            R.id.actionbar_up_on_with_nav_bar -> {
                isUpEnabled = true
                optionMenuLayout = R.menu.actionbar_light_charts_menu
            }
            R.id.actionbar_up_off_close_on -> {
                isUpEnabled = false
                optionMenuLayout = R.menu.actionbar_light_close_menu
            }

            R.id.actionbar_up_off -> isUpEnabled = false

            R.id.actionbar_up_off_skip_on -> {
                isUpEnabled = false
                optionMenuLayout = R.menu.actionbar_light_skip_menu
            }
            R.id.actionbar_up_on_skip_on -> {
                isUpEnabled = true
                optionMenuLayout = R.menu.actionbar_light_skip_menu
            }
            R.id.actionbar_up_on_close_on -> {
                isUpEnabled = true
                optionMenuLayout = R.menu.actionbar_light_close_menu
            }
            else -> throw IllegalStateException("R.attr.actionBarMenuType not set")
        }
        hideAppbarIfNecessary(activity)
        updateActionBarUpIndicator(activity)
        displayTitle(activity)
    }

    fun inflateActionBarMenu(activity: AppCompatActivity, menu: Menu) {
        optionMenuLayout?.let {
            activity.menuInflater.inflate(it, menu)
        }
    }

    private fun updateActionBarUpIndicator(activity: AppCompatActivity) {
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(isUpEnabled)
    }

    fun onMenuItemClicked(item: MenuItem): Boolean {
        optionMenuLayout?.let {
            return when (item.itemId) {
                R.id.action_skip_btn -> {
                    menuItemClickListener?.onSkipClicked()
                    true
                }
                R.id.action_close_btn -> {
                    menuItemClickListener?.onCloseClicked()
                    true
                }
                R.id.action_chart_button -> {
                    menuItemClickListener?.onShowMarketData()
                    true
                }
                else -> false
            }
        }
        return false
    }

    fun displayTitle(activity: AppCompatActivity, title: String? = null) {
        _title = title ?: activity.supportActionBar?.title.toString()
        activity.supportActionBar?.title = ""

        if (isActionBarGone && _title.isNotEmpty()) {
            removeTitle(activity)
        } else {
            renderTitle(activity)
        }
    }

    private fun hideAppbarIfNecessary(activity: AppCompatActivity) {
        if (isActionBarGone) {
            activity.findViewById<View>(R.id.cn_appbar_layout_container)?.apply {
                visibility = View.GONE
            }
        }
    }

    private fun renderTitle(activity: AppCompatActivity) {
        activity.findViewById<TextView>(R.id.appbar_title)?.apply {
            visibility = View.VISIBLE
            text = _title.toUpperCase()
        }
    }

    private fun removeTitle(activity: AppCompatActivity) {
        activity.findViewById<TextView>(R.id.appbar_title)?.apply {
            visibility = View.GONE
        }
    }
}
