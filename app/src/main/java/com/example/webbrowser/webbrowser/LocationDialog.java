package com.example.webbrowser.webbrowser;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.example.webbrowser.datasource.IPGeoLocationListener;
import com.example.webbrowser.datasource.IPGeoLocator;
import com.example.webbrowser.datasource.LocationModel;

/**
 * Created by username on 30/06/2017.
 */

public class LocationDialog extends AlertDialog implements IPGeoLocationListener {


    protected LocationDialog(@NonNull Context context) {
        super(context);

        IPGeoLocator locator = new IPGeoLocator(context);
        locator.fetchLocation();
        locator.setGeoLocationListenerListener(this);
    }

    @Override
    public void geoLocationFetched(LocationModel locationModel) {
        Toast.makeText(getContext(), "We are in " + locationModel.getCity(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void geoLocationFetchFailed(String error) {

    }
}
