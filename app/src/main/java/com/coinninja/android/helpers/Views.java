package com.coinninja.android.helpers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Vibrator;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.util.DefaultCurrencies;

import androidx.annotation.Nullable;

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

    public static void makeViewInvisible(View view, int resourceId) {
        withId(view, resourceId).setVisibility(View.INVISIBLE);
    }

    public static void makeViewVisible(View view, int resourceId) {
        withId(view, resourceId).setVisibility(View.VISIBLE);
    }

    public static void clickOn(View view, int resourceId) {
        clickOn(withId(view, resourceId));
    }

    public static void clickOn(AlertDialog dialog, int resourceId) {
        clickOn(withId(dialog, resourceId));
    }

    public static void clickOn(@Nullable View view) {
        if (view != null)
            view.performClick();
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

    public static void renderBTCIconOnCurrencyViewPair(Context context, DefaultCurrencies defaultCurrencies, TextView primaryCurrencyView,
                                                       double primaryScale, TextView secondaryCurrencyView, double secondaryScale) {
        Drawable drawable = defaultCurrencies.getCrypto().getSymbolDrawable(context);
        if (defaultCurrencies.getPrimaryCurrency().isCrypto()) {
            drawSymbol(context, primaryCurrencyView, secondaryCurrencyView, primaryScale, drawable);
        } else {
            drawSymbol(context, secondaryCurrencyView, primaryCurrencyView, secondaryScale, drawable);
        }
    }

    private static void drawSymbol(Context context, TextView viewToDraw, TextView viewToClear, double scale, Drawable drawable) {
        drawable.setBounds(0, 0,
                (int) (drawable.getIntrinsicWidth() * scale),
                (int) (drawable.getIntrinsicHeight() * scale));
        viewToDraw.setCompoundDrawables(drawable, null, null, null);
        viewToClear.setCompoundDrawables(null, null, null, null);
    }

    public static void clearCompoundDrawablesOn(TextView view) {
        view.setCompoundDrawables(null, null, null, null);
    }
}
