package com.coinninja.coinkeeper.ui.base;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.cn.wallet.CNWalletManager;
import com.coinninja.coinkeeper.ui.actionbar.ActionBarController;
import com.coinninja.coinkeeper.ui.actionbar.managers.DrawerController;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil;
import com.coinninja.coinkeeper.util.currency.USDCurrency;

import javax.inject.Inject;

import dagger.android.support.DaggerAppCompatActivity;

public abstract class BaseActivity extends DaggerAppCompatActivity implements MenuItemClickListener {

    @Inject
    public ActionBarController actionBarController;
    @Inject
    public Analytics analytics;
    @Inject
    TypedValue actionBarType;
    @Inject
    CNWalletManager cnWalletManager;
    @Inject
    DrawerController drawerController;
    @Inject
    ActivityNavigationUtil navigationUtil;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        actionBarController.inflateActionBarMenu(this, menu);
        actionBarController.setMenuItemClickListener(this);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean isConsumed;

        isConsumed = actionBarController.onMenuItemClicked(item);
        if (isConsumed) return true;

        isConsumed = drawerController.onMenuItemClicked(item);
        if (isConsumed) return true;

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (drawerController.isDrawerOpen()) {
            drawerController.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onCloseClicked() {
        navigationUtil.navigateToHome(this);
    }

    @Override
    public void onSkipClicked() {
        navigationUtil.navigateToHome(this);
    }

    public void showKeyboard(View view) {
        view.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    public void clearTitle() {
        actionBarController.updateTitle("");
    }

    public void updateActivityLabel(String string) {
        actionBarController.updateTitle(string);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(R.layout.cn_base_layout);
        LayoutInflater.from(this).inflate(layoutResID, findViewById(R.id.cn_content_container));
        setSupportActionBar(findViewById(R.id.toolbar));
        getTheme().resolveAttribute(R.attr.actionBarMenuType, actionBarType, true);

        actionBarController.setTheme(this, actionBarType);
        drawerController.inflateDrawer(this, actionBarType);

        actionBarController.displayTitle(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        drawerController.closeDrawerNoAnimation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (cnWalletManager.hasSkippedBackup()) {
            drawerController.showBackupNowDrawerActions();
        }

        drawerController.renderBadgeForUnverifiedDeviceIfNecessary();
    }

    protected void onPriceReceived(USDCurrency price) {
        drawerController.updatePriceOfBtcDisplay(price);
    }
}
