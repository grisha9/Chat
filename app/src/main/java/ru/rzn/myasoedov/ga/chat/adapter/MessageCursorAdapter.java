package ru.rzn.myasoedov.ga.chat.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Locale;

import ru.rzn.myasoedov.ga.chat.R;
import ru.rzn.myasoedov.ga.chat.dto.Message;


/**
 * Created by grisha on 18.08.16.
 */
public class MessageCursorAdapter extends CursorAdapter {
    private static final int MY_MESSAGE = 0;
    private static final int BOT_MESSAGE = 1;
    private static final int VIEW_HOLDER = 2123456789;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm",
            Locale.getDefault());
    private final LayoutInflater inflater;

    public MessageCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getItemViewType(int position) {
        Message message = getItem(position);
        if (message.isMy) {
            return MY_MESSAGE;
        } else {
            return BOT_MESSAGE;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        ViewHolder viewHolder = new ViewHolder();
        int itemViewType = getItemViewType(cursor.getPosition());
        View view;
        switch (itemViewType) {
            case MY_MESSAGE:
                view = inflater.inflate(R.layout.message, parent, false);
                break;
            default:
                view = inflater.inflate(R.layout.message_bot, parent, false);
                break;
        }

        viewHolder.message = (TextView) view.findViewById(R.id.message);
        view.setTag(VIEW_HOLDER, viewHolder);
        return view;
    }


    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag(VIEW_HOLDER);
        Message message = new Message(cursor);
        viewHolder.message.setText(message.text);
    }

    @Override
    public Message getItem(int position) {
        Cursor cursor = (Cursor) super.getItem(position);
        return new Message(cursor);
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    private class ViewHolder {
        TextView message;
    }
}
