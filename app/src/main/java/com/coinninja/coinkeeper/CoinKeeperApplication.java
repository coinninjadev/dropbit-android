package com.coinninja.coinkeeper;

import android.app.Activity;
import android.app.Application;
import android.app.Fragment;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.os.Build;

import com.coinninja.coinkeeper.cn.wallet.HDWallet;
import com.coinninja.coinkeeper.di.component.AppComponent;
import com.coinninja.coinkeeper.di.component.DaggerAppComponent;
import com.coinninja.coinkeeper.interfaces.Authentication;
import com.coinninja.coinkeeper.interfaces.PinEntry;
import com.coinninja.coinkeeper.model.db.DaoSession;
import com.coinninja.coinkeeper.model.helpers.AddressHelper;
import com.coinninja.coinkeeper.model.helpers.DaoSessionManager;
import com.coinninja.coinkeeper.model.helpers.UserHelper;
import com.coinninja.coinkeeper.qrscanner.QRScanManager;
import com.coinninja.coinkeeper.receiver.ApplicationStartedReceiver;
import com.coinninja.coinkeeper.service.client.CoinKeeperApiClient;
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient;
import com.coinninja.coinkeeper.service.tasks.CNHealthCheckTask;
import com.coinninja.coinkeeper.service.tasks.CoinNinjaUserQueryTask;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.LocalContactQueryUtil;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

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
    AddressHelper addressHelper;
    @Inject
    CoinKeeperLifecycleListener coinKeeperLifecycleListener;
    @Inject
    Authentication authentication;
    @Inject
    Analytics analytics;
    @Inject
    DaoSessionManager daoSessionManager;
    @Inject
    LocalBroadCastUtil localBroadCastUtil;

    @Override
    public void onCreate() {
        super.onCreate();
        createComponent();
        registerNotificationChannels();
        notifiyStart();
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

    @Deprecated
    public DaoSession getDaoSession() {
        return daoSessionManager.getDaoSession();
    }

    @Deprecated
    public PinEntry getPinEntry() {
        return appComponent.getPinEntry();
    }

    @Deprecated
    public CoinKeeperApiClient getAPIClient() {
        return appComponent.getCnApiClient();
    }

    @Deprecated
    public HDWallet getHDWallet() {
        if (appComponent.getWalletManager().hasWallet()) {
            return appComponent.getHDWallet();
        }
        throw new IllegalStateException("Attempting to access a wallet without seed words");
    }

    @Deprecated
    public UserHelper getUser() {
        return appComponent.getUserHelper();
    }

    @Deprecated
    public SignedCoinKeeperApiClient getSecuredClient() {
        return appComponent.getSignedApiClient();
    }

    @Deprecated
    public LocalBroadCastUtil getLocalBroadCastUtil() {
        return new LocalBroadCastUtil(this);
    }

    @Deprecated
    public CNHealthCheckTask getHealthCheckTask(CNHealthCheckTask.HealthCheckCallback callback) {
        return new CNHealthCheckTask(this, callback);
    }

    @Deprecated
    public CoinNinjaUserQueryTask getCoinNinjaUserQueryTask(SignedCoinKeeperApiClient client, LocalContactQueryUtil localContactQueryUtil, CoinNinjaUserQueryTask.OnCompleteListener onCompleteListener) {
        return new CoinNinjaUserQueryTask(client, localContactQueryUtil, onCompleteListener);
    }

    @Deprecated
    public QRScanManager getScanManager(Activity activity, DecoratedBarcodeView barcodeScannerView, QRScanManager.OnScanListener onScanListener) {
        return new QRScanManager(activity, barcodeScannerView, onScanListener);
    }

    protected void createComponent() {
        appComponent = DaggerAppComponent.builder().application(this).build();
        appComponent.inject(this);
    }

    protected void notifiyStart() {
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
