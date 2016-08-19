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

import java.util.Random;

import ru.rzn.myasoedov.ga.chat.receiver.BotReceiver;
import ru.rzn.myasoedov.ga.chat.service.BotJobService;

/**
 * Created by grisha on 18.08.16.
 */
public class ChatApplication extends Application {
    public static final String BOT = "bot";
    private static Context context;
    private PendingIntent pendingIntent;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        //// TODO: 19.08.2016 get lat lon
        stopBot();
        startBot();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void startBot() {
        int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentApiVersion >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            ComponentName serviceComponent = new ComponentName(context, BotJobService.class);
            JobInfo.Builder builder = new JobInfo.Builder(0, serviceComponent);
            Random random = new Random();
           // builder.setMinimumLatency(BuildConfig.BOT_DEELAY); // wait at least
           // builder.setOverrideDeadline(BuildConfig.BOT_DEELAY + random.nextInt(1000)); // maximum delay
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE); // require unmetered network
            builder.setRequiresDeviceIdle(true); // device should be idle
            builder.setRequiresCharging(false); // we don't care if the device is charging or not
            builder.setPeriodic(5000);
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
    public void stopBot() {
        int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentApiVersion >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            JobScheduler tm = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
            tm.cancelAll();
        } else {
            if (pendingIntent != null) {
                ((AlarmManager) getSystemService(ALARM_SERVICE)).cancel(pendingIntent);
            }
        }
    }

    public static Context getContext() {
        return context;
    }

    public Intent createIntent() {
        Intent intent = new Intent(this, BotReceiver.class);
        intent.setAction(BOT);
        return intent;
    }
}
