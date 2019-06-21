package com.coinninja.coinkeeper.util.android.app.JobIntentService;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;

import com.coinninja.coinkeeper.service.WalletTransactionRetrieverService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class JobServiceSchedulerTest {

    @Mock
    JobScheduler jobScheduler;

    @InjectMocks
    JobServiceScheduler jobServiceScheduler;

    List<JobInfo> scheduledJobs = new ArrayList<>();


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(jobScheduler.getAllPendingJobs()).thenReturn(scheduledJobs);
    }

    @Test
    public void scheudles_job() {
        ArgumentCaptor<JobInfo> jobInfoArgumentCaptor = ArgumentCaptor.forClass(JobInfo.class);
        Context context = mock(Context.class);

        jobServiceScheduler.schedule(context, 105, WalletTransactionRetrieverService.class,
                JobInfo.NETWORK_TYPE_ANY, 60L * 60L * 1000L, true);

        verify(jobScheduler).schedule(jobInfoArgumentCaptor.capture());

        JobInfo jobInfo = jobInfoArgumentCaptor.getValue();

        assertThat(jobInfo.getId(), equalTo(105));
        assertThat(jobInfo.getService().getClassName(), equalTo(WalletTransactionRetrieverService.class.getName()));
        assertThat(jobInfo.getNetworkType(), equalTo(JobInfo.NETWORK_TYPE_ANY));
        assertThat(jobInfo.isPeriodic(), equalTo(true));
        assertThat(jobInfo.getIntervalMillis(), equalTo(3600000L));
        assertThat(jobInfo.isPersisted(), equalTo(true));
    }

    @Test
    public void only_schedules_job_when_it_is_not_scheduled() {
        JobInfo job = new JobInfo.Builder(106,
                new ComponentName(RuntimeEnvironment.application,
                        WalletTransactionRetrieverService.class))
                .setPersisted(true)
                .setPeriodic(3600000L)
                .build();
        scheduledJobs.add(job);
        Context context = mock(Context.class);

        jobServiceScheduler.schedule(context, 106, WalletTransactionRetrieverService.class,
                JobInfo.NETWORK_TYPE_ANY, 60L * 60L * 1000L, true);

        verify(jobScheduler, times(0)).schedule(any());
    }

    @Test
    public void can_cancel_a_schduled_job() {
        jobServiceScheduler.cancelJob(106);

        verify(jobScheduler).cancel(106);
    }

}