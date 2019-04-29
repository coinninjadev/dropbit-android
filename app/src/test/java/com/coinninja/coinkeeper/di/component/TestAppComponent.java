package com.coinninja.coinkeeper.di.component;

import android.app.Application;

import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.di.builder.AndroidBroadcastReceiverBuilder;
import com.coinninja.coinkeeper.di.builder.AndroidServiceBuilder;
import com.coinninja.coinkeeper.di.interfaces.CoinkeeperApplicationScope;
import com.coinninja.coinkeeper.di.module.ApiClientTestModule;
import com.coinninja.coinkeeper.di.module.DaoSessionManagerTestModule;
import com.coinninja.coinkeeper.di.module.TestAppModule;
import com.coinninja.coinkeeper.ui.base.AndroidActivityBuilder;
import com.coinninja.coinkeeper.ui.base.AndroidFragmentBuilder;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjectionModule;
import dagger.android.AndroidInjector;

@CoinkeeperApplicationScope
@Component(modules = {AndroidInjectionModule.class, TestAppModule.class, ApiClientTestModule.class, DaoSessionManagerTestModule.class, AndroidServiceBuilder.class,
        AndroidBroadcastReceiverBuilder.class, AndroidFragmentBuilder.class, AndroidActivityBuilder.class})
public interface TestAppComponent extends AndroidInjector, CoinKeeperComponent {

    void inject(TestCoinKeeperApplication application);

    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder application(Application application);

        TestAppComponent build();
    }

}
