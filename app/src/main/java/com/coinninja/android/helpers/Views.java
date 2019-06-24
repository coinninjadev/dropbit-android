package com.coinninja.android.helpers;

import android.app.Activity;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

public class Views {


    @SuppressWarnings("unchecked")
    public static <T extends View> T withId(Activity activity, int resourceId) {
        return (T) activity.findViewById(resourceId);
    }

    @SuppressWarnings("unchecked")
    public static <T extends View> T withId(AppCompatActivity activity, int resourceId) {
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

    public static void clickOn(@Nullable View view) {
        if (view != null)
            view.performClick();
    }

    public static void shakeInError(View view) {
        Animation animation = AnimationUtils.loadAnimation(view.getContext(), R.anim.shake_view);
        Vibrator vibrator = (Vibrator) view.getContext().getSystemService(Context.VIBRATOR_SERVICE);
        view.startAnimation(animation);
        view.postDelayed(() -> vibrator.cancel(), 250);
        long[] pattern = {25, 100, 25, 100};
        vibrator.vibrate(pattern, 0);
    }

    public static void rotate(View view) {
        Animation animation = AnimationUtils.loadAnimation(view.getContext(), R.anim.rotate);
        animation.setRepeatCount(Animation.INFINITE);
        view.startAnimation(animation);
    }

    public static void renderBTCIconOnCurrencyViewPair(Context context, DefaultCurrencies defaultCurrencies, TextView primaryCurrencyView,
                                                       double primaryScale, TextView secondaryCurrencyView, double secondaryScale) {
        Drawable drawable = defaultCurrencies.getCrypto().getSymbolDrawable(context);
        if (defaultCurrencies.getPrimaryCurrency().isCrypto()) {
            drawSymbol(primaryCurrencyView, secondaryCurrencyView, primaryScale, drawable);
        } else {
            drawSymbol(secondaryCurrencyView, primaryCurrencyView, secondaryScale, drawable);
        }
    }

    private static void drawSymbol(TextView viewToDraw, TextView viewToClear, double scale, Drawable drawable) {
        drawable.setBounds(0, 0,
                (int) (drawable.getIntrinsicWidth() * scale),
                (int) (drawable.getIntrinsicHeight() * scale));
        viewToDraw.setCompoundDrawables(drawable, null, null, null);
        viewToClear.setCompoundDrawables(null, null, null, null);
    }

    public static void setCompondDrawableOnStart(TextView view, int drawableId, float scale) {
        Drawable drawable = ResourcesCompat.getDrawable(view.getResources(), drawableId, null);
        drawable.setBounds(0, 0,
                (int) (drawable.getIntrinsicWidth() * scale),
                (int) (drawable.getIntrinsicHeight() * scale));
        view.setCompoundDrawables(drawable, null, null, null);
    }

    public static void clearCompoundDrawablesOn(TextView view) {
        view.setCompoundDrawables(null, null, null, null);
    }
}
