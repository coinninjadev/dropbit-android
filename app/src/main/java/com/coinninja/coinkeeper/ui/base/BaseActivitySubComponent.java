package com.coinninja.coinkeeper.ui.base;

import dagger.Subcomponent;
import dagger.android.AndroidInjectionModule;
import dagger.android.AndroidInjector;

@ActivityScope
@Subcomponent(modules = {AndroidInjectionModule.class, AndroidActivityBuilder.class, AndroidFragmentBuilder.class})
public interface BaseActivitySubComponent extends AndroidInjector<BaseActivity> {

    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<BaseActivity> {};

}
