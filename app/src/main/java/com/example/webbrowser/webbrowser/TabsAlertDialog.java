package com.example.webbrowser.webbrowser;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.zip.Inflater;

/**
 * Created by username on 19/06/2017.
 */

public class TabsAlertDialog extends AlertDialog {


    protected TabsAlertDialog(@NonNull Context context) {
        super(context);
    }

    public TabsAlertDialog(@NonNull Context context, ArrayList<String> titles) {
        super(context);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, titles);
        LayoutInflater inflater = LayoutInflater.from(context);

        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 192, context.getResources().getDisplayMetrics());
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);

        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.tabs_list_view, null);
        ListView listView = (ListView) layout.findViewById(R.id.tabs_list_view);
//        listView.setLayoutParams(params);
        listView.setAdapter(adapter);

        setView(layout);

        setTitle("Tabs");
        setButton(AlertDialog.BUTTON_NEGATIVE, "Close", (OnClickListener) null);
    }
}
