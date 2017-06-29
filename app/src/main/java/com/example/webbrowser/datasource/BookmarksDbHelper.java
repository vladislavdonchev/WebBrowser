package com.example.webbrowser.datasource;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.webbrowser.webbrowser.WebBrowserApplication;

/**
 * Created by username on 29/06/2017.
 */

public class BookmarksDbHelper extends SQLiteOpenHelper {

    private static BookmarksDbHelper instance = null;

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Bookmarks.db";


    private BookmarksDbHelper (Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static BookmarksDbHelper getInstance() {
        if (instance == null) {
            instance = new BookmarksDbHelper(WebBrowserApplication.getContext());
        }

        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(BookmarksTableMetaData.SQL_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
