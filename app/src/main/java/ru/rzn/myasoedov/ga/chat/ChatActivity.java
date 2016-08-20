package ru.rzn.myasoedov.ga.chat;

import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Date;

import ru.rzn.myasoedov.ga.chat.adapter.MessageCursorAdapter;
import ru.rzn.myasoedov.ga.chat.db.MessageContract;
import ru.rzn.myasoedov.ga.chat.db.MessageObserver;
import ru.rzn.myasoedov.ga.chat.dto.Message;
import ru.rzn.myasoedov.ga.chat.receiver.BotReceiver;


public class ChatActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final int NOT_SCROLL_OFFSET_ITEM = 3;
    public static final String NEW_MESSAGE_ACTION = ChatActivity.class.getName() + ".NEW_MESSAGE";
    private MessageObserver observer;
    private BroadcastReceiver messageDbReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        final ListView listView = (ListView) findViewById(R.id.list_view);
        final Button button = (Button) findViewById(R.id.send);
        final Button newMessageButton = (Button) findViewById(R.id.new_message);
        final TextView text = (TextView) findViewById(R.id.text);

        if (listView == null || button == null || text == null || newMessageButton == null)
            throw new RuntimeException("bad view");

        createListeners(listView, button, newMessageButton, text);
        createBroadcatReceivers(listView, newMessageButton);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getChatApplication().startBot();
        getChatApplication().setChatShow(true);
        observer = new MessageObserver(null);
        registerReceiver(messageDbReceiver, new IntentFilter(NEW_MESSAGE_ACTION));
        getContentResolver().registerContentObserver(
                MessageContract.CONTENT_URI,
                true,
                observer);
    }

    @Override
    protected void onStop() {
        super.onStop();
        getChatApplication().setChatShow(false);
        getContentResolver().unregisterContentObserver(observer);
        unregisterReceiver(messageDbReceiver);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (BotReceiver.class.getName().equals(intent.getAction())) {
            ListView listView = (ListView) findViewById(R.id.list_view);
            if (listView != null) {
                listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, MessageContract.CONTENT_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        ListView listView = (ListView) findViewById(R.id.list_view);
        if (listView == null) return;

        if (listView.getAdapter() == null) {
            listView.setAdapter(new MessageCursorAdapter(this, data));
        } else {
            ((MessageCursorAdapter) listView.getAdapter()).swapCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        ListView listView = (ListView) findViewById(R.id.list_view);
        if (listView != null && listView.getAdapter() instanceof MessageCursorAdapter) {
            ((MessageCursorAdapter) listView.getAdapter()).swapCursor(null);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_exit) {
            getChatApplication().stopBot();
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private ChatApplication getChatApplication() {
        return (ChatApplication) getApplication();
    }


    private void createListeners(final ListView listView, final Button button,
                                 final Button newMessageButton, final TextView text) {
        button.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(text.getText())) {
                Message message = new Message(text.getText().toString(), new Date(), true);
                text.setText("");
                getContentResolver().insert(MessageContract.CONTENT_URI, message.toContentValues());
                newMessageButton.performClick();
            }
        });
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int lastVisiblePosition = view.getLastVisiblePosition();
                //Toast.makeText(ChatActivity.this, firstVisibleItem + "-" + i +"-"+totalItemCount, Toast.LENGTH_LONG).show();
                Log.e("d", firstVisibleItem + "-" + lastVisiblePosition + "-" + totalItemCount);
                if (totalItemCount - lastVisiblePosition < NOT_SCROLL_OFFSET_ITEM) {
                    BotReceiver.count = 0;
                    newMessageButton.setVisibility(View.GONE);
                } else {
                    listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_NORMAL);
                }
            }
        });
        newMessageButton.setOnClickListener(v -> {
            listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
            listView.post(() -> listView.setSelection(listView.getCount() - 1));
        });
    }

    private void createBroadcatReceivers(final ListView listView, final Button newMessageButton) {
        messageDbReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ListAdapter adapter = listView.getAdapter();
                if (adapter != null && (adapter.getCount() - listView.getLastVisiblePosition()) < NOT_SCROLL_OFFSET_ITEM) {
                    //newMessageButton.setVisibility(View.GONE);
                    listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
                } else {
                    newMessageButton.setVisibility(View.VISIBLE);
                }
            }
        };
    }
}
