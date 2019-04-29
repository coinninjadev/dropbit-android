package com.coinninja.coinkeeper.view.subviews;

import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

interface ActivityViewType {

    /**
     * @param activity component belongs to
     * @param rootView view component is responsible for managing
     */
    void render(AppCompatActivity activity, View rootView);

    void tearDown();
}
