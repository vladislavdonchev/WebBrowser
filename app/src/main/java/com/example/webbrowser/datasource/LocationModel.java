package com.example.webbrowser.datasource;

/**
 * Created by username on 30/06/2017.
 */

import com.google.gson.annotations.SerializedName;

public class LocationModel {
    @SerializedName("country_code")
    private String countyCode;

    @SerializedName("country_name")
    private String countryName;

    @SerializedName("region_code")
    private String regionCode;

    @SerializedName("region_name")
    private String regionName;

    @SerializedName("zip_code")
    private String zipCode;

    @SerializedName("time_zone")
    private String timeZone;

    @SerializedName("metro_code")
    private int metroCode;

    private String city;
    private String ip;
    private float latitude;
    private float longitude;

    public String getIp() {
        return ip;
    }

    public String getCountyCode() {
        return countyCode;
    }

    public String getCountryName() {
        return countryName;
    }

    public String getRegionCode() {
        return regionCode;
    }

    public String getRegionName() {
        return regionName;
    }

    public String getCity() {
        return city;
    }

    public String getZipCode() {
        return zipCode;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public float getLatitude() {
        return latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public int getMetroCode() {
        return metroCode;
    }
}
