package com.example.webbrowser.datasource;

import android.database.Cursor;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;

public class ReadBookmarksTask extends AsyncTask<Void, Void, Cursor> {

    private WeakReference<ReadBookmarkTaskListener> listener;

    public ReadBookmarksTask(ReadBookmarkTaskListener listener) {
        super();

        this.listener = new WeakReference<ReadBookmarkTaskListener>(listener);
    }

    @Override
    protected Cursor doInBackground(Void... voids) {

        return BookmarksDAO.query();
    }

    @Override
    protected void onPostExecute(Cursor cursor) {
        super.onPostExecute(cursor);

        if (listener.get() != null) {
            listener.get().bookmarkQueryCompleted(cursor);
        }
    }
}
