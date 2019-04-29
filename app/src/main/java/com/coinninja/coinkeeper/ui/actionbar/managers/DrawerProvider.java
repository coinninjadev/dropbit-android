package com.coinninja.coinkeeper.ui.actionbar.managers;

import javax.inject.Inject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

public class DrawerProvider {
    @Inject
    public DrawerProvider() {

    }

    public DrawerLayout createDrawer(AppCompatActivity activity) {
        DrawerLayout drawerLayout = new DrawerLayout(activity);
        return drawerLayout;
    }
}
