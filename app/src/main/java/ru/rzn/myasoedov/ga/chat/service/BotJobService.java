package ru.rzn.myasoedov.ga.chat.service;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Build;
import android.util.Log;

import ru.rzn.myasoedov.ga.chat.ChatApplication;

/**
 * Created by User on 19.08.2016.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class BotJobService extends JobService {
    private static final String TAG = "BotJobService";

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "on start job: " + params.getJobId());
        sendBroadcast(((ChatApplication) getApplication()).createIntent());
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

}
