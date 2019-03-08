package com.coinninja.coinkeeper.ui.actionbar.managers;

import android.app.Activity;

import javax.inject.Inject;

import androidx.drawerlayout.widget.DrawerLayout;

public class DrawerProvider {
    @Inject
    public DrawerProvider() {

    }

    public DrawerLayout createDrawer(Activity activity) {
        DrawerLayout drawerLayout = new DrawerLayout(activity);
        return drawerLayout;
    }
}
