package com.coinninja.coinkeeper.util.uri;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

public class UriUtil {

    public static void openUrl(Uri uri, Activity activity) {
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        activity.startActivity(intent);
    }
}
