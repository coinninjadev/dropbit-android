package com.coinninja.coinkeeper.service.runner;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class SteppedAsyncTaskTest {


    private StepperTask task;

    @Test
    public void increments_sub_steps() {
        StepperTask.STEPS = 4;
        task = new StepperTask();

        task.reportSubStepCompleted(4);
        assertThat(task.getProgress(), equalTo(.0625F));

        task.reportSubStepCompleted(4);
        assertThat(task.getProgress(), equalTo(.125F));

        task.reportSubStepCompleted(4);
        assertThat(task.getProgress(), equalTo(.1875F));

        task.reportSubStepCompleted(4);
        assertThat(task.getProgress(), equalTo(.25F));

        task.reportPrimaryStepCompleted();
        assertThat(task.getProgress(), equalTo(.25F));

        task.reportSubStepCompleted(2);
        assertThat(task.getProgress(), equalTo(.375F));

        task.reportSubStepCompleted(2);
        assertThat(task.getProgress(), equalTo(.5F));

        task.reportPrimaryStepCompleted();
        assertThat(task.getProgress(), equalTo(.5F));

    }


    @Test
    public void stops_at_100() {
        StepperTask.STEPS = 4;
        task = new StepperTask();

        task.reportPrimaryStepCompleted();
        task.reportPrimaryStepCompleted();
        task.reportPrimaryStepCompleted();
        task.reportPrimaryStepCompleted();
        task.reportPrimaryStepCompleted();

        assertThat(task.getProgress(), equalTo(1F));
    }

    @Test
    public void reports_progress_for_primary_steps() {
        StepperTask.STEPS = 4;
        task = new StepperTask();

        task.reportPrimaryStepCompleted();
        assertThat(task.getProgress(), equalTo(.25F));

        task.reportPrimaryStepCompleted();
        assertThat(task.getProgress(), equalTo(.5F));

        task.reportPrimaryStepCompleted();
        assertThat(task.getProgress(), equalTo(.75F));
    }

    static class StepperTask extends SteppedAsyncTask<Void, Integer, Void> {
        static int STEPS = 0;

        @Override
        int getNumberOfPrimarySteps() {
            return STEPS;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            return null;
        }
    }
}