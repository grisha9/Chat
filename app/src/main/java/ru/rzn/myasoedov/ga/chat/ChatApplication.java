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
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import ru.rzn.myasoedov.ga.chat.db.MessageContract;
import ru.rzn.myasoedov.ga.chat.dto.Message;
import ru.rzn.myasoedov.ga.chat.receiver.BotReceiver;
import ru.rzn.myasoedov.ga.chat.service.BotJobService;

/**
 * Created by grisha on 18.08.16.
 */
public class ChatApplication extends Application implements LocationListener {
    public static final String LOCATION_ON_FIRST_RUN = "locationOnFirstRun";
    public static final String BOT = "bot";
    public static final int JOB_ID = 10;
    private static final int PROVIDER_MIN_TIME = 500;
    private PendingIntent pendingIntent;
    private static boolean isChatShow;
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        isChatShow = false;
        context = getApplicationContext();
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

    public void determineLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location bestLocation = getBestKnownLocation(locationManager);
        if (bestLocation != null) {
            addLocation(bestLocation);
        } else {
            getLocationFromProvider(locationManager);

        }
    }

    private Location getBestKnownLocation(LocationManager locationManager) {
        List<String> matchingProviders = locationManager.getAllProviders();
        long currentTimeMillis = System.currentTimeMillis();
        Location bestLocation = null;
        for (String provider : matchingProviders) {
            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                float accuracy = location.getAccuracy();
                long diff = Math.abs(currentTimeMillis - location.getTime());
                if (BuildConfig.LOCATION_OLD_DEELAY > diff) {
                    if (bestLocation == null) {
                        bestLocation = location;
                    } else if (accuracy < bestLocation.getAccuracy()) {
                        bestLocation = location;
                    }
                }
            }
        }
        return bestLocation;
    }

    private void getLocationFromProvider(LocationManager locationManager) {
        boolean isGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (!isGps && !isNetwork) {
            Toast.makeText(this, R.string.no_location_providers, Toast.LENGTH_LONG).show();
            return;
        }
        LocationProvider locationProvider = (isNetwork)
                ? locationManager.getProvider(LocationManager.NETWORK_PROVIDER)
                : locationManager.getProvider(LocationManager.GPS_PROVIDER);
        locationManager.requestLocationUpdates(locationProvider.getName(),
                PROVIDER_MIN_TIME, 0, this);
    }

    private void addLocation(Location bestLocation) {
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putBoolean(LOCATION_ON_FIRST_RUN, true)
                .apply();
        Message message = new Message(String.format(Locale.ENGLISH, "lat=%.2f; lon=%.2f",
                bestLocation.getLatitude(), bestLocation.getLongitude()), new Date(), false);
        context.getContentResolver().insert(MessageContract.CONTENT_URI, message.toContentValues());
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

    @Override
    public void onLocationChanged(Location location) {
        addLocation(location);
        ((LocationManager) getSystemService(Context.LOCATION_SERVICE)).removeUpdates(this);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onProviderDisabled(String provider) {
        ((LocationManager) getSystemService(Context.LOCATION_SERVICE)).removeUpdates(this);
        Toast.makeText(this, getString(R.string.provider_was_disabled, provider),
                Toast.LENGTH_LONG).show();
    }
}
