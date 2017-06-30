package com.example.webbrowser.datasource;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.webbrowser.webbrowser.Constants;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by username on 29/06/2017.
 */

public class BookmarksDAO {
    public static void insert(Bookmark bookmark) {
        BookmarksDbHelper helper = BookmarksDbHelper.getInstance();
        SQLiteDatabase db = helper.getWritableDatabase();

        Log.i("TAG", String.valueOf(db.insert(BookmarksTableMetaData.TABLE_NAME, null, createContentValues(bookmark))));
    }


    public static Cursor query() {
        BookmarksDbHelper helper = BookmarksDbHelper.getInstance();
        SQLiteDatabase db = helper.getWritableDatabase();

        return db.query(BookmarksTableMetaData.TABLE_NAME, new String[] {BookmarksTableMetaData._ID, BookmarksTableMetaData.BOOKMARK_TITLE, BookmarksTableMetaData.BOOKMARK_URL, BookmarksTableMetaData.BOOKMARK_TIMESTAMP},
                null, null, null, null, null);
    }

    public static void delete(long bookmarkID) {
        BookmarksDbHelper helper = BookmarksDbHelper.getInstance();
        SQLiteDatabase db = helper.getWritableDatabase();
        String whereClause = BookmarksTableMetaData._ID + "=?";
        String[] arguments = new String[] {String.valueOf(bookmarkID)};
        db.delete(BookmarksTableMetaData.TABLE_NAME, whereClause, arguments);
    }

    public static Bookmark getBookmark(Cursor cursor) {
        Bookmark bookmark = new Bookmark();
        bookmark.setID(cursor.getLong(cursor.getColumnIndex(BookmarksTableMetaData._ID)));
        bookmark.setTitle(cursor.getString(cursor.getColumnIndex(BookmarksTableMetaData.BOOKMARK_TITLE)));
        bookmark.setUrl(cursor.getString(cursor.getColumnIndex(BookmarksTableMetaData.BOOKMARK_URL)));
        bookmark.setTimestamp((new Date(cursor.getLong((cursor.getColumnIndex(BookmarksTableMetaData.BOOKMARK_TIMESTAMP))))));

        return bookmark;
    }

    public static ContentValues createContentValues(Bookmark bookmark) {
        ContentValues values = new ContentValues();
        values.put(BookmarksTableMetaData.BOOKMARK_TITLE, bookmark.getTitle());
        values.put(BookmarksTableMetaData.BOOKMARK_URL, bookmark.getUrl());
        values.put(BookmarksTableMetaData.BOOKMARK_TIMESTAMP, bookmark.getTimestamp().getTime());
        return values;
    }
}
