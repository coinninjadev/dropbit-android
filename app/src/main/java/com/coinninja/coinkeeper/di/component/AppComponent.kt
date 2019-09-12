package com.coinninja.coinkeeper.di.component

import android.app.Application
import com.coinninja.coinkeeper.CoinKeeperApplication
import com.coinninja.coinkeeper.cn.wallet.tx.TransactionFundingProvider
import com.coinninja.coinkeeper.di.builder.AndroidActivityBuilder
import com.coinninja.coinkeeper.di.builder.AndroidBroadcastReceiverBuilder
import com.coinninja.coinkeeper.di.builder.AndroidFragmentBuilder
import com.coinninja.coinkeeper.di.builder.AndroidServiceBuilder
import com.coinninja.coinkeeper.di.interfaces.CoinkeeperApplicationScope
import com.coinninja.coinkeeper.di.module.*
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication

@CoinkeeperApplicationScope
@Component(modules = [
    AndroidInjectionModule::class,
    FeeManagerModule::class,
    AppModule::class,
    ApiClientModule::class,
    DaoSessionManagerModule::class,
    AndroidServiceBuilder::class,
    AndroidActivityBuilder::class,
    AndroidBroadcastReceiverBuilder::class,
    AndroidFragmentBuilder::class,
    TransactionFundingProvider::class,
    SyncModule::class,
    ThunderDomeModule::class,
    LibBitcoinModule::class
])
interface AppComponent : AndroidInjector<DaggerApplication>, CoinKeeperComponent {

    fun inject(application: CoinKeeperApplication)


    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder


        fun build(): AppComponent
    }

}
