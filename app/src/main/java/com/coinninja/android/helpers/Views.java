package com.coinninja.android.helpers;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.View;

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

}
