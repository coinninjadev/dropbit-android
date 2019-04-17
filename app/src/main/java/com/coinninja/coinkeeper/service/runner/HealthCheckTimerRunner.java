package com.coinninja.coinkeeper.service.runner;

import com.coinninja.coinkeeper.service.tasks.CNHealthCheckTask;

import javax.inject.Inject;

public class HealthCheckTimerRunner implements Runnable {
    private final CNHealthCheckTask task;

    @Inject
    HealthCheckTimerRunner(CNHealthCheckTask task) {
        this.task = task;
    }

    public void setCallback(CNHealthCheckTask.HealthCheckCallback callback) {
        task.setCallback(callback);
    }

    @Override
    public void run() {
        task.clone().execute();
    }
}
