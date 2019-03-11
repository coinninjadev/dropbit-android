package com.coinninja.android.helpers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Vibrator;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.coinninja.coinkeeper.R;

public class Views {


    @SuppressWarnings("unchecked")
    public static <T extends View> T withId(Activity activity, int resourceId) {
        return (T) activity.findViewById(resourceId);
    }

    @SuppressWarnings("unchecked")
    public static <T extends View> T withId(AlertDialog view, int resourceId) {
        return (T) view.findViewById(resourceId);
    }

    @SuppressWarnings("unchecked")
    public static <T extends View> T withId(View view, int resourceId) {
        return (T) view.findViewById(resourceId);
    }

    public static void makeViewGone(View view, int resourceId) {
        withId(view, resourceId).setVisibility(View.GONE);
    }

    public static void makeViewInvisibile(View view, int resourceId) {
        withId(view, resourceId).setVisibility(View.INVISIBLE);
    }

    public static void makeViewVisibile(View view, int resourceId) {
        withId(view, resourceId).setVisibility(View.VISIBLE);
    }

    public static void shakeInError(View view) {
        Animation animation = AnimationUtils.loadAnimation(view.getContext(), R.anim.shake_view);
        Vibrator vibrator = (Vibrator) view.getContext().getSystemService(Context.VIBRATOR_SERVICE);
        view.startAnimation(animation);
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                vibrator.cancel();
            }
        }, 250);
        long[] pattern = {25, 100, 25, 100};
        vibrator.vibrate(pattern, 0);
    }
}
