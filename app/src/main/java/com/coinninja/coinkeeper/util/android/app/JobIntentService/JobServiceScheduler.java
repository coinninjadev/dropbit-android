package com.coinninja.coinkeeper.util.android.app.JobIntentService;

import android.annotation.SuppressLint;
import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobInfo.Builder;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.JobIntentService;

import javax.inject.Inject;

public class JobServiceScheduler {

    public static final int SYNC_NOW_SERVICE_JOB_ID = 104;
    public static final int SYNC_HOURLY_SERVICE_JOB_ID = 106;
    public static final int ENDPOINT_REGISTRATION_SERVICE_JOB_ID = 100;
    public static final int GLOBAL_MESSAGING_SERVICE_JOB_ID = 102;
    public static final int BROADCAST_NOTIFICATION_SERVICE = 107;
    public static final int CONTACT_LOOKUP_SERVICE = 108;

    private JobScheduler jobScheduler;

    @Inject
    public JobServiceScheduler(JobScheduler jobScheduler) {
        this.jobScheduler = jobScheduler;
    }

    public void enqueueWork(Context context, Class cls, int jobId, Intent intent) {
        JobIntentService.enqueueWork(context, cls, jobId, intent);
    }

    public void schedule(Context context, int jobId, Class<? extends Service> job,
                         int networkType, long repeatFrequency, boolean isPersistant) {
        schduleJob(context, jobId, job, networkType, repeatFrequency, isPersistant);
    }

    public void cancelJob(int jobId) {
        jobScheduler.cancel(jobId);
    }

    @SuppressLint("ServiceCast")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void schduleJob(Context context, int jobId, Class<? extends Service> job,
                            int networkType, long repeatFrequency, boolean isPersistant) {
        if (isScheduled(jobId, jobScheduler)) return;

        JobInfo jobInfo = new Builder(jobId, new ComponentName(context, job.getName()))
                .setRequiredNetworkType(networkType)
                .setPeriodic(repeatFrequency)
                .setPersisted(isPersistant)
                .build();
        if (jobScheduler != null) {
            jobScheduler.schedule(jobInfo);
        }
    }

    private boolean isScheduled(int jobId, JobScheduler jobScheduler) {
        if (jobScheduler != null) {
            for (JobInfo scheduledJob : jobScheduler.getAllPendingJobs()) {
                if (scheduledJob.getId() == jobId)
                    return true;
            }
        }
        return false;
    }
}
