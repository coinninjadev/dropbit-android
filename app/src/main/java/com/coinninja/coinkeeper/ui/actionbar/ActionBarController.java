package com.coinninja.coinkeeper.ui.actionbar;

import android.content.Context;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.ui.actionbar.managers.TitleViewManager;
import com.coinninja.coinkeeper.ui.base.MenuItemClickListener;

import javax.inject.Inject;

public class ActionBarController {

    public MenuItemClickListener menuItemClickListener;
    public Boolean isActionBarGone;
    public Boolean isTitleUppercase;
    public Boolean isUpEnabled;
    public Integer optionMenuLayout;
    @Inject
    TitleViewManager titleViewManager;

    @Inject
    public ActionBarController() {
    }

    public void setTheme(Context context, TypedValue actionBarType) {
        isTitleUppercase = false;

        switch (actionBarType.resourceId) {
            case R.id.actionbar_gone:
                isActionBarGone = true;
                return;
            case R.id.actionbar_up_on:
                isUpEnabled = true;
                break;
            case R.id.actionbar_up_on_with_nav_bar:
                isUpEnabled = true;
                optionMenuLayout = R.menu.actionbar_light_charts_menu;
                break;
            case R.id.actionbar_up_off_close_on:
                isUpEnabled = false;
                optionMenuLayout = R.menu.actionbar_light_close_menu;
                break;
            case R.id.actionbar_up_off:
                isUpEnabled = false;
                break;
            case R.id.actionbar_up_off_skip_on:
                isUpEnabled = false;
                optionMenuLayout = R.menu.actionbar_light_skip_menu;
                break;
            case R.id.actionbar_up_on_skip_on:
                isUpEnabled = true;
                optionMenuLayout = R.menu.actionbar_light_skip_menu;
                break;
            case R.id.actionbar_up_on_close_on:
                isUpEnabled = true;
                optionMenuLayout = R.menu.actionbar_light_close_menu;
                break;
            default:
                throw new IllegalStateException("R.attr.actionBarMenuType not set");
        }

        initTitleView(((AppCompatActivity) context));

    }

    public void displayTitle(AppCompatActivity context) {
        if (isActionBarGone != null && isActionBarGone == true) {
            context.findViewById(R.id.cn_appbar_layout_container).setVisibility(View.GONE);
            return;
        }


        if (isTitleUppercase) {
            titleViewManager.renderUpperCaseTitleView();
        } else {
            titleViewManager.renderTitleView();
        }
    }

    public void inflateActionBarMenu(AppCompatActivity context, Menu menu) {
        if (isUpEnabled != null) {
            context.getSupportActionBar().setDisplayHomeAsUpEnabled(isUpEnabled);
        }

        if (optionMenuLayout != null) {
            context.getMenuInflater().inflate(optionMenuLayout, menu);
        }
    }

    public boolean onMenuItemClicked(MenuItem item) {
        if (optionMenuLayout == null) return false;
        switch (item.getItemId()) {
            case R.id.action_skip_btn:
                menuItemClickListener.onSkipClicked();
                return true;
            case R.id.action_close_btn:
                menuItemClickListener.onCloseClicked();
                return true;
            case R.id.action_chart_button:
                menuItemClickListener.onShowMarketData();
                return true;
            default:
                return false;
        }
    }

    public void setMenuItemClickListener(MenuItemClickListener menuItemClickListener) {
        this.menuItemClickListener = menuItemClickListener;
    }

    public void updateTitle(String string) {
        if (isActionBarGone != null && isActionBarGone) return;

        titleViewManager.renderTitleView(string);
    }

    private void initTitleView(AppCompatActivity context) {
        titleViewManager.setActionBar(context.getSupportActionBar());
        titleViewManager.setTitleView(context.findViewById(R.id.appbar_title));
    }
}
