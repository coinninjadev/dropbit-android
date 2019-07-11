package com.coinninja.coinkeeper.di.component;

import android.app.Application;

import com.coinninja.coinkeeper.CoinKeeperApplication;
import com.coinninja.coinkeeper.cn.wallet.tx.TransactionFundingProvider;
import com.coinninja.coinkeeper.di.builder.AndroidActivityBuilder;
import com.coinninja.coinkeeper.di.builder.AndroidBroadcastReceiverBuilder;
import com.coinninja.coinkeeper.di.builder.AndroidFragmentBuilder;
import com.coinninja.coinkeeper.di.builder.AndroidServiceBuilder;
import com.coinninja.coinkeeper.di.interfaces.CoinkeeperApplicationScope;
import com.coinninja.coinkeeper.di.module.ApiClientModule;
import com.coinninja.coinkeeper.di.module.AppModule;
import com.coinninja.coinkeeper.di.module.DaoSessionManagerModule;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjectionModule;
import dagger.android.AndroidInjector;

@CoinkeeperApplicationScope
@Component(modules = {AndroidInjectionModule.class, AppModule.class, ApiClientModule.class,
        DaoSessionManagerModule.class, AndroidServiceBuilder.class,
        AndroidActivityBuilder.class, AndroidBroadcastReceiverBuilder.class, AndroidFragmentBuilder.class,
        TransactionFundingProvider.class})
public interface AppComponent extends AndroidInjector, CoinKeeperComponent {

    void inject(CoinKeeperApplication application);


    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder application(Application application);


        AppComponent build();
    }

}
