package com.example.webbrowser.webbrowser;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.webbrowser.datasource.BookmarksAdapter;
import com.example.webbrowser.datasource.BookmarksDAO;
import com.example.webbrowser.datasource.IPGeoLocationListener;
import com.example.webbrowser.datasource.IPGeoLocator;
import com.example.webbrowser.datasource.LocationModel;
import com.example.webbrowser.datasource.LocationService;

import de.greenrobot.event.EventBus;

/**
 * Created by username on 30/06/2017.
 */

public class LocationDialog extends AlertDialog {

    private TextView ipTextView;
    private TextView countryTextView;
    private TextView countryCapitalTextView;
    private TextView cityTextView;
    private TextView zipCodeTextView;

    private LinearLayout locationLayout;
    private ProgressBar progressBar;


    protected LocationDialog(@NonNull Context context) {
        super(context);

        LayoutInflater inflater = LayoutInflater.from(context);
        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.dialog_location, null);

        ipTextView = layout.findViewById(R.id.location_dialog_ip_address);
        countryTextView = layout.findViewById(R.id.location_dialog_country);
        countryCapitalTextView = layout.findViewById(R.id.location_dialog_country_capital);
        cityTextView = layout.findViewById(R.id.location_dialog_city);
        zipCodeTextView = layout.findViewById(R.id.location_dialog_zip);

        locationLayout = layout.findViewById(R.id.location_dialog_container);
        progressBar = layout.findViewById(R.id.location_dialog_progress_bar);

        Intent retrieveLocationIntent = new Intent(context, LocationService.class);
        retrieveLocationIntent.setAction(LocationService.COM_WEBBROWSER_RETRIEVE_LOCATION_ACTION);
        context.startService(retrieveLocationIntent);

        setView(layout);
    }

    @Override
    public void show() {
        super.show();

        EventBus.getDefault().register(this);
    }

    @Override
    public void dismiss() {
        super.dismiss();

        EventBus.getDefault().unregister(this);
    }

    public void onEvent(LocationModel locationModel) {
        locationLayout.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);

        ipTextView.setText(locationModel.getIp());
        countryTextView.setText(locationModel.getCountryName());
        countryCapitalTextView.setText(locationModel.getRegionName());
        cityTextView.setText(locationModel.getCity());
        zipCodeTextView.setText(locationModel.getZipCode());
    }

    public void onEvent(String error) {
        locationLayout.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);

        ipTextView.setText("We can't get your location. Please try again later.");
        ipTextView.setTextColor(Color.RED);
    }
}
