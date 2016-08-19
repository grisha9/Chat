package ru.rzn.myasoedov.ga.chat;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import ru.rzn.myasoedov.ga.chat.receiver.BotReceiver;

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
        startBot();
    }

    private void startBot() {
        Intent intent = createIntent();
        pendingIntent = PendingIntent.getBroadcast(this, 1, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        stopBot();
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + BuildConfig.BOT_DEELAY,
                BuildConfig.BOT_DEELAY, pendingIntent);

    }

    public void stopBot() {
        if (pendingIntent != null) {
            ((AlarmManager) getSystemService(ALARM_SERVICE)).cancel(pendingIntent);
        }
     }

    public static Context getContext() {
        return context;
    }

    private Intent createIntent() {
        Intent intent = new Intent(this, BotReceiver.class);
        intent.setAction(BOT);
        return intent;
    }
}
