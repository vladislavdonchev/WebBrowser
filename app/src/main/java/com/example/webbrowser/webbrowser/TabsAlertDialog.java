package com.example.webbrowser.webbrowser;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.webbrowser.datasource.BookmarksAdapter;
import com.example.webbrowser.datasource.BookmarksDAO;

import java.util.ArrayList;
import java.util.zip.Inflater;

/**
 * Created by username on 19/06/2017.
 */

public class TabsAlertDialog extends AlertDialog implements AdapterView.OnItemClickListener, View.OnClickListener {

    private Button openTabsButton;
    private Button bookmarksButton;

    private ListView openTabsList;
    private ListView bookmarksList;

    protected TabsAlertDialog(@NonNull Context context) {
        super(context);
    }

    public TabsAlertDialog(@NonNull Context context, ArrayList<String> titles) {
        super(context);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, R.layout.tab_item, titles);
        LayoutInflater inflater = LayoutInflater.from(context);

        BookmarksAdapter bookmarksAdapter = new BookmarksAdapter(getContext(), BookmarksDAO.query(), 0);

        //int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 192, context.getResources().getDisplayMetrics());
        //ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);

        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.tabs_list_view, null);
        openTabsList = (ListView) layout.findViewById(R.id.tabs_list_view_open);
        openTabsList.setAdapter(adapter);
        openTabsList.setOnItemClickListener(this);
        bookmarksList = (ListView) layout.findViewById(R.id.tabs_list_view_bookmarked);
        bookmarksList.setAdapter(bookmarksAdapter);

        openTabsButton = (Button) layout.findViewById(R.id.tabs_list_view_open_button);
        bookmarksButton = (Button) layout.findViewById(R.id.tabs_list_view_bookmarked_button);

        openTabsButton.setOnClickListener(this);
        bookmarksButton.setOnClickListener(this);

        setView(layout);
        //setTitle("Open Tabs");
        setButton(AlertDialog.BUTTON_NEGATIVE, "Close", (OnClickListener) null);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent intent = new Intent(Constants.TAB_SELECTED_ACTION);
        intent.putExtra(Constants.SELECTED_TAB_KEY, i);
        getContext().sendBroadcast(intent);

        dismiss();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tabs_list_view_open_button:
                openTabsList.setVisibility(View.VISIBLE);
                bookmarksList.setVisibility(View.GONE);
                openTabsButton.setBackgroundResource(android.R.color.transparent);
                bookmarksButton.setBackgroundResource(android.R.color.darker_gray);
                break;
            case R.id.tabs_list_view_bookmarked_button:
                openTabsList.setVisibility(View.GONE);
                bookmarksList.setVisibility(View.VISIBLE);
                openTabsButton.setBackgroundResource(android.R.color.darker_gray);
                bookmarksButton.setBackgroundResource(android.R.color.transparent);
                break;
        }
    }
}
