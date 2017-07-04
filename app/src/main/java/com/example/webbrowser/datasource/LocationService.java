package com.example.webbrowser.datasource;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import de.greenrobot.event.EventBus;

/**
 * Created by username on 03/07/2017.
 */

public class LocationService extends Service implements IPGeoLocationListener {

    public static final String COM_WEBBROWSER_RETRIEVE_LOCATION_ACTION = "com.webbrowser.retrieveLocationAction";
    public static final int LOCATION_FETCH_DELAY = 60 * 1000;
    private LocationFetcher fetcher = new LocationFetcher();

    private String lastIPAddress;

    private class LocationFetcher implements Runnable {
        @Override
        public void run() {
            IPGeoLocator locator = new IPGeoLocator(LocationService.this);
            locator.fetchLocation();
            locator.setGeoLocationListenerListener(LocationService.this);
        }
    }

    private Handler locationHandler = new Handler();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && COM_WEBBROWSER_RETRIEVE_LOCATION_ACTION.equals(intent.getAction())) {
            locationHandler.post(fetcher);
        } else {
            locationHandler.postDelayed(fetcher, LOCATION_FETCH_DELAY);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void geoLocationFetched(LocationModel locationModel) {
        locationHandler.postDelayed(fetcher, LOCATION_FETCH_DELAY);

        if (lastIPAddress != null && !lastIPAddress.equals(locationModel.getIp())) {
            Toast.makeText(this, "IP changed from: " + lastIPAddress + " to: " + locationModel.getIp(), Toast.LENGTH_LONG).show();
        }

        lastIPAddress = locationModel.getIp();

        EventBus eventBus = EventBus.getDefault();
        eventBus.post(locationModel);
    }

    @Override
    public void geoLocationFetchFailed(String error) {
        locationHandler.postDelayed(fetcher, LOCATION_FETCH_DELAY);
        EventBus eventBus = EventBus.getDefault();
        eventBus.post(error);
    }

    @Override
    public void onDestroy() {
        locationHandler.removeCallbacks(fetcher);
        super.onDestroy();
    }
}
