package com.coinninja.coinkeeper.service.runner;

import com.coinninja.coinkeeper.CoinKeeperApplication;
import com.coinninja.coinkeeper.service.tasks.CNHealthCheckTask;

import javax.inject.Inject;

public class HealthCheckTimerRunner implements Runnable {
    private final CoinKeeperApplication application;
    private CNHealthCheckTask.HealthCheckCallback callback;

    @Inject
    public HealthCheckTimerRunner(CoinKeeperApplication application) {
        this.application = application;
    }

    public void setCallback(CNHealthCheckTask.HealthCheckCallback callback) {
        this.callback = callback;
    }

    @Override
    public void run() {
        application.getHealthCheckTask(callback).execute();
    }
}
