package com.example.webbrowser.datasource;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by username on 29/06/2017.
 */

public class BookmarksDAO {
    public static void insert(Bookmark bookmark) {
        BookmarksDbHelper helper = BookmarksDbHelper.getInstance();
        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(BookmarksTableMetaData.BOOKMARK_TITLE, bookmark.getTitle());
        values.put(BookmarksTableMetaData.BOOKMARK_URL, bookmark.getUrl());
        values.put(BookmarksTableMetaData.BOOKMARK_TIMESTAMP, bookmark.getTimestamp().getTime());

        Log.i("TAG", String.valueOf(db.insert(BookmarksTableMetaData.TABLE_NAME, null, values)));
    }


    public static Cursor query() {
        BookmarksDbHelper helper = BookmarksDbHelper.getInstance();
        SQLiteDatabase db = helper.getWritableDatabase();

        return db.query(BookmarksTableMetaData.TABLE_NAME, new String[] {BookmarksTableMetaData._ID, BookmarksTableMetaData.BOOKMARK_TITLE, BookmarksTableMetaData.BOOKMARK_URL, BookmarksTableMetaData.BOOKMARK_TIMESTAMP},
                null, null, null, null, null);
    }
}
