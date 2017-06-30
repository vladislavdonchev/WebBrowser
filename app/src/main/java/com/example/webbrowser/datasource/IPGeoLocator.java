package com.example.webbrowser.datasource;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class IPGeoLocator {

    public void setGeoLocationListenerListener(IPGeoLocationListener geoLocationListenerListener) {
        this.geoLocationListenerListener = new WeakReference<IPGeoLocationListener>(geoLocationListenerListener);
    }

    private WeakReference<IPGeoLocationListener> geoLocationListenerListener;

    private Context context;

    public IPGeoLocator(Context context) {
        this.context = context;
    }

    public void fetchLocation() {
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = "http://www.freegeoip.net/json/";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        GsonBuilder gsonBuilder = new GsonBuilder();
                        Gson gson = gsonBuilder.create();

                        if (geoLocationListenerListener.get() != null) {
                            LocationModel locationModel = gson.fromJson(response, LocationModel.class);
                            geoLocationListenerListener.get().geoLocationFetched(locationModel);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (geoLocationListenerListener.get() != null) {
                    geoLocationListenerListener.get().geoLocationFetchFailed(error.getLocalizedMessage());
                }
            }
        });

        queue.add(stringRequest);
    }
}
