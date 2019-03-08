package com.coinninja.coinkeeper.service.runner;

import android.os.AsyncTask;

public abstract class SteppedAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

    public static final float MAX_PROGRESS = 1F;
    private int currentStep = 0;
    private float incrementWeight;
    private float currentProgress = 0.0F;

    public SteppedAsyncTask() {
        incrementWeight = 1F / getNumberOfPrimarySteps();
    }

    abstract int getNumberOfPrimarySteps();

    public float getProgress() {
        return currentProgress;
    }

    public void reportPrimaryStepCompleted() {
        currentStep += 1;
        currentProgress = currentStep * incrementWeight;

        if (currentProgress >= MAX_PROGRESS) {
            currentProgress = MAX_PROGRESS;
        }
    }

    public void reportSubStepCompleted(int numSubSteps) {
        currentProgress += incrementWeight * (MAX_PROGRESS / numSubSteps);
    }
}
