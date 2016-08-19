package ru.rzn.myasoedov.ga.chat.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Date;

import ru.rzn.myasoedov.ga.chat.db.MessageContract;
import ru.rzn.myasoedov.ga.chat.dto.Message;

/**
 * Created by User on 19.08.2016.
 */
public class BotReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Date date = new Date();
        Message message = new Message(date.toString(), date, false);
        Log.e("eee", "eee");
        context.getContentResolver().insert(MessageContract.CONTENT_URI, message.toContentValues());
    }
}
