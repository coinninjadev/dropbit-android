package com.coinninja.coinkeeper.ui.base

import com.coinninja.coinkeeper.di.builder.AndroidActivityBuilder
import com.coinninja.coinkeeper.di.builder.AndroidFragmentBuilder
import com.coinninja.coinkeeper.di.interfaces.ActivityScope
import dagger.Subcomponent
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector

@ActivityScope
@Subcomponent(modules = [AndroidInjectionModule::class, AndroidActivityBuilder::class, AndroidFragmentBuilder::class])
interface BaseActivitySubComponent : AndroidInjector<BaseActivity> {

    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<BaseActivity>()

}
