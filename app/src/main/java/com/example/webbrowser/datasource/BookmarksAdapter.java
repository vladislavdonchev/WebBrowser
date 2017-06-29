package com.example.webbrowser.datasource;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.webbrowser.webbrowser.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by username on 29/06/2017.
 */

public class BookmarksAdapter extends CursorAdapter {

    private DateFormat dateFormat;

    public BookmarksAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);

        dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm");
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.bookmark_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        String title = cursor.getString(cursor.getColumnIndex(BookmarksTableMetaData.BOOKMARK_TITLE));
        String timestamp = dateFormat.format(new Date(cursor.getLong((cursor.getColumnIndex(BookmarksTableMetaData.BOOKMARK_TIMESTAMP)))));

        TextView titleTextView = (TextView)view.findViewById(R.id.bookmark_item_title);
        TextView timestampTextView = (TextView)view.findViewById(R.id.bookmark_item_timestamp);

        titleTextView.setText(title);
        timestampTextView.setText(timestamp);
    }

    @Override
    public Object getItem(int position) {
        return super.getItem(position);
    }
}
