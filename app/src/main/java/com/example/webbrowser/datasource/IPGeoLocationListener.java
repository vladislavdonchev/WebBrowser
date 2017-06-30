package com.example.webbrowser.datasource;

/**
 * Created by username on 30/06/2017.
 */
public interface IPGeoLocationListener {
    public void geoLocationFetched(LocationModel locationModel);
    public void geoLocationFetchFailed(String error);
}
