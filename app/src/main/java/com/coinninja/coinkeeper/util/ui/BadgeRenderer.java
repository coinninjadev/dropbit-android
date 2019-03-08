package com.coinninja.coinkeeper.util.ui;

import android.widget.ImageView;

import javax.inject.Inject;

import androidx.appcompat.widget.Toolbar;

public class BadgeRenderer {
    @Inject
    BadgeRenderer() {

    }

    public void renderBadge(ImageView imageView) {
        if (imageView.getDrawable() instanceof BadgeOverlay)
            return;

        imageView.setImageDrawable(
                BadgeOverlay.newInstance(
                        imageView.getResources(),
                        imageView.getDrawable()
                )
        );
    }

    public void renderBadge(Toolbar toolbar) {
        if (toolbar.getNavigationIcon() instanceof BadgeOverlay)
            return;

        toolbar.setNavigationIcon(
                BadgeOverlay.newInstance(
                        toolbar.getResources(),
                        toolbar.getNavigationIcon()
                )
        );
    }


}
