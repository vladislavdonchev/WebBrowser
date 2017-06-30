package com.example.webbrowser.datasource;

import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.example.webbrowser.webbrowser.WebBrowserApplication;

/**
 * Created by username on 30/06/2017.
 */

public class WriteBookmarkTask extends AsyncTask<Bookmark, Void, Void> {
    @Override
    protected Void doInBackground(Bookmark... bookmarks) {
        for (Bookmark bookmark : bookmarks) {
            BookmarksDAO.insert(bookmark);
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        Toast.makeText(WebBrowserApplication.getContext(), "Bookmark saved!", Toast.LENGTH_SHORT).show();
    }
}
