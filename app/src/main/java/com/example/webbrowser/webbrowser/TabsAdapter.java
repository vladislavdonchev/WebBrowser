package com.example.webbrowser.webbrowser;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.List;
/**
 * Created by username on 03/07/2017.
 */
class TabsAdapter extends ArrayAdapter<String> implements View.OnClickListener {

    protected static class ViewHolder{
        protected TextView titleText;
        protected Button deleteButton;
    }

    private WeakReference<TabsAdapterListener> listener;
    private List<String> titles;

    public TabsAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<String> objects, TabsAdapterListener listener) {
        super(context, resource, objects);
        this.listener = new WeakReference<TabsAdapterListener>(listener);
        titles = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View rowView = convertView;
        ViewHolder view;

        if(rowView == null)
        {
            // Get a new instance of the row layout view
            LayoutInflater inflater = LayoutInflater.from(getContext());
            rowView = inflater.inflate(R.layout.tab_item, null);

            // Hold the view objects in an object, that way the don't need to be "re-  finded"
            view = new ViewHolder();
            view.titleText = (TextView) rowView.findViewById(R.id.title_text);
            view.deleteButton = (Button) rowView.findViewById(R.id.tab_close_button);

            rowView.setTag(view);
        } else {
            view = (ViewHolder) rowView.getTag();
        }


        view.titleText.setText(getItem(position));
        view.deleteButton.setTag(position);
        view.deleteButton.setOnClickListener(this);

        return rowView;
    }

    @Override
    public void onClick(View view) {
        int index = (Integer) view.getTag();
        listener.get().onRemoveTab(index);
        titles.remove(index);
        notifyDataSetChanged();
    }
}
