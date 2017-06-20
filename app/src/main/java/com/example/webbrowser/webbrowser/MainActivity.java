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
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, TextWatcher, View.OnKeyListener, ViewPager.OnPageChangeListener, NetworkStateReceiver.NetworkStateReceiverListener {

    public static final String FRAGMENT_UIDS_STATE_KEY = "fragmentUidsStateKey";

    private EditText addressBarEditText;
    private Button addNewTabButton;
    private Button menuButton;
    private Button goButton;

    private ViewPager webViewPager;
    private WebViewPagerAdapter webViewPagerAdapter;

    private AlertDialog wifiStatusDialog;

    private ArrayList<String> webViewFragmentTags = new ArrayList<>();
    private HashMap<String, WebViewFragment> webViewFragments = new HashMap<>();

    private NetworkStateReceiver networkStateReceiver;

    //This is not ne
    // eded for now as we do not care which fragments are active.
    private SparseArray<WebViewFragment> activeFragmentsMap = new SparseArray<>();
    private WebViewFragmentBroadcastReceiver webViewReceiver;

    @Override
    public void networkAvailable() {
        if (wifiStatusDialog != null && wifiStatusDialog.isShowing()) {
            wifiStatusDialog.hide();
        }

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
                case Constants.UPDATE_PAGE_TITLE_ACTION:
                    updatePageTitle();
                    break;
                case Constants.WEB_VIEW_DID_LOAD_ACTION:
                    String url = intent.getStringExtra(Constants.WEB_VIEW_FRAGMENT_URL_KEY);
                    webViewDidLoadURL(url);
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
        Toast.makeText(this, "Tab selected: " + tabToSelect, Toast.LENGTH_SHORT).show();
        webViewPager.setCurrentItem(tabToSelect);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addressBarEditText = (EditText) findViewById(R.id.activity_main_address_bar);
        addNewTabButton = (Button) findViewById(R.id.activity_main_new_tab_button);
        menuButton = (Button) findViewById(R.id.activity_main_menu_button);
        goButton = (Button) findViewById(R.id.activity_main_go_button);
        webViewPager = (ViewPager) findViewById(R.id.activity_main_web_view_pager);

        webViewPagerAdapter = new WebViewPagerAdapter(getSupportFragmentManager());

        if (savedInstanceState != null) {
            addressBarEditText.setText(savedInstanceState.getString(Constants.ADDRESS_BAR_TEXT_KEY, ""));
            ArrayList<String> restoredWebViewFragmentTags = savedInstanceState.getStringArrayList(FRAGMENT_UIDS_STATE_KEY);
            if (restoredWebViewFragmentTags != null) {
                webViewFragmentTags = restoredWebViewFragmentTags;
                for (String webViewFragmentTag : webViewFragmentTags) {
                    WebViewFragment webViewFragment = (WebViewFragment) getSupportFragmentManager().getFragment(savedInstanceState, webViewFragmentTag);
                    webViewFragments.put(webViewFragmentTag, webViewFragment);
                }
            }
        } else {
            addNewTab();
        }

        webViewPager.addOnPageChangeListener(this);
        webViewPager.setAdapter(webViewPagerAdapter);
        webViewPager.setOffscreenPageLimit(1);

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
        filter.addAction(Constants.UPDATE_PAGE_TITLE_ACTION);
        filter.addAction(Constants.WEB_VIEW_DID_LOAD_ACTION);
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

    private ArrayList<String> getTitles() {
        ArrayList<String> titles = new ArrayList<>();
//
        for (String tag: webViewFragmentTags) {
            WebViewFragment fragment = webViewFragments.get(tag);
            String title = fragment.getPageTitle();
            titles.add(title);
        }

        return titles;
    }

    private void addNewTab() {
        WebViewFragment webViewFragment = new WebViewFragment();

        //Generate an unique key for each fragment.
        String uid = UUID.randomUUID().toString();
        Log.d(MainActivity.class.getName(), "addingNewtab: " + uid);

        webViewFragmentTags.add(uid);
        webViewFragments.put(uid, webViewFragment);

        webViewPagerAdapter.notifyDataSetChanged();
        webViewPager.setCurrentItem(webViewFragments.size() - 1);

        addressBarEditText.setText("");

        //TODO Why does not this work on all devices?
        showKeyboard();
        addressBarEditText.setFocusableInTouchMode(true);
        addressBarEditText.requestFocus();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        String currentFragmentURL = webViewFragments.get(webViewFragmentTags.get(position)).getFragmentURL();
        addressBarEditText.setText(currentFragmentURL);
    }

    @Override
    public void onPageSelected(int position) {
        Log.d(WebViewFragment.TAG, String.valueOf(position));
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
            return webViewFragments.get(webViewFragmentTags.get(position));
        }

        @Override
        public int getCount() {
            return webViewFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return webViewFragments.get(webViewFragmentTags.get(position)).getPageTitle();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            WebViewFragment webViewFragment = webViewFragments.get(webViewFragmentTags.get(position));
            if (webViewFragment.isAdded()) {
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.remove(webViewFragment);
                fragmentTransaction.commitNow();
            }
            return super.instantiateItem(container, position);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(Constants.ADDRESS_BAR_TEXT_KEY, addressBarEditText.getText().toString());
        outState.putStringArrayList(FRAGMENT_UIDS_STATE_KEY, webViewFragmentTags);
        for (Map.Entry<String, WebViewFragment> webViewFragmentEntry : webViewFragments.entrySet()) {
            if (getSupportFragmentManager().getFragment(outState, webViewFragmentEntry.getKey()) == null) {
                if (!webViewFragmentEntry.getValue().isAdded()) {
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.add(webViewFragmentEntry.getValue(), webViewFragmentEntry.getKey());
                    fragmentTransaction.commitNow();
                }
                getSupportFragmentManager().putFragment(outState, webViewFragmentEntry.getKey(), webViewFragmentEntry.getValue());
            }
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.activity_main_new_tab_button:
                addNewTab();
                break;
            case R.id.activity_main_menu_button:
                tabsButtonPressed();
                break;
            case R.id.activity_main_go_button:
                webViewFragments.get(webViewFragmentTags.get(webViewPager.getCurrentItem())).loadURL(addressBarEditText.getText().toString());
                break;
        }

        hideKeyboard();
    }

    private void tabsButtonPressed() {
        TabsAlertDialog dialog = new TabsAlertDialog(this, getTitles());
        dialog.show();
    }

    private WebViewFragment getCurrentFragment() {
        int position = webViewPager.getCurrentItem();
        String tag = webViewFragmentTags.get(position);

        return webViewFragments.get(tag);
    }

    @Override
    public void onBackPressed() {
        boolean canGoBack = webViewFragments.get(webViewFragmentTags.get(webViewPager.getCurrentItem())).onBackPressed();

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

    public void webViewDidLoadURL(String url) {
        addressBarEditText.setText(url);
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
                    webViewFragments.get(webViewFragmentTags.get(webViewPager.getCurrentItem())).loadURL(addressBarEditText.getText().toString());
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
