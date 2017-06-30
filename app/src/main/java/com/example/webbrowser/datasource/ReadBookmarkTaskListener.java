package com.example.webbrowser.datasource;

import android.database.Cursor;

/**
 * Created by username on 30/06/2017.
 */

public interface ReadBookmarkTaskListener {
    void bookmarkQueryCompleted(Cursor cursor);
}
