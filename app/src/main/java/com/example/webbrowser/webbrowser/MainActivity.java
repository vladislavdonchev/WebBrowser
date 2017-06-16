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
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends FragmentActivity implements View.OnClickListener, TextWatcher, WebViewFragment.HideKeyboardListener, WebViewFragment.WebViewNavigationListener, View.OnKeyListener {

    private EditText addressBarEditText;
    private Button addNewTabButton;
    private ImageButton menuButton;
    private Button goButton;
    private ViewPager webViewPager;

    private ArrayList<WebViewFragment> webViewFragments = new ArrayList<>();

    private WebViewPagerAdapter webViewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addressBarEditText = (EditText) findViewById(R.id.activity_main_address_bar);
        addNewTabButton = (Button) findViewById(R.id.activity_main_new_tab_button);
        menuButton = (ImageButton) findViewById(R.id.activity_main_menu_button);
        goButton = (Button) findViewById(R.id.activity_main_go_button);
        webViewPager = (ViewPager) findViewById(R.id.activity_main_web_view_pager);

        webViewPagerAdapter = new WebViewPagerAdapter(getSupportFragmentManager());

        webViewPager.setAdapter(webViewPagerAdapter);

        goButton.setEnabled(false);

        addNewTab();

        addNewTabButton.setOnClickListener(this);
        menuButton.setOnClickListener(this);
        goButton.setOnClickListener(this);

        addressBarEditText.setOnKeyListener(this);
        addressBarEditText.addTextChangedListener(this);
        addressBarEditText.setFocusableInTouchMode(true);

        if (savedInstanceState != null) {
            addressBarEditText.setText(savedInstanceState.getString(Constants.ADDRESS_BAR_TEXT_KEY, ""));
        }
    }

    private void addNewTab() {
        WebViewFragment webViewFragment = new WebViewFragment();
        webViewFragments.add(webViewFragment);
        webViewPagerAdapter.notifyDataSetChanged();
        webViewPager.setCurrentItem(webViewFragments.size() - 1);

        webViewFragment.setHideKeyboardListener(this);
        webViewFragment.setNavigationListener(this);

        addressBarEditText.setText("");
        showKeyboard();
        addressBarEditText.setFocusableInTouchMode(true);
        addressBarEditText.requestFocus();
    }

    private class WebViewPagerAdapter extends FragmentStatePagerAdapter {

        public WebViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return webViewFragments.get(position);
        }

        @Override
        public int getCount() {
            return webViewFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return webViewFragments.get(webViewPager.getCurrentItem()).getPageTitle();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(Constants.ADDRESS_BAR_TEXT_KEY, addressBarEditText.getText().toString());
        webViewFragments.get(webViewPager.getCurrentItem()).saveWebViewState(outState);
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
                webViewFragments.get(webViewPager.getCurrentItem()).loadURL(addressBarEditText.getText().toString());
                break;
        }

        hideKeyboard();
    }

    @Override
    public void onBackPressed() {
        webViewFragments.get(webViewPager.getCurrentItem()).onBackPressed();
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
                    webViewFragments.get(webViewPager.getCurrentItem()).loadURL(addressBarEditText.getText().toString());
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
