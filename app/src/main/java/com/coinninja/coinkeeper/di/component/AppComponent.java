package com.coinninja.coinkeeper.di.component;

import android.app.Application;

import com.coinninja.coinkeeper.CoinKeeperApplication;
import com.coinninja.coinkeeper.cn.wallet.CNWalletManager;
import com.coinninja.coinkeeper.cn.wallet.HDWallet;
import com.coinninja.coinkeeper.cn.wallet.runner.SaveRecoveryWordsRunner;
import com.coinninja.coinkeeper.di.builder.AndroidBroadcastReceiverBuilder;
import com.coinninja.coinkeeper.di.builder.AndroidServiceBuilder;
import com.coinninja.coinkeeper.di.interfaces.CoinkeeperApplicationScope;
import com.coinninja.coinkeeper.di.module.ApiClientModule;
import com.coinninja.coinkeeper.di.module.AppModule;
import com.coinninja.coinkeeper.di.module.DaoSessionManagerModule;
import com.coinninja.coinkeeper.interactor.UserPreferences;
import com.coinninja.coinkeeper.interfaces.PinEntry;
import com.coinninja.coinkeeper.model.helpers.TargetStatHelper;
import com.coinninja.coinkeeper.model.helpers.UserHelper;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.service.client.CoinKeeperApiClient;
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient;
import com.coinninja.coinkeeper.service.runner.FullSyncWalletRunner;
import com.coinninja.coinkeeper.ui.base.AndroidActivityBuilder;
import com.coinninja.coinkeeper.ui.base.AndroidFragmentBuilder;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.android.ClipboardUtil;
import com.coinninja.coinkeeper.util.android.PreferencesUtil;

import java.util.Locale;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjectionModule;

@CoinkeeperApplicationScope
@Component(modules = {AndroidInjectionModule.class, AppModule.class, ApiClientModule.class,
        DaoSessionManagerModule.class, AndroidServiceBuilder.class, AndroidActivityBuilder.class,
        AndroidBroadcastReceiverBuilder.class, AndroidFragmentBuilder.class})
public interface AppComponent {


    void inject(CoinKeeperApplication application);

    FullSyncWalletRunner getFullSyncRunner();

    SaveRecoveryWordsRunner getSaveRecoveryWordsRunner();

    Analytics getAnalytics();

    Locale getLocale();

    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder application(Application application);

        AppComponent build();
    }

}
