package com.example.webbrowser.webbrowser;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.text.TextUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

public class BrowserSharedPreferences {

    public static final String TITLE = "title";
    public static final String URL = "url";
    public static final String UUID = "uuid";

    private static String WEB_BROWSER_TAB_UUIDs_KEY = "tabUUIDs";
    private static String WEB_BROWSER_ACTIVE_TAB_KEY = "activeTabs";

    private static Handler handler = new Handler();

    private static class UpdateUIRunnable implements Runnable {
        private ArrayList<HashMap<String, String>> tabs;
        private WeakReference<BrowserPreferenceReadListener> listener;

        public UpdateUIRunnable(ArrayList<HashMap<String, String>> tabs, BrowserPreferenceReadListener listener) {
            this.tabs = tabs;
            this.listener = new WeakReference<BrowserPreferenceReadListener>(listener);
        }

        @Override
        public void run() {
            if (listener.get() != null) {
                listener.get().browserTabsLoaded(tabs);
            }
        }
    }

    private static class ReadTabsRunnable implements Runnable {
        private Activity activity;
        private WeakReference<BrowserPreferenceReadListener> listener;

        public ReadTabsRunnable(Activity activity, BrowserPreferenceReadListener listener) {
            this.activity = activity;
            this.listener = new WeakReference<BrowserPreferenceReadListener>(listener);
        }

        @Override
        public void run() {
            SharedPreferences preferences = activity.getPreferences(Context.MODE_PRIVATE);
            String uuidsCSV = preferences.getString(WEB_BROWSER_TAB_UUIDs_KEY, "");
            if (uuidsCSV.length() == 0) {
                return;
            }
            String[] uuids = uuidsCSV.split(",");

            ArrayList<HashMap<String, String>> result = new ArrayList<HashMap<String, String>>();

            for (String uuid: uuids) {
                HashMap<String, String> tab = new HashMap<String, String>();

                String title = preferences.getString(uuid + TITLE, "New Tab");
                String url = preferences.getString(uuid + URL, "");
                tab.put(TITLE, title);
                tab.put(URL, url);
                tab.put(UUID, uuid);

                result.add(tab);
            }

            UpdateUIRunnable updateUIRunnable = new UpdateUIRunnable(result, listener.get());
            handler.post(updateUIRunnable);
        }
    }

    private static class SaveTabRunnable implements Runnable {

        private Activity activity;
        private String url;
        private String title;
        private ArrayList<String> uuids;
        private String uuid;

        public SaveTabRunnable(Activity activity, String uuid, String url, String title, ArrayList<String> uuids) {
            this.activity = activity;
            this.uuid = uuid;
            this.url = url;
            this.title = title;
            this.uuids = uuids;
        }

        @Override
        public void run() {
            SharedPreferences preferences = activity.getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(uuid + TITLE, title);
            editor.putString(uuid + URL, url);

            String csv =  TextUtils.join(",", uuids);
            editor.putString(WEB_BROWSER_TAB_UUIDs_KEY, csv);

            editor.commit();
        }
    }

    private static class RemoveTabRunnable implements Runnable {
        private Activity activity;
        private ArrayList<String> uuids;
        private String uuid;

        public RemoveTabRunnable(Activity activity, String uuid, ArrayList<String> uuids) {
            this.activity = activity;
            this.uuid = uuid;
            this.uuids = uuids;
        }

        @Override
        public void run() {
            SharedPreferences preferences = activity.getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.remove(uuid + TITLE);
            editor.remove(uuid + URL);
            editor.remove(WEB_BROWSER_TAB_UUIDs_KEY);

            String csv =  TextUtils.join(",", uuids);
            editor.putString(WEB_BROWSER_TAB_UUIDs_KEY, csv);
            editor.commit();
        }
    }

    public static void removeTab(Activity activity, String uuid, ArrayList<String> uuids) {
        Thread thread = new Thread(new RemoveTabRunnable(activity, uuid, uuids));
        thread.start();
    }

    public static void saveTab(Activity activity, String uuid, String url, String title, ArrayList<String> uuids) {
        Thread thread = new Thread(new SaveTabRunnable(activity, uuid, url, title, uuids));
        thread.start();
    }

    public static void loadTabs(Activity activity, BrowserPreferenceReadListener listener) {
        Thread thread = new Thread(new ReadTabsRunnable(activity, listener));
        thread.start();
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
