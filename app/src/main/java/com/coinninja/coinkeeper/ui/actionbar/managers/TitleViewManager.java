package com.coinninja.coinkeeper.ui.actionbar.managers;


import android.view.View;
import android.widget.TextView;

import javax.inject.Inject;

import androidx.appcompat.app.ActionBar;

public class TitleViewManager {
    private ActionBar actionBar;
    private TextView titleView;

    @Inject
    public TitleViewManager() {
    }

    public void renderTitleView() {
        String title = getTitle();
        if (isTitleValid(title)) {
            setTitle(title);
        } else {
            removeTitle();
        }
    }

    public void renderTitleView(String string) {
        setTitle(string);
    }

    public void renderUpperCaseTitleView() {
        String title = getTitle();
        if (isTitleValid(title)) {
            setTitle(title.toUpperCase());
        } else {
            removeTitle();
        }

    }

    public String getTitle() {
        String title = actionBar.getTitle().toString();
        actionBar.setTitle("");
        return title;
    }


    private void setTitle(String title) {
        if ((title != null && title.equals("")) || title == null) {
            titleView.setVisibility(View.GONE);
            return;
        }

        titleView.setVisibility(View.VISIBLE);
        titleView.setText(title);
    }

    private void removeTitle() {
        titleView.setVisibility(View.GONE);
    }

    public void setActionBar(ActionBar actionBar) {
        this.actionBar = actionBar;
    }

    public void setTitleView(TextView titleView) {
        this.titleView = titleView;
    }

    public boolean isTitleValid(String title) {
        return title != null && !title.isEmpty();
    }
}
