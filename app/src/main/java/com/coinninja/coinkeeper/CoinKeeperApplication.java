package com.coinninja.coinkeeper;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.coinninja.coinkeeper.di.component.AppComponent;
import com.coinninja.coinkeeper.di.component.CoinKeeperComponent;
import com.coinninja.coinkeeper.di.component.DaggerAppComponent;
import com.coinninja.coinkeeper.interfaces.Authentication;
import com.coinninja.coinkeeper.receiver.ApplicationStartedReceiver;
import com.coinninja.coinkeeper.util.DropbitIntents;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.support.DaggerApplication;

import static com.coinninja.coinkeeper.R.string;

public class CoinKeeperApplication extends DaggerApplication {

    public static final String INVITES_SERVICE_CHANNEL_ID = "com.coinninja.coinkeeper.service.INVITES";
    public static CoinKeeperComponent appComponent;
    @Inject
    CoinKeeperLifecycleListener coinKeeperLifecycleListener;
    @Inject
    Authentication authentication;
    @Inject
    LocalBroadCastUtil localBroadCastUtil;

    @Override
    public void onCreate() {
        super.onCreate();
        registerNotificationChannels();
        notifyOfStart();
        registerActivityLifecycleCallbacks(coinKeeperLifecycleListener);
    }

    @Deprecated
    public Authentication getAuthentication() {
        return authentication;
    }

    @Override
    protected AndroidInjector<? extends DaggerApplication> applicationInjector() {
        appComponent = DaggerAppComponent.builder().application(this).build();
        ((AppComponent) appComponent).inject(this);
        return (AndroidInjector<? extends DaggerApplication>) appComponent;
    }

    protected void notifyOfStart() {
        localBroadCastUtil.sendGlobalBroadcast(ApplicationStartedReceiver.class, DropbitIntents.ACTION_ON_APPLICATION_START);
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
