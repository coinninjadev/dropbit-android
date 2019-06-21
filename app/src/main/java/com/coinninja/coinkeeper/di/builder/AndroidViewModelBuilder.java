package com.coinninja.coinkeeper.di.builder;

import com.coinninja.coinkeeper.service.tasks.CoinNinjaUserViewModel;
import com.coinninja.coinkeeper.service.tasks.TwitterUserViewModel;
import com.coinninja.coinkeeper.view.adapter.util.PickUserViewModel;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class AndroidViewModelBuilder {
    abstract PickUserViewModel pickUserViewModel();

    abstract CoinNinjaUserViewModel coinNinjaUserViewModel();

    abstract TwitterUserViewModel twitterUserViewModel();
}
