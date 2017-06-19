package com.example.webbrowser.webbrowser;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
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
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class MainActivity extends FragmentActivity implements View.OnClickListener, TextWatcher, WebViewFragment.HideKeyboardListener, WebViewFragment.WebViewNavigationListener, View.OnKeyListener, ViewPager.OnPageChangeListener {

    public static final String FRAGMENT_UIDS_STATE_KEY = "fragmentUidsStateKey";

    private EditText addressBarEditText;
    private Button addNewTabButton;
    private Button menuButton;
    private Button goButton;

    private ViewPager webViewPager;
    private WebViewPagerAdapter webViewPagerAdapter;

    private ArrayList<String> webViewFragmentTags = new ArrayList<>();
    private HashMap<String, WebViewFragment> webViewFragments = new HashMap<>();

    //This is not needed for now as we do not care which fragments are active.
    private SparseArray<WebViewFragment> activeFragmentsMap = new SparseArray<>();

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
                    webViewFragments.put(webViewFragmentTag, (WebViewFragment) getSupportFragmentManager().getFragment(savedInstanceState, webViewFragmentTag));
                }
            }
        } else {
            addNewTab();
        }

        webViewPager.addOnPageChangeListener(this);
        webViewPager.setAdapter(webViewPagerAdapter);
        webViewPager.setOffscreenPageLimit(5);

        goButton.setEnabled(false);

        addNewTabButton.setOnClickListener(this);
        menuButton.setOnClickListener(this);
        goButton.setOnClickListener(this);

        addressBarEditText.setOnKeyListener(this);
        addressBarEditText.addTextChangedListener(this);
        addressBarEditText.setFocusableInTouchMode(true);
    }

    private void addNewTab() {
        WebViewFragment webViewFragment = new WebViewFragment();
        webViewFragment.setHideKeyboardListener(MainActivity.this);
        webViewFragment.setNavigationListener(MainActivity.this);

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

//        @Override
//        public Object instantiateItem(ViewGroup container, int position) {
//            WebViewFragment fragment = webViewFragments.get(position);
//            activeFragmentsMap.put(position, fragment);
//            return fragment;
//        }
//
//        @Override
//        public void destroyItem(ViewGroup container, int position, Object object) {
//            activeFragmentsMap.remove(position);
//            super.destroyItem(container, position, object);
//        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(Constants.ADDRESS_BAR_TEXT_KEY, addressBarEditText.getText().toString());
        outState.putStringArrayList(FRAGMENT_UIDS_STATE_KEY, webViewFragmentTags);
        for (Map.Entry<String, WebViewFragment> webViewFragmentEntry : webViewFragments.entrySet()) {
            if (getSupportFragmentManager().getFragment(outState, webViewFragmentEntry.getKey()) == null) {
                getSupportFragmentManager().putFragment(outState, webViewFragmentEntry.getKey(), webViewFragmentEntry.getValue());
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.activity_main_new_tab_button:
                addNewTab();
                break;
            case R.id.activity_main_menu_button:
                Toast.makeText(this, "Menu button clicked", Toast.LENGTH_SHORT).show();
                break;
            case R.id.activity_main_go_button:
                webViewFragments.get(webViewFragmentTags.get(webViewPager.getCurrentItem())).loadURL(addressBarEditText.getText().toString());
                break;
        }

        hideKeyboard();
    }

    @Override
    public void onBackPressed() {
        webViewFragments.get(webViewFragmentTags.get(webViewPager.getCurrentItem())).onBackPressed();
    }

    @Override
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

    @Override
    public void updatePageTitle() {
        webViewPagerAdapter.notifyDataSetChanged();
    }

    @Override
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
