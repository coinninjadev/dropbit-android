package com.coinninja.coinkeeper.view.subviews;

import android.view.View;

public abstract class AbstractBasicViewType implements ViewType {
    protected final View view;

    AbstractBasicViewType(View view) {
        this.view = view;
        render();
    }
}
