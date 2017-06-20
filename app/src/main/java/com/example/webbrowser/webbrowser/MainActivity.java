package com.example.webbrowser.webbrowser;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, TextWatcher, View.OnKeyListener, ViewPager.OnPageChangeListener, NetworkStateReceiver.NetworkStateReceiverListener {

    public static final String ADDRESS_BAR_TEXT_KEY = "textFieldValue";
    public static final String FRAGMENT_UIDS_STATE_KEY = "fragmentUidsStateKey";
    public static final String WEB_PAGE_TITLES_KEY = "webPageTitlesKey";
    public static final String WEBVIEW_STATES_STATE_KEY = "webviewStatesStateKey";

    private EditText addressBarEditText;
    private Button addNewTabButton;
    private Button menuButton;
    private Button goButton;
    private TextView tabsCounter;

    private ViewPager webViewPager;
    private WebViewPagerAdapter webViewPagerAdapter;

    private ArrayList<String> webViewFragmentUids = new ArrayList<>();
    private HashMap<String, WebViewFragment> webViewFragments = new HashMap<>();
    private HashMap<String, Bundle> restoredWebViewStates = new HashMap<>();
    private HashMap<String, String> webPageTitles = new HashMap<>();

    private NetworkStateReceiver networkStateReceiver;
    private AlertDialog wifiStatusDialog;

    private WebViewFragmentBroadcastReceiver webViewReceiver;

    @Override
    public void networkAvailable() {
        if (wifiStatusDialog != null && wifiStatusDialog.isShowing()) {
            wifiStatusDialog.hide();
        }

        //TODO We need to make sure that this is an user-friendly behavior.
        getCurrentFragment().reload();
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
        setContentView(R.layout.activity_main);

        addressBarEditText = (EditText) findViewById(R.id.activity_main_address_bar);
        addNewTabButton = (Button) findViewById(R.id.activity_main_new_tab_button);
        menuButton = (Button) findViewById(R.id.activity_open_tabs_button);
        goButton = (Button) findViewById(R.id.activity_main_go_button);
        webViewPager = (ViewPager) findViewById(R.id.activity_main_web_view_pager);
        tabsCounter = (TextView) findViewById(R.id.activity_main_tabs_counter);

        webViewPagerAdapter = new WebViewPagerAdapter(getSupportFragmentManager());

        if (savedInstanceState != null) {
            addressBarEditText.setText(savedInstanceState.getString(ADDRESS_BAR_TEXT_KEY, ""));
            ArrayList<String> restoredWebViewFragmentUids = savedInstanceState.getStringArrayList(FRAGMENT_UIDS_STATE_KEY);
            ArrayList<Bundle> restoredWebViewStates = savedInstanceState.getParcelableArrayList(WEBVIEW_STATES_STATE_KEY);
            ArrayList<String> restoredWebPageTitles = savedInstanceState.getStringArrayList(WEB_PAGE_TITLES_KEY);

            if (restoredWebViewFragmentUids != null) {
                webViewFragmentUids = restoredWebViewFragmentUids;
                for (int i = 0; i < restoredWebViewFragmentUids.size(); i++) {
                    String uid = restoredWebViewFragmentUids.get(i);
                    WebViewFragment webViewFragment = new WebViewFragment();

                    webViewFragment.setUid(uid);

                    this.restoredWebViewStates.put(uid, restoredWebViewStates.get(i));
                    webViewFragments.put(uid, webViewFragment);
                    webPageTitles.put(uid, restoredWebPageTitles.get(i));
                }
                tabsCounter.setText(String.valueOf(webViewFragments.size()));
            }
        } else {
            addNewTab();
        }

        webViewPager.addOnPageChangeListener(this);
        webViewPager.setAdapter(webViewPagerAdapter);
        webViewPager.setOffscreenPageLimit(3);

        goButton.setEnabled(false);

        addNewTabButton.setOnClickListener(this);
        menuButton.setOnClickListener(this);
        goButton.setOnClickListener(this);

        addressBarEditText.setOnKeyListener(this);
        addressBarEditText.addTextChangedListener(this);
        addressBarEditText.setFocusableInTouchMode(true);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!isConnected()) {
            networkUnavailable();
        }

        registerNetworkReceiver();
        registerWebViewReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterNetworkReceiver();
        unregisterWebViewReceiver();
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

    private void addNewTab() {
        WebViewFragment webViewFragment = new WebViewFragment();

        //Generate an unique key for each fragment.
        String uid = UUID.randomUUID().toString();

        webViewFragment.setUid(uid);

        Log.d(MainActivity.class.getName(), "addingNewtab: " + uid);

        webViewFragmentUids.add(uid);
        webViewFragments.put(uid, webViewFragment);

        webViewPagerAdapter.notifyDataSetChanged();
        webViewPager.setCurrentItem(webViewFragments.size() - 1);

        tabsCounter.setText(String.valueOf(webViewFragments.size()));

        addressBarEditText.setText("");

        //TODO Why does not this work on all devices?
        showKeyboard();
        addressBarEditText.setFocusableInTouchMode(true);
        addressBarEditText.requestFocus();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        String currentFragmentURL = webViewFragments.get(webViewFragmentUids.get(position)).getFragmentURL();
        addressBarEditText.setText(currentFragmentURL);
    }

    @Override
    public void onPageSelected(int position) {
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
            String title = "New Tab";
            String loadedTitle = webPageTitles.get(webViewFragmentUids.get(position));
            if (!TextUtils.isEmpty(loadedTitle)) {
                title = loadedTitle;
            }
            return title;
        }

        @Override
        public int getCount() {
            return webViewFragments.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            WebViewFragment webViewFragment = (WebViewFragment) super.instantiateItem(container, position);
            if (restoredWebViewStates.containsKey(webViewFragmentUids.get(position))) {
                webViewFragment.setWebViewState(restoredWebViewStates.get(webViewFragmentUids.get(position)));
                restoredWebViewStates.remove(webViewFragmentUids.get(position));
            }
            return webViewFragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            WebViewFragment webViewFragment = (WebViewFragment) object;
            restoredWebViewStates.put(webViewFragmentUids.get(position), webViewFragment.getWebViewState());
            super.destroyItem(container, position, object);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(ADDRESS_BAR_TEXT_KEY, addressBarEditText.getText().toString());
        outState.putStringArrayList(FRAGMENT_UIDS_STATE_KEY, webViewFragmentUids);
        ArrayList<Bundle> savedWebViewtStates = new ArrayList<>();
        ArrayList<String> webPageTitlesList = new ArrayList<>();
        for (String webViewFragmentKey : webViewFragmentUids) {
            WebViewFragment webViewFragment = webViewFragments.get(webViewFragmentKey);
            savedWebViewtStates.add((Bundle) webViewFragment.getWebViewState().clone());
            webPageTitlesList.add(webPageTitles.get(webViewFragmentKey));

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.remove(webViewFragment);
            fragmentTransaction.commitNow();

            webViewFragments.remove(webViewFragmentKey);
        }
        outState.putStringArrayList(WEB_PAGE_TITLES_KEY, webPageTitlesList);
        outState.putParcelableArrayList(WEBVIEW_STATES_STATE_KEY, savedWebViewtStates);
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
        TabsAlertDialog dialog = new TabsAlertDialog(this, getWebPageTitlesList());
        dialog.show();
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
