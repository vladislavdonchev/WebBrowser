package com.example.webbrowser.webbrowser;

import android.content.Context;
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

/**
 * Created by username on 30/06/2017.
 */

public class LocationDialog extends AlertDialog implements IPGeoLocationListener {

    private TextView ipTextView;
    private TextView countryTextView;
    private TextView countryCapitalTextView;
    private TextView cityTextView;
    private TextView zipCodeTextView;

    private LinearLayout locationLayout;
    private ProgressBar progressBar;


    protected LocationDialog(@NonNull Context context) {
        super(context);

        IPGeoLocator locator = new IPGeoLocator(context);
        locator.fetchLocation();
        locator.setGeoLocationListenerListener(this);

        LayoutInflater inflater = LayoutInflater.from(context);
        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.dialog_location, null);

        ipTextView = layout.findViewById(R.id.location_dialog_ip_address);
        countryTextView = layout.findViewById(R.id.location_dialog_country);
        countryCapitalTextView = layout.findViewById(R.id.location_dialog_country_capital);
        cityTextView = layout.findViewById(R.id.location_dialog_city);
        zipCodeTextView = layout.findViewById(R.id.location_dialog_zip);

        locationLayout = layout.findViewById(R.id.location_dialog_container);
        progressBar = layout.findViewById(R.id.location_dialog_progress_bar);

        setView(layout);
    }

    @Override
    public void geoLocationFetched(LocationModel locationModel) {
        Toast.makeText(getContext(), "We are in " + locationModel.getCity(), Toast.LENGTH_LONG).show();

        locationLayout.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);

        ipTextView.setText(locationModel.getIp());
        countryTextView.setText(locationModel.getCountryName());
        countryCapitalTextView.setText(locationModel.getRegionName());
        cityTextView.setText(locationModel.getCity());
        zipCodeTextView.setText(locationModel.getZipCode());
    }

    @Override
    public void geoLocationFetchFailed(String error) {
        locationLayout.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);

        ipTextView.setText("We can't get your location. Please try again later.");
        ipTextView.setTextColor(Color.RED);
    }
}
