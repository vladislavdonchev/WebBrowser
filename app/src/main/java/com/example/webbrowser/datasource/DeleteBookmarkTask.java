package com.example.webbrowser.datasource;

import android.os.AsyncTask;

import java.lang.ref.WeakReference;

public class DeleteBookmarkTask extends AsyncTask<Long, Void, Void> {


    private WeakReference<DeleteBookmarkTaskListener> listener;

    public DeleteBookmarkTask(DeleteBookmarkTaskListener listener) {
        super();

        this.listener = new WeakReference<DeleteBookmarkTaskListener>(listener);
    }

    @Override
    protected Void doInBackground(Long... bookmarkIds) {
        for (Long bookmarkId : bookmarkIds) {
            BookmarksDAO.delete(bookmarkId);
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        if (listener.get() != null) {
            listener.get().bookmarkDeleted();
        }
    }
}
