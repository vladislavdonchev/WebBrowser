package com.example.webbrowser.webbrowser;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.util.Pair;

import java.util.HashMap;
import java.util.Set;

/**
 * Created by Asus on 6/21/2017.
 */

public class BrowserSharedPreferences {

    public static final String TITLE = "title";
    public static final String URL = "url";
    private static String WEB_BROWSER_TABS_COUNT_KEY = "tabsCount";
    private static String WEB_BROWSER_ACTIVE_TAB_KEY = "activeTabs";

    public static void saveTab(Activity activity, int index, String url, String title) {
        SharedPreferences preferences = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(index + TITLE, title);
        editor.putString(index + URL, url);

        int count = preferences.getInt(WEB_BROWSER_TABS_COUNT_KEY, 0);
        if (!preferences.contains(index + TITLE)) {
            editor.putInt(WEB_BROWSER_TABS_COUNT_KEY, ++count);
        }

        editor.commit();
    }

    public static HashMap<String, String> loadTab(Activity activity, int index) {
        SharedPreferences preferences = activity.getPreferences(Context.MODE_PRIVATE);

        HashMap<String, String> result = new HashMap<String, String>();
        String title = preferences.getString(index + TITLE, "New Tab");
        String url = preferences.getString(index + URL, "");
        result.put(TITLE, title);
        result.put(URL, url);

        return result;
    }

    public static int getTabsCount(Activity activity) {
        SharedPreferences preferences = activity.getPreferences(Context.MODE_PRIVATE);
        return preferences.getInt(WEB_BROWSER_TABS_COUNT_KEY, 0);
    }

    public static void setActiveTabIndex(Activity activity, int activeTabIndex) {
        SharedPreferences preferences = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(WEB_BROWSER_ACTIVE_TAB_KEY, activeTabIndex);
        editor.commit();
    }

    public static int getActiveTabIndex(Activity activity) {
        SharedPreferences preferences = activity.getPreferences(Context.MODE_PRIVATE);
        return preferences.getInt(WEB_BROWSER_ACTIVE_TAB_KEY, 0);
    }
}
