package ru.rzn.myasoedov.ga.chat;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.List;

import ru.rzn.myasoedov.ga.chat.receiver.BotReceiver;
import ru.rzn.myasoedov.ga.chat.service.BotJobService;

/**
 * Created by grisha on 18.08.16.
 */
public class ChatApplication extends Application {
    public static final String BOT = "bot";
    public static final int JOB_ID = 10;
    private PendingIntent pendingIntent;
    private static boolean isChatShow;
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        isChatShow = false;
        context = getApplicationContext();
        //// TODO: 19.08.2016 get lat lon
        //stopBot();
        //startBot();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void startBot() {
        int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentApiVersion >= android.os.Build.VERSION_CODES.LOLLIPOP && !checkJobRunning()) {
            ComponentName serviceComponent = new ComponentName(this, BotJobService.class);
            JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, serviceComponent)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE)
                    .setRequiresDeviceIdle(true)
                    .setRequiresCharging(false)
                    .setPeriodic(BuildConfig.BOT_DEELAY);
            JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
            jobScheduler.schedule(builder.build());
        } else {
            Intent intent = createIntent();
            pendingIntent = PendingIntent.getBroadcast(this, 1, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + BuildConfig.BOT_DEELAY,
                    BuildConfig.BOT_DEELAY, pendingIntent);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private boolean checkJobRunning() {
        JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        List<JobInfo> jobs = jobScheduler.getAllPendingJobs();
        for (JobInfo jobInfo : jobs) {
            if (jobInfo.getId() == JOB_ID) {
                return true;
            }
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void stopBot() {
        int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentApiVersion >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
            jobScheduler.cancel(JOB_ID);
        } else {
            if (pendingIntent != null) {
                ((AlarmManager) getSystemService(ALARM_SERVICE)).cancel(pendingIntent);
            }
        }
    }

    public Intent createIntent() {
        Intent intent = new Intent(this, BotReceiver.class);
        intent.setAction(BOT);
        return intent;
    }

    public static boolean isChatShow() {
        return isChatShow;
    }

    public void setChatShow(boolean chatShow) {
        isChatShow = chatShow;
    }

    public static Context getContext() {
        return context;
    }
}
