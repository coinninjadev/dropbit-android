package com.coinninja.coinkeeper.ui.base;

import com.coinninja.coinkeeper.di.builder.AndroidFragmentBuilder;
import com.coinninja.coinkeeper.di.interfaces.ActivityScope;

import dagger.Subcomponent;
import dagger.android.AndroidInjectionModule;
import dagger.android.AndroidInjector;

@ActivityScope
@Subcomponent(modules = {AndroidInjectionModule.class, AndroidFragmentBuilder.class})
public interface BaseDialogSubcomponent extends AndroidInjector<BaseDialogFragment> {
    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<BaseDialogFragment> {};
}
