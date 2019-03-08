package com.coinninja.coinkeeper;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.coinninja.coinkeeper.di.interfaces.CoinkeeperApplicationScope;

import java.util.ArrayList;

import javax.inject.Inject;

@CoinkeeperApplicationScope
public class CoinKeeperLifecycleListener implements Application.ActivityLifecycleCallbacks {

    private ArrayList<ForegroundStatusChangeReceiver> foregroundStatusChangeReceivers = new ArrayList<>();

    int startedActivityCounter = 0;

    @Inject
    public CoinKeeperLifecycleListener() {
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {
        boolean notificationRequired = !isAppInForeground();

        startedActivityCounter++;

        if (notificationRequired) {
            notifyForegrounded();
        }
    }

    private void notifyForegrounded() {
        for (ForegroundStatusChangeReceiver receiver : foregroundStatusChangeReceivers) {
            if(receiver != null)
                receiver.onBroughtToForeground();
        }
    }

    private void notifyBackgrounded() {
        for (ForegroundStatusChangeReceiver receiver : foregroundStatusChangeReceivers) {
            if(receiver != null)
                receiver.onSentToBackground();
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {
        boolean initialState = isAppInForeground();

        startedActivityCounter--;

        if (initialState && !isAppInForeground()) {
            notifyBackgrounded();
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    public boolean isAppInForeground() {
        return startedActivityCounter > 0;
    }

    public void registerReceiver(ForegroundStatusChangeReceiver receiver) {
        foregroundStatusChangeReceivers.add(receiver);
    }
}
