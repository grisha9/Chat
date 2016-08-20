package ru.rzn.myasoedov.ga.chat.db;

import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;

import ru.rzn.myasoedov.ga.chat.ChatActivity;
import ru.rzn.myasoedov.ga.chat.ChatApplication;

/**
 * Created by grisha on 20.08.16.
 */
public class MessageObserver extends ContentObserver {
    /**
     * Creates a content observer.
     *
     * @param handler The handler to run {@link #onChange} on, or null if none.
     */
    public MessageObserver(Handler handler) {
        super(handler);
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        super.onChange(selfChange, uri);
        ChatApplication.getContext().sendBroadcast(new Intent(ChatActivity.NEW_MESSAGE_ACTION));
    }
}
