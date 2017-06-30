package com.example.webbrowser.datasource;

import android.provider.BaseColumns;

/**
 * Created by username on 29/06/2017.
 */

public class BookmarksTableMetaData implements BaseColumns {
    public static final String TABLE_NAME = "BOOKMARKS";
    public static final String BOOKMARK_TITLE = "title";
    public static final String BOOKMARK_URL = "url";
    public static final String BOOKMARK_TIMESTAMP = "timestamp";


    public static final String SQL_CREATE =
            "CREATE TABLE " + TABLE_NAME + " (" +
             _ID + " INTEGER PRIMARY KEY," +
             BOOKMARK_TITLE + " TEXT," +
             BOOKMARK_URL + " TEXT," +
             BOOKMARK_TIMESTAMP + " LONG)";


}
