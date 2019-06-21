package com.coinninja.coinkeeper.ui.actionbar.managers;

import android.content.Context;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.coinninja.android.helpers.Resources;
import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.di.interfaces.BuildVersionName;
import com.coinninja.coinkeeper.model.helpers.DropbitAccountHelper;
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil;
import com.coinninja.coinkeeper.util.currency.USDCurrency;
import com.coinninja.coinkeeper.util.ui.BadgeRenderer;
import com.google.android.material.navigation.NavigationView;

import javax.inject.Inject;

import static com.coinninja.android.helpers.Views.withId;

public class DrawerController {

    private final String versionName;
    private final DropbitAccountHelper dropbitAccountHelper;
    DrawerLayout drawerLayout;
    private BadgeRenderer badgeRenderer;
    private ActivityNavigationUtil navigationUtil;

    @Inject
    public DrawerController(BadgeRenderer badgeRenderer, ActivityNavigationUtil navigationUtil,
                            @BuildVersionName String versionName, DropbitAccountHelper dropbitAccountHelper) {
        this.badgeRenderer = badgeRenderer;
        this.navigationUtil = navigationUtil;
        this.versionName = versionName;
        this.dropbitAccountHelper = dropbitAccountHelper;
    }

    public void inflateDrawer(AppCompatActivity activity, TypedValue actionBarType) {
        if (actionBarType.resourceId == R.id.actionbar_up_on_with_nav_bar) {
            View root = activity.findViewById(R.id.cn_content_wrapper);
            wrapBaseLayoutWithDrawer(activity, root);
            inflate(activity);
            setupNavigationView();
            setupDrawerButtons();
            displayAppVersion();
        }
    }

    public void openDrawer() {
        if (drawerLayout == null) return;
        drawerLayout.openDrawer(GravityCompat.START);
    }

    public void closeDrawer() {
        if (drawerLayout == null) return;
        drawerLayout.closeDrawer(GravityCompat.START);
    }

    public void closeDrawerNoAnimation() {
        if (drawerLayout == null) return;
        drawerLayout.closeDrawer(GravityCompat.START, false);
    }

    public boolean isDrawerOpen() {
        if (drawerLayout == null) return false;

        return drawerLayout.isDrawerOpen(GravityCompat.START);
    }

    public void renderBadgeForUnverifiedDeviceIfNecessary() {
        if (drawerLayout == null) {
            return;
        }

        if (!dropbitAccountHelper.getHasVerifiedAccount()) {
            badgeRenderer.renderBadge((ImageView) drawerLayout.findViewById(R.id.contact_phone));
            badgeRenderer.renderBadge((Toolbar) drawerLayout.findViewById(R.id.toolbar));
        }
    }

    public void updatePriceOfBtcDisplay(USDCurrency price) {
        if (drawerLayout == null) return;

        TextView drawerPriceTxtView = drawerLayout.findViewById(R.id.drawer_action_price_text);

        if (!price.isZero()) drawerPriceTxtView.setText(price.toFormattedCurrency());
    }

    public void displayAppVersion() {
        if (drawerLayout == null) return;

        TextView version = withId(drawerLayout, R.id.drawer_action_footer_version);
        version.setText(Resources.INSTANCE.getString(version.getContext(), R.string.app_version_label, versionName));
    }

    public void showBackupNowDrawerActions() {
        if (drawerLayout == null) return;

        badgeRenderer.renderBadge((Toolbar) drawerLayout.findViewById(R.id.toolbar));
        badgeRenderer.renderBadge((ImageView) drawerLayout.findViewById(R.id.setting_icon));
        Button backupNowButton = drawerLayout.findViewById(R.id.drawer_backup_now);
        backupNowButton.setVisibility(View.VISIBLE);
        backupNowButton.setOnClickListener(this::onBackupNowClicked);
    }

    public boolean onMenuItemClicked(MenuItem item) {
        if (drawerLayout == null) return false;

        if (item.getItemId() == android.R.id.home) {
            openDrawer();
            return true;
        }
        return false;
    }

    private void wrapBaseLayoutWithDrawer(AppCompatActivity activity, View root) {
        drawerLayout = new DrawerLayout(activity);
        drawerLayout.setFitsSystemWindows(true);
        ViewGroup screen = (ViewGroup) root.getParent();
        screen.removeView(root);
        screen.addView(drawerLayout);
        drawerLayout.addView(root);
    }

    private void inflate(AppCompatActivity activity) {
        activity.getLayoutInflater().inflate(R.layout.cn_drawer_layout, drawerLayout, true);
    }

    private void setupDrawerButtons() {
        drawerLayout.findViewById(R.id.drawer_setting).setOnClickListener(this::onSettingsClicked);
        drawerLayout.findViewById(R.id.drawer_support).setOnClickListener(this::onSupportClicked);
        drawerLayout.findViewById(R.id.drawer_where_to_buy).setOnClickListener(this::onWhereToSpendClicked);
        drawerLayout.findViewById(R.id.drawer_phone).setOnClickListener(this::onPhoneClicked);
        drawerLayout.findViewById(R.id.buy_bitcoin_drawer).setOnClickListener(this::onBuyBitcoinClicked);
    }

    private void onBuyBitcoinClicked(View view) {
        navigationUtil.navigtateToBuyBitcoin(view.getContext());
    }

    private void onPhoneClicked(View view) {
        navigationUtil.navigateToUserVerification(view.getContext());
    }

    private void setupNavigationView() {
        NavigationView navigationView = drawerLayout.findViewById(R.id.drawer_action_view);
        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(
                menuItem -> {
                    menuItem.setChecked(true);
                    closeDrawer();
                    return true;
                });
    }

    private void onSettingsClicked(View view) {
        Context context = view.getContext();

        navigationUtil.navigateToSettings(context);
    }

    private void onSupportClicked(View view) {
        Context context = view.getContext();

        navigationUtil.navigateToSupport(context);
    }

    private void onWhereToSpendClicked(View view) {
        navigationUtil.navigateToSpendBitcoin(view.getContext());
    }

    private void onBackupNowClicked(View view) {
        Context context = view.getContext();

        navigationUtil.navigateToBackupRecoveryWords(context);
    }
}
