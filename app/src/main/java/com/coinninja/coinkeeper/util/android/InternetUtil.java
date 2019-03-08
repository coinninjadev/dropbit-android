package com.coinninja.coinkeeper.util.android;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class InternetUtil {

    private final ConnectivityManager connectivityManager;

    private InternetUtil(ConnectivityManager connectivityManager) {
        this.connectivityManager = connectivityManager;
    }

    public static InternetUtil newInstance(Context context) {
        return new InternetUtil((ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE));
    }

    public ConnectivityManager getConnectivityManager() {
        return connectivityManager;
    }

    public boolean hasInternet() {
        NetworkInfo stats = getConnectivityManager().getActiveNetworkInfo();

        return stats != null && stats.isConnected();
    }
}
