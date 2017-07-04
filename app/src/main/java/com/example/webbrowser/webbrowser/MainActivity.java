package com.example.webbrowser.webbrowser;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.webbrowser.datasource.Bookmark;
import com.example.webbrowser.datasource.LocationService;
import com.example.webbrowser.datasource.WriteBookmarkTask;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, TextWatcher, View.OnKeyListener, ViewPager.OnPageChangeListener, NetworkStateReceiver.NetworkStateReceiverListener, BrowserPreferenceReadListener, TabsAdapterListener {

    public static final String SAVED_INSTANCE_STATE_KEY = "savedInstanceState";
    public static final String ADDRESS_BAR_TEXT_KEY = "textFieldValue";
    public static final String FRAGMENT_UIDS_STATE_KEY = "fragmentUidsState";
    public static final String WEB_PAGE_TITLES_KEY = "webPageTitles";
    public static final String WEBVIEW_STATES_STATE_KEY = "webviewStatesState";
    public static final String WEBVIEW_STATES_URLS_KEY = "webviewUrlsState";
    public static final String ACTIVE_WEB_VIEW_INDEX_KEY = "activeWebviewIndex";

    private EditText addressBarEditText;
    private Button addNewTabButton;
    private Button menuButton;
    private Button goButton;
    private TextView tabsCounter;

    private ViewPager webViewPager;
    private WebViewPagerAdapter webViewPagerAdapter;

    private ArrayList<String> webViewFragmentUids = new ArrayList<>();
    private DualHashBidiMap<String, WebViewFragment> webViewFragments = new DualHashBidiMap<>();
    private HashMap<String, Bundle> restoredWebViewStates = new HashMap<>();
    private HashMap<String, String> restoredWebViewUrls = new HashMap<>();
    private HashMap<String, String> webPageTitles = new HashMap<>();

    private NetworkStateReceiver networkStateReceiver;
    private AlertDialog wifiStatusDialog;

    private WebViewFragmentBroadcastReceiver webViewReceiver;

    public void networkAvailable() {
        if (wifiStatusDialog != null && wifiStatusDialog.isShowing()) {
            wifiStatusDialog.hide();
        }

        //TODO We need to make sure that this is an user-friendly behavior.
        //getCurrentFragment().reload();
    }

    @Override
    public void networkUnavailable() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("WIFI Unavailable");

        alertDialogBuilder
                .setMessage("Do you want to enable WIFI ?")
                .setCancelable(false)
                .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
                    }
                })
                .setNegativeButton("Cancel", null);

        wifiStatusDialog = alertDialogBuilder.create();

        wifiStatusDialog.show();
    }

    public void favouritesTapped(View view) {
        String uid = webViewFragmentUids.get(webViewPager.getCurrentItem());

        Bookmark bookmark = new Bookmark();
        bookmark.setTitle(webPageTitles.get(uid));
        bookmark.setUrl(addressBarEditText.getText().toString());
        bookmark.setTimestamp(new Date());

        WriteBookmarkTask writeTask = new WriteBookmarkTask();
        writeTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, bookmark);
    }

    @Override
    public void browserTabsLoaded(ArrayList<HashMap<String, String>> tabs) {
        for (HashMap<String, String> tab: tabs) {
            WebViewFragment webViewFragment = new WebViewFragment();
            String uid = tab.get(BrowserSharedPreferences.UUID);

            webViewFragment.setUid(uid);

            webViewFragments.put(uid, webViewFragment);
            webViewFragmentUids.add(uid);
            webPageTitles.put(uid, tab.get(BrowserSharedPreferences.TITLE));
            restoredWebViewUrls.put(uid, tab.get(BrowserSharedPreferences.URL));
        }

        tabsCounter.setText(String.valueOf(webViewFragments.size()));
        webViewPagerAdapter.notifyDataSetChanged();
        webViewPager.setCurrentItem(BrowserSharedPreferences.getActiveTabIndex(this));
    }

    @Override
    public void onRemoveTab(int index) {
        String uuid = webViewFragmentUids.get(index);
        removeTab(uuid);
    }

    public class WebViewFragmentBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case Constants.HIDE_KEYBOARD_ACTION:
                    hideKeyboard();
                    break;
                case Constants.WEB_PAGE_LOADED_ACTION:
                    String title = intent.getStringExtra(Constants.WEBVIEW_FRAGMENT_EXTRA_TITLE_KEY);
                    String url = intent.getStringExtra(Constants.WEBVIEW_FRAGMENT_EXTRA_URL_KEY);
                    String uid = intent.getStringExtra(Constants.WEBVIEW_FRAGMENT_EXTRA_UID);

                    Log.d(WebViewFragment.class.getName(), "URL load recevied: " + uid + " " + title + " " + url);

                    webPageTitles.put(uid, title);
                    webViewPagerAdapter.notifyDataSetChanged();
                    urlLoaded(uid, url);
                    break;
                case Constants.TAB_SELECTED_ACTION:
                    selectTab(intent.getIntExtra(Constants.SELECTED_TAB_KEY, 0));
                    break;
                case Constants.OPEN_BOOKMARK:
                    String bookmarkURL = intent.getStringExtra(Constants.WEBVIEW_FRAGMENT_EXTRA_URL_KEY);
                    addNewTab();
                    if (!TextUtils.isEmpty(bookmarkURL)) {
                        webViewFragments.get(webViewFragmentUids.get(webViewPager.getCurrentItem())).loadURL(bookmarkURL);
                    }
                    break;
            }
        }
    }

    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        return isConnected;
    }

    private void selectTab(int tabToSelect) {
        webViewPager.setCurrentItem(tabToSelect);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(MainActivity.class.getName(), "onCreate");
        setContentView(R.layout.activity_main);

        addressBarEditText = (EditText) findViewById(R.id.activity_main_address_bar);
        addNewTabButton = (Button) findViewById(R.id.activity_main_new_tab_button);
        menuButton = (Button) findViewById(R.id.activity_open_tabs_button);
        goButton = (Button) findViewById(R.id.activity_main_go_button);
        webViewPager = (ViewPager) findViewById(R.id.activity_main_web_view_pager);
        tabsCounter = (TextView) findViewById(R.id.activity_main_tabs_counter);

        webViewPager.addOnPageChangeListener(this);
        webViewPager.setOffscreenPageLimit(1);

        goButton.setEnabled(false);

        addNewTabButton.setOnClickListener(this);
        menuButton.setOnClickListener(this);
        goButton.setOnClickListener(this);

        addressBarEditText.setOnKeyListener(this);
        addressBarEditText.addTextChangedListener(this);
        addressBarEditText.setFocusableInTouchMode(true);

        if (savedInstanceState == null) {
            restorePersistedTabs();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(MainActivity.class.getName(), "onStart");

        Bundle savedInstanceState = getIntent().getExtras();
        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState);
        }

        webViewPagerAdapter = new WebViewPagerAdapter(getSupportFragmentManager());
        webViewPager.setAdapter(webViewPagerAdapter);

        if (webViewFragments.size() == 0) {
            addNewTab();
        } else {
            //TODO Discern between first run and loading persisting tabs, and orientation change.
            if (savedInstanceState != null) {
                webViewPager.setCurrentItem(savedInstanceState.getInt(ACTIVE_WEB_VIEW_INDEX_KEY, 0));
            }
        }

        Intent locationServiceIntent = new Intent(this, LocationService.class);
        startService(locationServiceIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar, menu);

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(MainActivity.class.getName(), "onResume");

        if (!isConnected()) {
            networkUnavailable();
        }

        registerNetworkReceiver();
        registerWebViewReceiver();
    }

    private void restorePersistedTabs() {
        BrowserSharedPreferences.loadTabs(this, this);
    }

    private void restoreInstanceState(Bundle savedInstanceState) {
        addressBarEditText.setText(savedInstanceState.getString(ADDRESS_BAR_TEXT_KEY, ""));
        ArrayList<String> restoredWebViewFragmentUids = savedInstanceState.getStringArrayList(FRAGMENT_UIDS_STATE_KEY);
        ArrayList<Bundle> restoredWebViewStates = savedInstanceState.getParcelableArrayList(WEBVIEW_STATES_STATE_KEY);
        ArrayList<String> restoredWebViewUrls = savedInstanceState.getStringArrayList(WEBVIEW_STATES_URLS_KEY);
        ArrayList<String> restoredWebPageTitles = savedInstanceState.getStringArrayList(WEB_PAGE_TITLES_KEY);

        if (restoredWebViewFragmentUids != null) {
            webViewFragmentUids = restoredWebViewFragmentUids;
            for (int i = 0; i < restoredWebViewFragmentUids.size(); i++) {
                String uid = restoredWebViewFragmentUids.get(i);
                WebViewFragment webViewFragment = new WebViewFragment();

                webViewFragment.setUid(uid);

                if (restoredWebViewStates.get(i) != null) {
                    this.restoredWebViewStates.put(uid, restoredWebViewStates.get(i));
                }
                if (restoredWebViewUrls.get(i) != null) {
                    this.restoredWebViewUrls.put(uid, restoredWebViewUrls.get(i));
                }
                webViewFragments.put(uid, webViewFragment);
                webPageTitles.put(uid, restoredWebPageTitles.get(i));
            }
            tabsCounter.setText(String.valueOf(webViewFragments.size()));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(MainActivity.class.getName(), "onPause");

        unregisterNetworkReceiver();
        unregisterWebViewReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Intent locationServiceIntent = new Intent(this, LocationService.class);
        stopService(locationServiceIntent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_bar_my_location:
                checkLocation();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void checkLocation() {
        LocationDialog locationDialog = new LocationDialog(this);
        locationDialog.show();
    }

    private void unregisterNetworkReceiver() {
        unregisterReceiver(networkStateReceiver);
    }

    private void registerWebViewReceiver() {
        webViewReceiver = new WebViewFragmentBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.HIDE_KEYBOARD_ACTION);
        filter.addAction(Constants.WEB_PAGE_LOADED_ACTION);
        filter.addAction(Constants.TAB_SELECTED_ACTION);
        filter.addAction(Constants.OPEN_BOOKMARK);

        registerReceiver(webViewReceiver, filter);
    }

    private void registerNetworkReceiver() {
        networkStateReceiver = new NetworkStateReceiver();
        networkStateReceiver.addListener(this);
        registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
    }

    private void unregisterWebViewReceiver() {
        unregisterReceiver(webViewReceiver);
    }

    private void removeTab(String uuid) {
        WebViewFragment webViewFragment = webViewFragments.get(uuid);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.remove(webViewFragment);
        fragmentTransaction.commitNow();

        webViewFragmentUids.remove(uuid);
        restoredWebViewStates.remove(uuid);
        restoredWebViewUrls.remove(uuid);
        webViewFragments.remove(uuid);
        webPageTitles.remove(uuid);

        webViewPagerAdapter.notifyDataSetChanged();
        webViewPager.setAdapter(webViewPagerAdapter);

        BrowserSharedPreferences.removeTab(this, uuid, webViewFragmentUids);
        updateTabsCounter();
    }

    private void addNewTab() {
        WebViewFragment webViewFragment = new WebViewFragment();

        //Generate an unique key for each fragment.
        String uid = UUID.randomUUID().toString();

        webViewFragment.setUid(uid);

        Log.d(MainActivity.class.getName(), "addingNewtab: " + uid);

        webViewFragmentUids.add(uid);
        webViewFragments.put(uid, webViewFragment);
        webPageTitles.put(uid, "New Tab");

        webViewPagerAdapter.notifyDataSetChanged();
        webViewPager.setCurrentItem(webViewFragments.size() - 1);
        persistTab(webViewFragments.size() - 1);

        updateTabsCounter();

        addressBarEditText.setText("");

        //TODO Why does not this work on all devices?
        addressBarEditText.setFocusableInTouchMode(true);
        addressBarEditText.requestFocus();
        showKeyboard();
    }

    private void updateTabsCounter() {
        tabsCounter.setText(String.valueOf(webViewFragments.size()));
    }

    private void persistTab(int index) {
        WebViewFragment fragment = (WebViewFragment) webViewPagerAdapter.getItem(index);
        String uid = webViewFragmentUids.get(index);
        BrowserSharedPreferences.saveTab(this, uid, fragment.getFragmentURL(), webPageTitles.get(uid), webViewFragmentUids);
    }

    private void removePersitedTab(int index) {
        WebViewFragment fragment = (WebViewFragment) webViewPagerAdapter.getItem(index);
        String uid = webViewFragmentUids.get(index);
        BrowserSharedPreferences.saveTab(this, uid, fragment.getFragmentURL(), webPageTitles.get(uid), webViewFragmentUids);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        BrowserSharedPreferences.setActiveTabIndex(this, position);
        String currentFragmentURL = webViewFragments.get(webViewFragmentUids.get(position)).getFragmentURL();
        addressBarEditText.setText(currentFragmentURL);
        Log.d(WebViewFragment.LOG_TAG, String.valueOf(position));
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private class WebViewPagerAdapter extends FragmentStatePagerAdapter {

        public WebViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Log.d(WebViewFragment.class.getName(), "getItem: " + webViewFragmentUids.get(position));
            WebViewFragment webViewFragment = webViewFragments.get(webViewFragmentUids.get(position));
            return webViewFragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Log.d(WebViewFragment.class.getName(), "getPageTitle: " + position);
            String title = "New Tab";
            String loadedTitle = "";
            try {
                loadedTitle = webPageTitles.get(webViewFragmentUids.get(position));
            } catch (Exception e) {
                Log.w(WebViewFragment.LOG_TAG, e.getMessage());
            }
            if (!TextUtils.isEmpty(loadedTitle)) {
                title = loadedTitle;
            }
            return title;
        }

        @Override
        public int getItemPosition(Object object) {
            Log.d(WebViewFragment.class.getName(), "getItemPosition: " + webViewFragments.getKey(object) + " " + webViewFragmentUids.indexOf(webViewFragments.getKey(object)));
            return webViewFragmentUids.indexOf(webViewFragments.getKey(object));
         }

        @Override
        public int getCount() {
            Log.d(WebViewFragment.class.getName(), "getCount: " + webViewFragments.size());
            return webViewFragments.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            WebViewFragment webViewFragment = (WebViewFragment) super.instantiateItem(container, position);
            if (restoredWebViewStates.containsKey(webViewFragmentUids.get(position))) {
                webViewFragment.setWebViewState(restoredWebViewStates.get(webViewFragmentUids.get(position)));
                restoredWebViewStates.remove(webViewFragmentUids.get(position));
                Log.d(WebViewFragment.LOG_TAG, "restoredWebViewState " + " " + webViewFragmentUids.get(position));
            } else if (restoredWebViewUrls.containsKey(webViewFragmentUids.get(position))) {
                webViewFragment.setPersistedURL(restoredWebViewUrls.get(webViewFragmentUids.get(position)));
                restoredWebViewUrls.remove(webViewFragmentUids.get(position));
                Log.d(WebViewFragment.LOG_TAG, "restoredWebViewUrl " + " " + webViewFragmentUids.get(position));
            }
            return webViewFragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            WebViewFragment webViewFragment = (WebViewFragment) object;
            if (webViewFragments.containsValue(webViewFragment)) {
                restoredWebViewStates.put(webViewFragmentUids.get(position), webViewFragment.getWebViewState());
                Log.d(WebViewFragment.LOG_TAG, "webViewStateToBeRestored " + " " + webViewFragmentUids.get(position));
            }
            super.destroyItem(container, position, object);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(MainActivity.class.getName(), "onSaveInstanceState");
        outState.putString(ADDRESS_BAR_TEXT_KEY, addressBarEditText.getText().toString());
        outState.putStringArrayList(FRAGMENT_UIDS_STATE_KEY, webViewFragmentUids);
        ArrayList<Bundle> savedWebViewStates = new ArrayList<>();
        ArrayList<String> savedWebViewUrls = new ArrayList<>();
        ArrayList<String> webPageTitlesList = new ArrayList<>();
        for (String webViewFragmentKey : webViewFragmentUids) {
            WebViewFragment webViewFragment = webViewFragments.get(webViewFragmentKey);
            //TODO Refator this to use SparseArray instead of saving null values.
            if (!webViewFragment.isAdded()) {
                if (restoredWebViewStates.containsKey(webViewFragmentKey)) {
                    savedWebViewStates.add((Bundle) restoredWebViewStates.get(webViewFragmentKey).clone());
                } else {
                    savedWebViewStates.add(null);
                }
                if (restoredWebViewUrls.containsKey(webViewFragmentKey)) {
                    savedWebViewUrls.add(restoredWebViewUrls.get(webViewFragmentKey));
                } else {
                    savedWebViewUrls.add(null);
                }
            } else {
                savedWebViewStates.add((Bundle) webViewFragment.getWebViewState().clone());
                savedWebViewUrls.add(null);
            }

            Log.d(WebViewFragment.LOG_TAG, "onSaveInstanceState webViewStateToBeRestored " + " " + webViewFragmentKey);
            webPageTitlesList.add(webPageTitles.get(webViewFragmentKey));

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.remove(webViewFragment);
            fragmentTransaction.commitNow();

            webViewFragments.remove(webViewFragmentKey);
        }
        outState.putParcelableArrayList(WEBVIEW_STATES_STATE_KEY, savedWebViewStates);
        outState.putStringArrayList(WEBVIEW_STATES_URLS_KEY, savedWebViewUrls);
        outState.putStringArrayList(WEB_PAGE_TITLES_KEY, webPageTitlesList);
        outState.putInt(ACTIVE_WEB_VIEW_INDEX_KEY, webViewPager.getCurrentItem());

        getIntent().putExtras(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.activity_main_new_tab_button:
                addNewTab();
                break;
            case R.id.activity_open_tabs_button:
                tabsButtonPressed();
                break;
            case R.id.activity_main_go_button:
                webViewFragments.get(webViewFragmentUids.get(webViewPager.getCurrentItem())).loadURL(addressBarEditText.getText().toString());
                break;
        }

        hideKeyboard();
    }

    private void tabsButtonPressed() {
        TabsAdapter tabsAdapter = new TabsAdapter(this, R.layout.tab_item, getWebPageTitlesList(), this);

        TabsAlertDialog tabsAlertDialog = new TabsAlertDialog(this, tabsAdapter);
        tabsAlertDialog.show();
    }

    private ArrayList<String> getWebPageTitlesList() {
        ArrayList<String> webPageTitlesList = new ArrayList<>();
        for (String webViewFragmentUid : webViewFragmentUids) {
            webPageTitlesList.add(webPageTitles.get(webViewFragmentUid));
        }
        return webPageTitlesList;
    }

    private WebViewFragment getCurrentFragment() {
        int position = webViewPager.getCurrentItem();
        String uid = webViewFragmentUids.get(position);

        return webViewFragments.get(uid);
    }

    @Override
    public void onBackPressed() {
        boolean canGoBack = webViewFragments.get(webViewFragmentUids.get(webViewPager.getCurrentItem())).onBackPressed();

        if (!canGoBack) {
            closeApp();
        }
    }

    public void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(
                Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(addressBarEditText.getWindowToken(), 0);
    }

    public void showKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(
                Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(addressBarEditText, InputMethodManager.SHOW_FORCED);
    }

    public void urlLoaded(String fragmentUid, String url) {
        if (webViewFragmentUids.get(webViewPager.getCurrentItem()).equals(fragmentUid)) {
            addressBarEditText.setText(url);
        }

        persistTab(webViewFragmentUids.indexOf(fragmentUid));
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        goButton.setEnabled(editable.length() != 0);
    }

    @Override
    public boolean onKey(View view, int i, KeyEvent keyEvent) {
        if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
            switch (i) {
                case KeyEvent.KEYCODE_ENTER:
                    webViewFragments.get(webViewFragmentUids.get(webViewPager.getCurrentItem())).loadURL(addressBarEditText.getText().toString());
                    hideKeyboard();
                    return true;
                default:
                    break;
            }
        }

        return false;
    }

    public void updatePageTitle() {
        webViewPagerAdapter.notifyDataSetChanged();
    }

    public void closeApp() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
}
