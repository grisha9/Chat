package ru.rzn.myasoedov.ga.chat.dto;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.Date;

import ru.rzn.myasoedov.ga.chat.db.MessageContract;

/**
 * Created by grisha on 18.08.16.
 */
public class Message {
    public long id;
    public String text;
    public Date date;
    public boolean isMy;

    public Message() {
    }

    public Message(String text, Date date, boolean isMy) {
        this.text = text;
        this.date = date;
        this.isMy = isMy;
    }

    public Message(Cursor cursor) {
        this.id = cursor.getLong(cursor.getColumnIndex(MessageContract._ID));
        this.text = cursor.getString(cursor.getColumnIndex(MessageContract.TEXT));
        this.date = new Date(cursor.getLong(cursor.getColumnIndex(MessageContract.DATE)));
        this.isMy = cursor.getShort(cursor.getColumnIndex(MessageContract.IS_MY)) == 1;
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(MessageContract.TEXT, text);
        values.put(MessageContract.DATE, date.getTime());
        values.put(MessageContract.IS_MY, isMy);
        return values;
    }
}
