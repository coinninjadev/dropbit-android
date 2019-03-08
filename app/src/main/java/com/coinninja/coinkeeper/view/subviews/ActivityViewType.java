package com.coinninja.coinkeeper.view.subviews;

import android.app.Activity;
import android.view.View;

interface ActivityViewType {

    /**
     * @param activity component belongs to
     * @param rootView view component is responsible for managing
     */
    void render(Activity activity, View rootView);
    void tearDown();
}
