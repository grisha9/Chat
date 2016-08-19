package ru.rzn.myasoedov.ga.chat;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Date;

import ru.rzn.myasoedov.ga.chat.adapter.MessageCursorAdapter;
import ru.rzn.myasoedov.ga.chat.db.MessageContract;
import ru.rzn.myasoedov.ga.chat.dto.Message;


public class ChatActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Button button = (Button) findViewById(R.id.send);
        final TextView text = (TextView) findViewById(R.id.text);
        button.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(text.getText())) {
                Message message = new Message(text.getText().toString(), new Date(), true);
                text.setText("");
                getContentResolver().insert(MessageContract.CONTENT_URI, message.toContentValues());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getLoaderManager().initLoader(0, null, this);
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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_exit) {
            ((ChatApplication) this.getApplication()).stopBot();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
