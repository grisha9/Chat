package ru.rzn.myasoedov.ga.chat.receiver;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.Date;

import ru.rzn.myasoedov.ga.chat.ChatActivity;
import ru.rzn.myasoedov.ga.chat.ChatApplication;
import ru.rzn.myasoedov.ga.chat.R;
import ru.rzn.myasoedov.ga.chat.db.MessageContract;
import ru.rzn.myasoedov.ga.chat.dto.Message;

/**
 * Created by User on 19.08.2016.
 */
public class BotReceiver extends BroadcastReceiver {
    public static int count = 0;

    private static final String TAG = "BotReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Date date = new Date();
        Message message = new Message(date.toString(), date, false);
        Log.e(TAG, new Date().toString());
        context.getContentResolver().insert(MessageContract.CONTENT_URI, message.toContentValues());
        if (!ChatApplication.isChatShow()) {
            createNotification(message, context);
        }
    }

    private void createNotification(Message message, Context context) {
        count++;
        Intent intent = new Intent(context, ChatActivity.class);
        intent.setAction(BotReceiver.class.getName());
        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(context.getString(R.string.new_message))
                        .setContentText(message.text)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent);

        if (count > 1) {
            builder
                    .setNumber(count)
                    .setContentTitle(context.getString(R.string.new_messages));
        }

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());

    }
}
