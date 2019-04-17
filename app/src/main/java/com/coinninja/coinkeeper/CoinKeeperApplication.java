package com.coinninja.coinkeeper;

import android.app.Activity;
import android.app.Application;
import android.app.Fragment;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.os.Build;

import com.coinninja.coinkeeper.di.component.AppComponent;
import com.coinninja.coinkeeper.di.component.DaggerAppComponent;
import com.coinninja.coinkeeper.interfaces.Authentication;
import com.coinninja.coinkeeper.receiver.ApplicationStartedReceiver;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;

import javax.inject.Inject;

import androidx.annotation.RequiresApi;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import dagger.android.HasBroadcastReceiverInjector;
import dagger.android.HasFragmentInjector;
import dagger.android.HasServiceInjector;

import static com.coinninja.coinkeeper.R.string;

public class CoinKeeperApplication extends Application implements HasServiceInjector, HasActivityInjector, HasFragmentInjector, HasBroadcastReceiverInjector {
    public static final String INVITES_SERVICE_CHANNEL_ID = "com.coinninja.coinkeeper.service.INVITES";
    public static AppComponent appComponent;
    @Inject
    DispatchingAndroidInjector<Activity> dispatchingAndroidActivityInjector;
    @Inject
    DispatchingAndroidInjector<Service> dispatchingAndroidServiceInjector;
    @Inject
    DispatchingAndroidInjector<Fragment> dispatchingAndroidFragmentInjector;
    @Inject
    DispatchingAndroidInjector<BroadcastReceiver> dispatchingAndroidBroadcastReceiverInjector;
    @Inject
    CoinKeeperLifecycleListener coinKeeperLifecycleListener;
    @Inject
    Authentication authentication;
    @Inject
    LocalBroadCastUtil localBroadCastUtil;

    @Override
    public void onCreate() {
        super.onCreate();
        createComponent();
        registerNotificationChannels();
        notifyOfStart();
        registerActivityLifecycleCallbacks(coinKeeperLifecycleListener);
    }

    public AppComponent getAppComponent() {
        return appComponent;
    }

    @Deprecated
    public Authentication getAuthentication() {
        return authentication;
    }

    @Override
    public AndroidInjector<BroadcastReceiver> broadcastReceiverInjector() {
        return dispatchingAndroidBroadcastReceiverInjector;
    }

    @Override
    public AndroidInjector<Service> serviceInjector() {
        return dispatchingAndroidServiceInjector;
    }

    @Override
    public AndroidInjector<Activity> activityInjector() {
        return dispatchingAndroidActivityInjector;
    }

    @Override
    public AndroidInjector<Fragment> fragmentInjector() {
        return dispatchingAndroidFragmentInjector;
    }

    protected void createComponent() {
        appComponent = DaggerAppComponent.builder().application(this).build();
        appComponent.inject(this);
    }

    protected void notifyOfStart() {
        localBroadCastUtil.sendGlobalBroadcast(ApplicationStartedReceiver.class, Intents.ACTION_ON_APPLICATION_START);
    }

    @Deprecated
    protected void registerNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerInvitesNotificationChannel();
        }
    }

    @Deprecated
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void registerInvitesNotificationChannel() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager.getNotificationChannel(INVITES_SERVICE_CHANNEL_ID) != null) {
            return;
        }

        CharSequence name = getString(string.channel_invites);
        String description = getString(string.channel_invites_description);
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel mChannel = new NotificationChannel(INVITES_SERVICE_CHANNEL_ID, name, importance);
        mChannel.setDescription(description);
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (null != notificationManager) {
            notificationManager.createNotificationChannel(mChannel);
        }
    }

}
