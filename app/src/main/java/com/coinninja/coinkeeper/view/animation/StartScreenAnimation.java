package com.coinninja.coinkeeper.view.animation;


import android.content.res.Resources;
import android.transition.ChangeBounds;
import android.view.View;
import android.view.animation.AlphaAnimation;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.view.activity.StartActivity;

public class StartScreenAnimation {

    private final StartActivity activity;
    private final Resources resources;

    private final View relLayFooter;
    private final View txtSlogan;

    public StartScreenAnimation(StartActivity activity) {
        this.activity = activity;
        resources = activity.getResources();

        relLayFooter = activity.findViewById(R.id.start_footer_rellayout);
        txtSlogan = activity.findViewById(R.id.start_txt_slogan);
    }

    public void animateOut() {
        activity.overridePendingTransition(0, 0);
    }

    public void animateIn() {
        int logoAnimLength = resources.getInteger(R.integer.logo_animation_length_ms);

        activity.getWindow().setEnterTransition(null);
        ChangeBounds bounds = new ChangeBounds();
        bounds.setDuration(logoAnimLength);
        activity.getWindow().setSharedElementEnterTransition(bounds);

        int footerAnimLength = resources.getInteger(R.integer.footer_animation_length_ms);
        int sloganAnimLength = resources.getInteger(R.integer.slogan_animation_length_ms);

        AlphaAnimation animFadeInFooter = new AlphaAnimation(0.0f, 1.0f);
        animFadeInFooter.setDuration(footerAnimLength);
        relLayFooter.startAnimation(animFadeInFooter);

        AlphaAnimation animFadeInSlogan = new AlphaAnimation(0.0f, 1.0f);
        animFadeInSlogan.setDuration(sloganAnimLength);
        txtSlogan.startAnimation(animFadeInSlogan);
    }

}
