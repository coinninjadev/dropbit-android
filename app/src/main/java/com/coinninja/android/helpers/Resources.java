package com.coinninja.android.helpers;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;

public class Resources {
    public static String getString(Context context, int resourceId) {
        return context.getResources().getString(resourceId);
    }

    public static String getString(Context context, int resourceId, String... formats) {
        return context.getResources().getString(resourceId, (Object[]) formats);
    }

    public static Drawable getDrawable(Context context, int resourceId) {
        return context.getResources().getDrawable(resourceId);
    }

    public static int getColor(Context context, int resourceId) {
        return context.getResources().getColor(resourceId);
    }

    public static float scaleValue(Context context, int unit, float value) {
        return TypedValue.applyDimension(unit, value, context.getResources().getDisplayMetrics());
    }
}
