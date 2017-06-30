package com.example.webbrowser.webbrowser;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;

import java.lang.ref.WeakReference;
import java.util.HashMap;

public class BrowserSharedPreferences {

    public static final String TITLE = "title";
    public static final String URL = "url";
    private static String WEB_BROWSER_TABS_COUNT_KEY = "tabsCount";
    private static String WEB_BROWSER_ACTIVE_TAB_KEY = "activeTabs";

    private static Handler handler = new Handler();

    private static class UpdateUIRunnable implements Runnable {
        private HashMap<String, String>[] tabs;
        private WeakReference<BrowserPreferenceReadListener> listener;

        public UpdateUIRunnable(HashMap<String, String>[] tabs, BrowserPreferenceReadListener listener) {
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
            int count = preferences.getInt(WEB_BROWSER_TABS_COUNT_KEY, 0);

//            ArrayList<HashMap<String, String>> result = new ArrayList<HashMap<String, String>>();
            HashMap<String, String>[] result = new HashMap[count];

            for (int index = 0; index < count; ++index) {
                HashMap<String, String> tab = new HashMap<String, String>();

                String title = preferences.getString(index + TITLE, "New Tab");
                String url = preferences.getString(index + URL, "");
                tab.put(TITLE, title);
                tab.put(URL, url);

                result[index] = tab;
            }

            UpdateUIRunnable updateUIRunnable = new UpdateUIRunnable(result, listener.get());
            handler.post(updateUIRunnable);
        }
    }



    private static class SaveTabRunnable implements Runnable {

        private Activity activity;
        private int index;
        private String url;
        private String title;

        public SaveTabRunnable(Activity activity, int index, String url, String title) {
            this.activity = activity;
            this.index = index;
            this.url = url;
            this.title = title;
        }

        @Override
        public void run() {
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
    }

    public static void saveTab(Activity activity, int index, String url, String title) {
        Thread thread = new Thread(new SaveTabRunnable(activity, index, url, title));
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
