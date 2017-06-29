package com.example.webbrowser.webbrowser;

import android.app.Application;
import android.content.Context;

/**
 * Created by username on 29/06/2017.
 */

public class WebBrowserApplication extends Application {
    private static Context context;

    public void onCreate() {
        super.onCreate();
        WebBrowserApplication.context = getApplicationContext();
    }

    public static Context getContext() {
        return context;
    }
}
