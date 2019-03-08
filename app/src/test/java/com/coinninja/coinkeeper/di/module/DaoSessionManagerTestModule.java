package com.coinninja.coinkeeper.di.module;

import com.coinninja.coinkeeper.di.interfaces.CoinkeeperApplicationScope;
import com.coinninja.coinkeeper.model.helpers.DaoSessionManager;

import dagger.Module;
import dagger.Provides;

import static org.mockito.Mockito.mock;

@Module
public class DaoSessionManagerTestModule {

    @CoinkeeperApplicationScope
    @Provides
    DaoSessionManager daoSessionManager() {
        return mock(DaoSessionManager.class);
    }
}
