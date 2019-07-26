package com.coinninja.coinkeeper.ui.actionbar

import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.ui.actionbar.managers.TitleViewManager
import com.coinninja.coinkeeper.ui.base.MenuItemClickListener
import javax.inject.Inject

@Mockable
class ActionBarController @Inject constructor(internal val titleViewManager: TitleViewManager) {

    var menuItemClickListener: MenuItemClickListener? = null
    internal var isActionBarGone: Boolean = false
    internal var isUpEnabled: Boolean = false
    internal var optionMenuLayout: Int? = null

    fun setTheme(context: AppCompatActivity, actionBarType: TypedValue) {

        when (actionBarType.resourceId) {
            R.id.actionbar_gone -> {
                isActionBarGone = true
                return
            }
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
        updateActionBarUpIndicator(context)
        initTitleView(context)

    }

    fun displayTitle(context: AppCompatActivity) {
        if (isActionBarGone) {
            context.findViewById<View>(R.id.cn_appbar_layout_container).visibility = View.GONE
        } else {
            titleViewManager.renderTitle()
        }
    }

    fun inflateActionBarMenu(context: AppCompatActivity, menu: Menu) {
        optionMenuLayout?.let {
            context.menuInflater.inflate(it, menu)
        }
    }

    private fun updateActionBarUpIndicator(context: AppCompatActivity) {
        context.supportActionBar?.setDisplayHomeAsUpEnabled(isUpEnabled)
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

    fun updateTitle(string: String) {
        if (!isActionBarGone) {
            titleViewManager.title = string
            titleViewManager.renderTitle()
        }
    }

    private fun initTitleView(context: AppCompatActivity) {
        titleViewManager.actionBar = context.supportActionBar
        titleViewManager.titleView = context.findViewById(R.id.appbar_title)
    }
}
