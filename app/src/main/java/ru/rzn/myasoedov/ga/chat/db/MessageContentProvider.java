package ru.rzn.myasoedov.ga.chat.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import com.tjeannin.provigen.ProviGenProvider;
import com.tjeannin.provigen.helper.TableBuilder;
import com.tjeannin.provigen.helper.TableUpdater;

import ru.rzn.myasoedov.ga.chat.BuildConfig;

/**
 * Created by User on 19.08.2016.
 */
public class MessageContentProvider extends ProviGenProvider {

    @Override
    public Class[] contractClasses() {
        MessageContract.CONTENT_URI = Uri.parse("content://" + getContext().getPackageName() +"/messages");
        return new Class[]{MessageContract.class};
    }

    @Override
    public SQLiteOpenHelper openHelper(Context context) {

        return new SQLiteOpenHelper(getContext(), BuildConfig.DB_NAME, null, BuildConfig.DB_VERSION) {

            @Override
            public void onCreate(SQLiteDatabase database) {
                new TableBuilder(MessageContract.class).createTable(database);
            }

            @Override
            public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
                TableUpdater.addMissingColumns(database, MessageContract.class);
            }
        };
    }
}
